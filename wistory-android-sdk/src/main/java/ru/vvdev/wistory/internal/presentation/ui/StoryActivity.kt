package ru.vvdev.wistory.internal.presentation.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager.widget.ViewPager
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.IFlexible
import kotlinx.android.synthetic.main.wistory_activity.*
import ru.vvdev.wistory.R
import ru.vvdev.wistory.UiConfig
import ru.vvdev.wistory.internal.data.models.Story
import ru.vvdev.wistory.internal.domain.events.BaseEvent
import ru.vvdev.wistory.internal.domain.events.NavigateEvent
import ru.vvdev.wistory.internal.domain.events.StoryCompleteEvent
import ru.vvdev.wistory.internal.domain.events.UpdateEvent
import ru.vvdev.wistory.internal.presentation.callback.StoryFragmentCallback
import ru.vvdev.wistory.internal.presentation.callback.WistoryCommunication
import ru.vvdev.wistory.internal.presentation.decorators.GridSpacingItemDecoration
import ru.vvdev.wistory.internal.presentation.items.PagerAdapter
import ru.vvdev.wistory.internal.presentation.items.StoryItem
import ru.vvdev.wistory.internal.presentation.utils.DepthTransformation
import ru.vvdev.wistory.internal.presentation.views.extentions.removeItemDecorations

internal class StoryActivity : AppCompatActivity(), StoryFragmentCallback,
    StoryItem.OnStoryClickListener {

    companion object {
        const val ARG_TYPE = "ARG_TYPE"
        const val ARG_STORIES = "ARG_STORIES"
        const val ARG_SETTINGS = "ARG_SETTINGS"
        const val ARG_POSITION = "ARG_POSITION"
        const val ARG_URL = "ARG_URL"
        const val TYPE_STORIES = "TYPE_STORIES"
        const val TYPE_WEBVIEW = "TYPE_WEBVIEW"
        const val TYPE_FAVORITES = "TYPE_FAVORITES"
    }

    private lateinit var pagerAdapter: PagerAdapter
    private var flexAdapter = FlexibleAdapter<IFlexible<*>>(listOf())
    private var list: Array<Story>? = null
    private var url: String? = null
    private var config: UiConfig? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val type = intent.getStringExtra(ARG_TYPE)
        if (type == TYPE_STORIES)
            setTheme(R.style.Wistory_StoryActivity)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wistory_activity)

        when (type) {
            TYPE_STORIES -> {
                list = intent.getSerializableExtra(ARG_STORIES) as Array<Story>
                config = intent.getSerializableExtra(ARG_SETTINGS) as UiConfig

                setTheme(R.style.Wistory_StoryActivity)
                viewPager.visibility = View.VISIBLE
                recyclerViewLayout.visibility = View.GONE
                webViewLayout.visibility = View.GONE
                val position = intent.getIntExtra(ARG_POSITION, 0)
                setupStoriesViewPager(list, config, position)
            }
            TYPE_FAVORITES -> {
                list = intent.getSerializableExtra(ARG_STORIES) as Array<Story>
                config = intent.getSerializableExtra(ARG_SETTINGS) as UiConfig

                viewPager.visibility = View.GONE
                recyclerViewLayout.visibility = View.VISIBLE
                webViewLayout.visibility = View.VISIBLE
                setupFavoriteList(list)
                toolbar.setNavigationOnClickListener {
                    finish()
                }
            }
            TYPE_WEBVIEW -> {
                url = intent.getStringExtra(ARG_URL) as String

                viewPager.visibility = View.GONE
                recyclerViewLayout.visibility = View.GONE
                webViewLayout.visibility = View.VISIBLE
                showWebView(url)
                toolbarWebView.setNavigationOnClickListener {
                    finish()
                }
            }
        }
    }

    private fun showWebView(url: String?) {
        webView.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                allowContentAccess = true
                allowFileAccess = true
                allowFileAccessFromFileURLs = true
                javaScriptCanOpenWindowsAutomatically = true
                loadsImagesAutomatically = true
            }

            loadUrl(url)
        }
    }

    private fun setupFavoriteList(list: Array<Story>?) {
        recyclerView.apply {
            val dividerItemDecoration =
                GridSpacingItemDecoration(3, 12, false)

            flexAdapter.clear()
            removeItemDecorations()
            layoutManager = GridLayoutManager(
                context, 3
            )
            adapter = flexAdapter.apply {
                list?.forEach {
                    addItem(StoryItem(this@StoryActivity, it, this@StoryActivity))
                }
            }
            addItemDecoration(dividerItemDecoration)
        }
    }

    private fun setupStoriesViewPager(list: Array<Story>?, config: UiConfig?, position: Int) {
        pagerAdapter = PagerAdapter(supportFragmentManager)

        list?.forEachIndexed { index, story ->
            addItem(story, config, index)
        }

        viewPager.offscreenPageLimit = 0
        viewPager.adapter = pagerAdapter
        viewPager.setPageTransformer(true, DepthTransformation())
        if (pagerAdapter.count > position) {
            viewPager.currentItem = position
            /* Answers.getInstance().logCustom(CustomEvent("Stories")
                   .putCustomAttribute("Id", intent.extras.getParcelableArrayList<CatalogBlockItem.CatalogStoriesItem>(ARG_DATA)[intent.extras.getString(ARG_POS).toInt()].let { it.title })
                   .putCustomAttribute("Platform", "android"))
*/
        }

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {}
        })
    }

    private fun addItem(item: Story, config: UiConfig?, position: Int) {
        pagerAdapter.addFragments(
            StoryFragment.newInstance(item, config, position)
        )
        pagerAdapter.notifyDataSetChanged()
    }

    private fun getCurrentPosition(): Int {
        return viewPager.currentItem
    }

    private fun canGoNext() =
        if (pagerAdapter.count - 1 > getCurrentPosition())
            viewPager.currentItem++
        else
            finish()

    override fun storyEvent(event: BaseEvent) {
        WistoryCommunication.getInstance().handleEvent(event)

        when (event) {
            is StoryCompleteEvent -> {
                canGoNext()
            }
            is UpdateEvent -> updateStoryValueAtAdapter(event.position, event.story)
            is NavigateEvent -> {
                when (event.type) {
                    "browser" -> startActivity(Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(event.value)
                    })
                    "webView" ->
                        startActivity(Intent(this, StoryActivity::class.java).apply {
                            putExtra(ARG_TYPE, TYPE_WEBVIEW)
                            putExtra(ARG_URL, event.value)
                        })
                }
                Handler().postDelayed({ runOnUiThread { finish() } }, 500)
            }
        }
    }

    private fun updateStoryValueAtAdapter(position: Int?, story: Story) {
        position?.let {
            pagerAdapter.updateFragment(
                position,
                StoryFragment.newInstance(story, config, position)
            )
            pagerAdapter.notifyDataSetChanged()
        }
    }

    override fun onStoryClick(position: Int) {
        val intent = Intent(this, StoryActivity::class.java)
        intent.putExtra(ARG_TYPE, TYPE_STORIES)
        intent.putExtra(ARG_STORIES, list)
        intent.putExtra(ARG_SETTINGS, config)
        intent.putExtra(ARG_POSITION, position)

        startActivity(intent)
    }

    override fun onFavoriteItemClick() {}
}
