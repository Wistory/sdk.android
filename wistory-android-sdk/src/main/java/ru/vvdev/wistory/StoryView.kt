package ru.vvdev.wistory

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import ru.vvdev.wistory.internal.presentation.callback.StoryEventListener
import ru.vvdev.wistory.internal.presentation.callback.WistoryCommunication
import ru.vvdev.wistory.internal.presentation.ui.WistorySoaringFragment
import java.lang.reflect.Executable


class StoryView(mContext: Context) : View(mContext) {

    fun initStory(
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
}
