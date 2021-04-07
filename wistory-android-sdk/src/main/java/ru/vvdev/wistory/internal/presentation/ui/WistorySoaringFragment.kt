package ru.vvdev.wistory.internal.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import ru.vvdev.wistory.R
import ru.vvdev.wistory.ServerConfig
import ru.vvdev.wistory.UiConfig
import ru.vvdev.wistory.internal.data.network.StoriesApi
import ru.vvdev.wistory.internal.data.repository.StoriesRepository
import ru.vvdev.wistory.internal.domain.events.UpdateOnFavoriteEvent
import ru.vvdev.wistory.internal.domain.events.UpdateOnPollEvent
import ru.vvdev.wistory.internal.domain.events.UpdateOnReadEvent
import ru.vvdev.wistory.internal.domain.events.UpdateOnRelationEvent
import ru.vvdev.wistory.internal.presentation.callback.WistoryCommunication
import ru.vvdev.wistory.internal.presentation.viewmodel.WistoryViewModel

internal class WistorySoaringFragment : AbstractWistoryFragment() {

    companion object {
        private const val EVENT_ID = "EVENT_ID"

        fun newInstance(
            token: String?,
            serverUrl: String?,
            registrationId: String?,
            config: UiConfig?,
            eventId: Int
        ): WistorySoaringFragment {
            val args = Bundle()
            args.putString(TOKEN, token)
            args.putSerializable(CONFIG, config)
            args.putString(SERVER_URL, serverUrl)
            args.putString(REGISTRATION_ID, registrationId)
            args.putInt(EVENT_ID, eventId)

            val fragment = WistorySoaringFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var eventId: Int? = null

    override fun initWistoryParams(arguments: Bundle?) {
        token = arguments?.getString(TOKEN) ?: ""
        serverUrl = arguments?.getString(SERVER_URL) ?: ""
        registrationId = arguments?.getString(REGISTRATION_ID)
        config = arguments?.getSerializable(CONFIG) as UiConfig
        eventId = arguments.getInt(EVENT_ID)
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
        initListener()
        initApiService()

        viewModel = ViewModelProviders.of(this, ViewModelFactory(StoriesRepository()))
            .get(WistoryViewModel::class.java)

        eventId?.apply { viewModel.register(this) }

        viewModel.storyItems.sub { list ->
            list?.let {
                navigateToStory(0)
                fragmentManager?.popBackStack()
            }
        }

        viewModel.errorLiveData.sub {
            it?.let {
                postException(it)
                viewModel.errorLiveData.value = null
            }
        }
    }


    override fun onPoll(storyId: String, sheet: Int, newpoll: String?, oldpoll: String?) {
        viewModel.onPoll(storyId, sheet, newpoll)
    }
}