package ru.vvdev.wistory.internal.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.IFlexible
import kotlinx.android.synthetic.main.wistory_list_fragment.*
import ru.vvdev.wistory.R
import ru.vvdev.wistory.UiConfig
import ru.vvdev.wistory.internal.data.models.Story
import ru.vvdev.wistory.internal.data.repository.StoriesRepository
import ru.vvdev.wistory.internal.domain.events.*
import ru.vvdev.wistory.internal.presentation.callback.WistoryCommunication
import ru.vvdev.wistory.internal.presentation.items.FavoriteStoryItem
import ru.vvdev.wistory.internal.presentation.items.PlaceholderStoryItem
import ru.vvdev.wistory.internal.presentation.items.ReadedStoryItem
import ru.vvdev.wistory.internal.presentation.items.StoryItem
import ru.vvdev.wistory.internal.presentation.viewmodel.WistoryViewModel

internal open class WistoryListFragment : AbstractWistoryFragment(),
    StoryItem.OnStoryClickListener {

    private var flexAdapter = FlexibleAdapter<IFlexible<*>>(listOf())
    private var favoriteStoryItem: FavoriteStoryItem? = null

    companion object {
        fun newInstance(
            token: String?,
            serverUrl: String?,
            registrationId: String?,
            config: UiConfig?
        ): WistoryListFragment {
            val args = Bundle()
            args.putString(TOKEN, token)
            args.putSerializable(CONFIG, config)
            args.putString(SERVER_URL, serverUrl)
            args.putString(REGISTRATION_ID, registrationId)

            val fragment = WistoryListFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initWistoryParams(arguments: Bundle?) {
        token = arguments?.getString(TOKEN) ?: ""
        serverUrl = arguments?.getString(SERVER_URL) ?: ""
        registrationId = arguments?.getString(REGISTRATION_ID)
        config = arguments?.getSerializable(CONFIG) as UiConfig
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initWistoryParams(arguments)
        return inflater.inflate(R.layout.wistory_list_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
        initApiService()
        initAdapter()

        viewModel = ViewModelProviders.of(requireActivity(), ViewModelFactory(StoriesRepository()))
            .get(WistoryViewModel::class.java)

        viewModel.register()

        viewModel.storyItems.sub { list ->
            list?.let {
                setItems(it)
            }
        }
        viewModel.favoriteStoryItems.sub { items ->
            items?.let {
                setFavoriteItems(it)
            }
        }
        viewModel.updateLiveData.sub { events ->
            events?.forEach { event ->
                event.story.run {
                    when (event) {
                        is UpdateOnReadEvent -> readItemAtStoryView(_id, this)
                        is UpdateOnRelationEvent -> updateItemAtStoryView(_id, this)
                        is UpdateOnPollEvent -> updateItemAtStoryView(_id, this)
                        is UpdateOnFavoriteEvent -> updateItemAtStoryView(_id, this)
                    }
                }
            }
            viewModel.clearUpdated()
        }
        viewModel.errorLiveData.sub {
            it?.let {
                postException(it)
                viewModel.errorLiveData.value = null
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.publishEvents()
    }

    private fun navigateToFavoriteList() {
        requireActivity().startActivity(Intent(activity, StoryActivity::class.java).apply {
            putExtra(StoryActivity.ARG_TYPE, StoryActivity.TYPE_FAVORITES)
            putExtra(
                StoryActivity.ARG_STORIES,
                viewModel.favoriteStoryItems.value?.toTypedArray()
            )
            putExtra(StoryActivity.ARG_SETTINGS, config)
        })
    }

    private fun initAdapter() {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(
                recyclerView.context,
                RecyclerView.HORIZONTAL,
                false
            )
            adapter = flexAdapter.apply {
                for (i in 0..4)
                    addItem(PlaceholderStoryItem())
            }
        }
    }

    private fun setItems(list: ArrayList<Story>) {
        removeFavoriteItem()
        val currentItemsCount = getStoryItemsCount()
        requireActivity().runOnUiThread {
            flexAdapter.apply {
                viewModel.storyItems.value?.let {
                    if (currentItemsCount > it.size) {
                        removeRange(it.size, currentItemsCount - it.size)
                    }
                    list.forEachIndexed { index, story ->
                        addOrUpdateItems(
                            index, if (story.fresh)
                                StoryItem(context, story, this@WistoryListFragment)
                            else
                                ReadedStoryItem(context, story, this@WistoryListFragment)
                        )
                    }
                }
            }
        }
    }

    private fun addOrUpdateItems(
        index: Int,
        story: StoryItem
    ) {
        if (index < getStoryItemsCount())
            flexAdapter.updateItem(index, story, null)
        else
            flexAdapter.addItem(story)
    }

    private fun addOrUpdateFavoriteItem(
        index: Int,
        story: FavoriteStoryItem
    ) {
        if (index < getStoryItemsCount())
            flexAdapter.updateItem(index, story, null)
        else
            flexAdapter.addItem(story)
    }

    private fun getStoryItemsCount(): Int {
        var storyItemsCount = 0
        flexAdapter.currentItems.forEach { if (it is StoryItem || it is PlaceholderStoryItem) storyItemsCount++ }
        return storyItemsCount
    }

    private fun setFavoriteItems(list: ArrayList<Story>) {
        flexAdapter.apply {
            removeFavoriteItem()
            if (list.isNotEmpty()) {
                favoriteStoryItem =
                    FavoriteStoryItem(activity, list, this@WistoryListFragment)

                addItem(currentItems.size, favoriteStoryItem!!)
            } else {
                favoriteStoryItem = null
            }
        }
    }

    private fun removeFavoriteItem() {
        flexAdapter.apply {
            getItem(currentItems.size - 1)?.let {
                if (it is FavoriteStoryItem)
                    removeItem(currentItems.size - 1)
            }
        }
    }

    private fun updateItemAtStoryView(storyId: String, story: Story) {
        requireActivity().runOnUiThread {
            getItemById(storyId)?.let {
                flexAdapter.updateItem(it.apply { data = story })
            }
            getStoryPositionById(storyId)?.let { position ->
                viewModel.storyItems.value?.add(position, story)
            }
        }
    }

    private fun readItemAtStoryView(storyId: String, story: Story) {
        requireActivity().runOnUiThread {
            getStoryPositionById(storyId)?.let { position ->
                flexAdapter.removeItem(position)
                flexAdapter.addItem(
                    position,
                    ReadedStoryItem(context, story, this@WistoryListFragment)
                )
                viewModel.storyItems.value?.remove(story)
                viewModel.storyItems.value?.add(position, story)
            }
        }
    }

    private fun getItemById(id: String): StoryItem? {
        flexAdapter.currentItems.forEach {
            return when (it) {
                is StoryItem -> if (it.data._id == id) it else null
                else -> null
            }
        }
        return null
    }

    private fun getStoryPositionById(id: String): Int? {
        viewModel.storyItems.value?.forEachIndexed { index, story ->
            if (story._id == id) {
                viewModel.storyItems.value?.remove(story)
                return index
            }
        }
        return null
    }

    override fun onRead(storyId: String) {
        viewModel.onRead(storyId)
    }

    override fun onPoll(storyId: String, sheet: Int, newpoll: String?, oldpoll: String?) {
        viewModel.onPoll(storyId, sheet, newpoll)
    }

    override fun onRelation(storyId: String, relation: String) {
        viewModel.onRelation(storyId, relation)
    }

    override fun onFavorite(storyId: String, favorite: Boolean) {
        viewModel.onFavorite(storyId, favorite)
    }

    override fun onStoryClick(position: Int) {
        navigateToStory(position)
    }

    override fun onFavoriteItemClick() {
        navigateToFavoriteList()
    }
}
