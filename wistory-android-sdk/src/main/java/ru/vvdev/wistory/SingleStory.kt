package ru.vvdev.wistory

import android.content.Context
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import ru.vvdev.wistory.internal.presentation.callback.StoryEventListener
import ru.vvdev.wistory.internal.presentation.callback.WistoryCommunication
import ru.vvdev.wistory.internal.presentation.ui.WistorySoaringFragment

class SingleStory(mContext: Context) : View(mContext) {

    var eventId: Int? = null
    var eventListener: StoryEventListener? = null
    var serverUrl: String? = null
    var token: String? = null
    var config: UiConfig? = UiConfig()
    val registrationId: String? = null

    private fun initStory(
        eventId: Int? = null,
        eventListener: StoryEventListener? = null,
        serverUrl: String? = null,
        token: String? = null,
        config: UiConfig? = UiConfig(),
        registrationId: String? = null
    ) {
        try {
            eventListener?.let {
                WistoryCommunication.getInstance().removeCallbackListener(it)
                WistoryCommunication.getInstance().addCallBackListener(it)
            }

            eventId?.apply {
                Log.d("second", "ed")
                getFragmentManager(context)?.beginTransaction()
                    ?.addToBackStack(null)
                    ?.add(
                        android.R.id.content,
                        WistorySoaringFragment.newInstance(
                            token,
                            serverUrl,
                            registrationId,
                            config,
                            this
                        )
                    )?.commit()
            }
        } catch (e: Exception) {
        }
    }

    private fun getFragmentManager(context: Context?): FragmentManager? {
        return when (context) {
            is AppCompatActivity -> context.supportFragmentManager
            is ContextThemeWrapper -> getFragmentManager(context.baseContext)
            else -> null
        }
    }

    operator fun invoke(block: SingleStory.() -> Unit): SingleStory = apply(block).apply {
        initStory(eventId, eventListener, serverUrl, token, config, registrationId)
    }
}
