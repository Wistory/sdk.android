package ru.vvdev.storiesdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import java.lang.Exception
import kotlinx.android.synthetic.main.activity_main.*
import ru.vvdev.wistory.UiConfig
import ru.vvdev.wistory.internal.presentation.callback.StoryEventListener

class MainActivity : AppCompatActivity(), StoryEventListener {

    private var baseServer: Int = R.string.wistory_dev_url
    private var isAutoOpenUnreadStory: Boolean = false
    private val ffStorage by lazy { FFStorage(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        swipeToRefresh.setOnRefreshListener {
            applyStories()
        }

        etToken.setText(ffStorage.getToken() ?: "")

        swFullscreen.setOnCheckedChangeListener { _, isChecked ->
            storiesView.config.format =
                if (isChecked) UiConfig.Format.FULLSCREEN else UiConfig.Format.FIXED
        }

        swServer.setOnCheckedChangeListener { _, isChecked ->
            baseServer = if (isChecked) R.string.wistory_dev_url else R.string.wistory_base_url
            applyStories()
        }

        swipeToRefresh.setOnRefreshListener {
            applyStories()
        }

        swAutoOpen.isChecked = ffStorage.getIsAutoOpenUnread()
        swAutoOpen.setOnCheckedChangeListener { _, isChecked ->
            isAutoOpenUnreadStory = isChecked
            ffStorage.setIsAutoOpenUnread(isChecked)
        }

        btnApply.setOnClickListener {
            ffStorage.saveToken(etToken.text.toString())
            applyStories()
        }

        btnShow.setOnClickListener {
            supportFragmentManager.beginTransaction().add(R.id.container, StoryEventFragment())
                .commit()

            /*    val view = Wistory.singleStory(this)
                 view {
                     token = etToken.text.takeIf { !it.isNullOrEmpty() }?.let {
                         it.toString()
                     } ?: let {
                         null
                     }
                     serverUrl = getString(baseServer)
                     config = UiConfig().apply { format = UiConfig.Format.FULLSCREEN }
                     eventListener = this@MainActivity
                     eventId = etEventId.text.takeIf { !it.isNullOrEmpty() }?.let {
                         it.toString().toInt()
                     } ?: let {
                         null
                     }

                 }*/
        }

        applyStories()
    }

    private fun applyStories() {
        storiesView {
            ffStorage.getToken().takeIf { !it.isNullOrEmpty() }?.let {
                token = it
            } ?: let {
                token = null
            }
            serverUrl = getString(baseServer)
            config {
                format = UiConfig.Format.FIXED
                storyTitleState = UiConfig.StoryTitleState.VISIBLE
            }
            eventListener = this@MainActivity
            isAutoOpenUnreadStory = true
        }
        swipeToRefresh.isRefreshing = false
    }

    override fun onError(e: Exception) {
        super.onError(e)
        Snackbar.make(swipeToRefresh, e.localizedMessage, LENGTH_SHORT).show()
    }
}
