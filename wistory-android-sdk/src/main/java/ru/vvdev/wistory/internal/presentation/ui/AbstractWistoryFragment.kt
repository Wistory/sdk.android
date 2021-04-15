package ru.vvdev.wistory.internal.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import ru.vvdev.wistory.ServerConfig
import ru.vvdev.wistory.UiConfig
import ru.vvdev.wistory.internal.data.network.StoriesApi
import ru.vvdev.wistory.internal.data.repository.StoriesRepository
import ru.vvdev.wistory.internal.domain.events.*
import ru.vvdev.wistory.internal.presentation.callback.StoryEventListener
import ru.vvdev.wistory.internal.presentation.callback.WistoryCommunication
import ru.vvdev.wistory.internal.presentation.viewmodel.WistoryViewModel

internal abstract class AbstractWistoryFragment : Fragment(), StoryEventListener {

    companion object {
        internal const val TOKEN = "token"
        internal const val CONFIG = "config"
        internal const val SERVER_URL = "serverUrl"
        internal const val REGISTRATION_ID = "registrationId"
    }

    internal var config: UiConfig? = null
    internal var token: String? = null
    internal var serverUrl: String? = null
    internal var registrationId: String? = null
    internal var eventId: Int? = null

    internal lateinit var viewModel: WistoryViewModel

    abstract fun currentFragment(): Fragment
    abstract fun initWistoryParams(arguments: Bundle?)

    internal fun navigateToStory(position: Int) {
        requireActivity().startActivity(Intent(activity, StoryActivity::class.java).apply {
            putExtra(StoryActivity.ARG_TYPE, StoryActivity.TYPE_STORIES)
            putExtra(StoryActivity.ARG_STORIES, viewModel.storyItems.value?.toTypedArray())
            putExtra(StoryActivity.ARG_POSITION, position)
            putExtra(StoryActivity.ARG_SETTINGS, config)
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
        initApiService()
        viewModel = ViewModelProviders.of(currentFragment(), ViewModelFactory(StoriesRepository()))
            .get(WistoryViewModel::class.java)
        viewModel.register(eventId)
        viewModel.errorLiveData.sub {
            it?.let {
                postException(it)
                viewModel.errorLiveData.value = null
            }
        }
    }

    private fun initApiService() {
        try {
            StoriesApi.createService(
                requireContext(),
                ServerConfig(token, serverUrl, registrationId)
            )
        } catch (e: Exception) {
            postException(e)
        }
    }

    private fun initListener() {
        WistoryCommunication.getInstance().removeCallbackListener(this)
        WistoryCommunication.getInstance().addCallBackListener(this)
    }

    private fun postException(e: Exception?) = e?.let {
        WistoryCommunication.getInstance().handleEvent(ErrorEvent(e))
    }

    internal fun <T> LiveData<T>.sub(func: (T?) -> Unit) {
        observe(viewLifecycleOwner, Observer { T -> func.invoke(T) })
    }
}
