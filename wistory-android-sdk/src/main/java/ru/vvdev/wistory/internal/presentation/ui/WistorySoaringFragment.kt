package ru.vvdev.wistory.internal.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.vvdev.wistory.UiConfig

internal class WistorySoaringFragment : AbstractWistoryFragment() {

    override fun currentFragment(): Fragment = this

    override fun initWistoryParams(arguments: Bundle?) {
        token = arguments?.getString(TOKEN) ?: ""
        serverUrl = arguments?.getString(SERVER_URL) ?: ""
        registrationId = arguments?.getString(REGISTRATION_ID)
        config = arguments?.getSerializable(CONFIG) as UiConfig
        eventId = arguments.getInt(EVENT_ID)
        isOpenFromUnreadStory = arguments.getBoolean(IS_OPEN_FROM_UNREAD_STORY)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        initWistoryParams(arguments)
        return View(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.mStoryItems.sub { list ->
            list?.let {
                navigateToStory(0)
                parentFragmentManager.popBackStack()
            }
        }
    }

    override fun onPoll(storyId: String, sheet: Int, newpoll: String?, oldpoll: String?) {
        viewModel.onPoll(storyId, sheet, newpoll)
    }
    companion object {
        private const val EVENT_ID = "EVENT_ID"

        fun newInstance(
            token: String?,
            serverUrl: String?,
            registrationId: String?,
            config: UiConfig?,
            eventId: Int,
            isOpenFromUnreadStory: Boolean
        ): WistorySoaringFragment {
            val args = Bundle()
            args.putString(TOKEN, token)
            args.putSerializable(CONFIG, config)
            args.putString(SERVER_URL, serverUrl)
            args.putString(REGISTRATION_ID, registrationId)
            args.putInt(EVENT_ID, eventId)
            args.putBoolean(IS_OPEN_FROM_UNREAD_STORY, isOpenFromUnreadStory)

            val fragment = WistorySoaringFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
