package ru.vvdev.wistory

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import ru.vvdev.wistory.internal.presentation.callback.StoryEventListener
import ru.vvdev.wistory.internal.presentation.callback.WistoryCommunication
import ru.vvdev.wistory.internal.presentation.ui.WistorySoaringFragment


class Stories(mContext: Context, val eventId: Int) : View(mContext) {

    var eventListener: StoryEventListener? = null
    var serverUrl: String? = null
    var token: String? = null
    var config: UiConfig? = UiConfig()
    val registrationId: String? = null

    /**
     * @see isOpenFromUnreadStory - если true открывает истории с непросмотренной, false - открывает истории сначала
     * */
    var isOpenFromUnreadStory: Boolean = false

    private fun initStory(
        eventListener: StoryEventListener? = null,
        serverUrl: String? = null,
        token: String? = null,
        config: UiConfig? = UiConfig(),
        registrationId: String? = null,
        isOpenFromUnreadStory: Boolean
    ) {
        try {
            eventListener?.let {
                WistoryCommunication.getInstance().removeCallbackListener(it)
                WistoryCommunication.getInstance().addCallBackListener(it)
            }

            routeToWistorySoaringFragment(
                eventId,
                serverUrl,
                token,
                config,
                registrationId,
                isOpenFromUnreadStory
            )

        } catch (e: Exception) {
        }
    }

    private fun routeToWistorySoaringFragment(
        eventId: Int,
        serverUrl: String?,
        token: String?,
        config: UiConfig?,
        registrationId: String?,
        isOpenFromUnreadStory: Boolean
    ) {
        getFragmentManager(context)?.beginTransaction()
            ?.addToBackStack(null)
            ?.add(
                android.R.id.content,
                WistorySoaringFragment.newInstance(
                    token,
                    serverUrl,
                    registrationId,
                    config,
                    eventId,
                    isOpenFromUnreadStory
                )
            )?.commit()
    }

    private fun getFragmentManager(context: Context?): FragmentManager? {
        return when (context) {
            is AppCompatActivity -> context.supportFragmentManager
            is ContextThemeWrapper -> getFragmentManager(context.baseContext)
            else -> null
        }
    }

    operator fun invoke(block: Stories.() -> Unit): Stories = apply(block).apply {
        initStory(eventListener, serverUrl, token, config, registrationId, isOpenFromUnreadStory)
    }
}
