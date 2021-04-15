package ru.vvdev.wistory

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import ru.vvdev.wistory.internal.presentation.callback.StoryEventListener
import ru.vvdev.wistory.internal.presentation.callback.WistoryCommunication
import ru.vvdev.wistory.internal.presentation.ui.WistoryListFragment

class WistoryView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RelativeLayout(context, attrs, defStyle) {

    var eventListener: StoryEventListener? = null
    var serverUrl: String? = null
    var token: String? = null
    var fragmentManager: FragmentManager? = null
    var registrationId: String? = null
    var config: UiConfig = UiConfig()

    init {
        LayoutInflater.from(context).inflate(R.layout.wistory_view, this)
        viewTreeObserver.addOnGlobalLayoutListener { requestLayout() }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        recreate()
    }

    private fun recreate() {
        eventListener?.let {
            WistoryCommunication.getInstance().removeCallbackListener(it)
            WistoryCommunication.getInstance().addCallBackListener(it)
        }
        val fragmentManager: FragmentManager? =
            try {
                FragmentManager.findFragment<Fragment>(this).childFragmentManager
            } catch (e: IllegalStateException) {
                getFragmentManager(context)
            }
        try {
            fragmentManager?.fragments
            fragmentManager?.apply {
                findFragmentByTag(WistoryListFragment::class.java.simpleName)?.let {
                    WistoryCommunication.getInstance().removeCallbackListener(it as WistoryListFragment)
                }
                beginTransaction().replace(
                        R.id.fragmentContainerView,
                        WistoryListFragment.newInstance(
                            token ?: Wistory.token,
                            serverUrl ?: Wistory.serverUrl,
                            registrationId,
                            config
                        ),
                        WistoryListFragment::class.java.simpleName
                    ).commit()
            }
        } catch (e: Exception) {
            e
        }
    }

    private fun getFragmentManager(context: Context?): FragmentManager? {
        return fragmentManager ?: when (context) {
            is FragmentActivity -> context.supportFragmentManager
            is ContextThemeWrapper -> getFragmentManager(context.baseContext)
            else -> null
        }
    }

    override fun requestLayout() {
        super.requestLayout()
        post {
            measure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
            )
            layout(left, top, right, bottom)
        }
    }

    operator fun invoke(block: WistoryView.() -> Unit): WistoryView = apply(block).apply {
        recreate()
    }
}
