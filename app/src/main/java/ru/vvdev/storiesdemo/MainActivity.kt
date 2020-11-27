package ru.vvdev.storiesdemo

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import ru.vvdev.wistory.UiConfig
import ru.vvdev.wistory.internal.presentation.callback.StoryEventListener

class MainActivity : AppCompatActivity(), StoryEventListener {

    private var baseServer: Int = R.string.wistory_dev_url

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etToken.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }
        })

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
                statusBarPosition = UiConfig.VerticalAlignment.BOTTOM
            }
            eventListener = this@MainActivity
        }

        swipeToRefresh.isRefreshing = false
    }
}
