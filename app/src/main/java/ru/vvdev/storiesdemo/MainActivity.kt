package ru.vvdev.storiesdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import java.lang.Exception
import kotlinx.android.synthetic.main.activity_main.*
import ru.vvdev.wistory.UiConfig
import ru.vvdev.wistory.Wistory
import ru.vvdev.wistory.internal.presentation.callback.StoryEventListener

class MainActivity : AppCompatActivity(), StoryEventListener {

    private var baseServer: Int = R.string.wistory_dev_url

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        swipeToRefresh.setOnRefreshListener {
            applyStories()
        }

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

        btnApply.setOnClickListener {
            applyStories()
        }
        btnShow.setOnClickListener {
            Wistory.getStoryView(this).initStory(
                token = etToken.text.takeIf { !it.isNullOrEmpty() }?.let {
                    it.toString()
                } ?: let {
                    null
                },
                serverUrl = getString(baseServer),
                config = UiConfig().apply { format = UiConfig.Format.FULLSCREEN },
                eventListener = this@MainActivity,
                eventId = etEventId.text.takeIf { !it.isNullOrEmpty() }?.let {
                    it.toString().toInt()
                } ?: let {
                    null
                }
            )
        }

        applyStories()
    }

    private fun applyStories() {

        storiesView {
            etToken.text.takeIf { !it.isNullOrEmpty() }?.let {
                token = it.toString()
            } ?: let {
                token = null
            }
            serverUrl = getString(baseServer)
            config {
                format = UiConfig.Format.FIXED
            }
            eventListener = this@MainActivity
        }

        swipeToRefresh.isRefreshing = false
    }

    override fun onError(e: Exception) {
        super.onError(e)
        Snackbar.make(swipeToRefresh, e.localizedMessage, LENGTH_SHORT).show()
    }
}
