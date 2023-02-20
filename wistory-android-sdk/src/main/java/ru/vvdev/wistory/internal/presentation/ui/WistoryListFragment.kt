package ru.vvdev.wistory.internal.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.IFlexible
import kotlinx.android.synthetic.main.wistory_list_fragment.*
import ru.vvdev.wistory.R
import ru.vvdev.wistory.UiConfig
import ru.vvdev.wistory.internal.data.models.Story
import ru.vvdev.wistory.internal.domain.events.*
import ru.vvdev.wistory.internal.presentation.items.FavoriteStoryItem
import ru.vvdev.wistory.internal.presentation.items.PlaceholderStoryItem
import ru.vvdev.wistory.internal.presentation.items.ReadedStoryItem
import ru.vvdev.wistory.internal.presentation.items.StoryItem

internal open class WistoryListFragment : AbstractWistoryFragment(),
    StoryItem.OnStoryClickListener {

    private var flexAdapter = FlexibleAdapter<IFlexible<*>>(listOf())
    private var favoriteStoryItem: FavoriteStoryItem? = null


    override fun currentFragment(): Fragment = this

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
        initAdapter()

        viewModel.mStoryItems.sub { list ->
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
                recyclerView.context, RecyclerView.HORIZONTAL, false
            )
            adapter = flexAdapter.apply {
                for (i in 0..4) {
                    addItem(PlaceholderStoryItem())
                }
            }
        }
    }

    private fun setItems(list: ArrayList<Story>) {
        removeFavoriteItem()
        val currentItemsCount = getStoryItemsCount()
        requireActivity().runOnUiThread {
            viewModel.mStoryItems.value?.let {
                if (currentItemsCount > it.size) {
                    flexAdapter.removeRange(it.size, currentItemsCount - it.size)
                }
                list.forEachIndexed { index, story ->
                    addOrUpdateItems(index, itemFactory(story))
                }
            }
        }
    }

    private fun itemFactory(story: Story): StoryItem {
        return if (story.fresh) {
            StoryItem(context, story, this@WistoryListFragment)
        } else {
            ReadedStoryItem(context, story, this@WistoryListFragment)
        }
    }

    private fun addOrUpdateItems(
        index: Int,
        story: StoryItem
    ) {
        if (index < getStoryItemsCount()) {
            flexAdapter.updateItem(index, story, null)
        } else {
            flexAdapter.addItem(story)
        }
    }

    private fun addOrUpdateFavoriteItem(
        index: Int,
        story: FavoriteStoryItem
    ) {
        if (index < getStoryItemsCount()) {
            flexAdapter.updateItem(index, story, null)
        } else {
            flexAdapter.addItem(story)
        }
    }

    private fun getStoryItemsCount(): Int {
        var storyItemsCount = 0
        flexAdapter.currentItems.forEach { if (it is StoryItem || it is PlaceholderStoryItem) storyItemsCount++ }
        return storyItemsCount
    }

    private fun setFavoriteItems(list: ArrayList<Story>) {
        removeFavoriteItem()
        if (list.isNotEmpty()) {
            favoriteStoryItem =
                FavoriteStoryItem(activity, list, this@WistoryListFragment)

            flexAdapter.addItem(flexAdapter.currentItems.size, favoriteStoryItem!!)
        } else {
            favoriteStoryItem = null
        }
    }

    private fun removeFavoriteItem() {
        flexAdapter.apply {
            getItem(currentItems.size - 1)?.let {
                if (it is FavoriteStoryItem) {
                    removeItem(currentItems.size - 1)
                }
            }
        }
    }

    private fun updateItemAtStoryView(storyId: String, story: Story) {
        requireActivity().runOnUiThread {
            getItemById(storyId)?.let {
                flexAdapter.updateItem(it.apply { data = story })
            }
            getStoryPositionById(storyId)?.let { position ->
                viewModel.mStoryItems.value?.add(position, story)
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
                viewModel.mStoryItems.value?.remove(story)
                viewModel.mStoryItems.value?.add(position, story)
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
        viewModel.mStoryItems.value?.forEachIndexed { index, story ->
            if (story._id == id) {
                viewModel.mStoryItems.value?.remove(story)
                return index
            }
        }
        return null
    }

    override fun onRead(storyId: String, snapHash: Int) {
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

    companion object {
        fun newInstance(
            token: String?,
            serverUrl: String?,
            registrationId: String?,
            config: UiConfig?,
            isAutoOpenUnreadStory: Boolean
        ): WistoryListFragment {
            val args = Bundle()
            args.putString(TOKEN, token)
            args.putSerializable(CONFIG, config)
            args.putString(SERVER_URL, serverUrl)
            args.putString(REGISTRATION_ID, registrationId)
            args.putBoolean(IS_OPEN_FROM_UNREAD_STORY, isAutoOpenUnreadStory)

            val fragment = WistoryListFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
