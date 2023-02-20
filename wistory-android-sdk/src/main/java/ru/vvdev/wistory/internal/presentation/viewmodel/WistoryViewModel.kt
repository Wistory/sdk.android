package ru.vvdev.wistory.internal.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.vvdev.wistory.internal.data.models.RegisterResponse
import ru.vvdev.wistory.internal.data.models.Story
import ru.vvdev.wistory.internal.data.repository.StoriesRepository
import ru.vvdev.wistory.internal.domain.events.UpdateEvent
import ru.vvdev.wistory.internal.domain.events.UpdateOnFavoriteEvent
import ru.vvdev.wistory.internal.domain.events.UpdateOnPollEvent
import ru.vvdev.wistory.internal.domain.events.UpdateOnReadEvent
import ru.vvdev.wistory.internal.domain.events.UpdateOnRelationEvent
import java.util.UUID

internal class WistoryViewModel(
    private val storiesRepository: StoriesRepository,
    private val isOpenFromUnreadStory: Boolean
) : ViewModel() {

    val mStoryItems = MutableLiveData<ArrayList<Story>>()
    val favoriteStoryItems = MutableLiveData<ArrayList<Story>>()
    var errorLiveData = MutableLiveData<Exception>()
    var updateLiveData = MediatorLiveData<ArrayList<UpdateEvent>>()

    var updateList = arrayListOf<UpdateEvent>()

    init {
        favoriteStoryItems.value = arrayListOf()
    }

    fun register(eventId: Int? = null) {
        viewModelScope.launch {
            try {
                when (storiesRepository.register()) {
                    is RegisterResponse -> mStoryItems.value.run { getItems(eventId) }
                }
            } catch (e: Exception) {
                errorLiveData.value = e
                mStoryItems.value.run { getItems(eventId) }
            }
        }
    }

    private fun getItems(eventId: Int?) {
        viewModelScope.launch {
            try {
                val list: ArrayList<Story>? =
                    if (eventId == null) storiesRepository.getStories()?.apply {
                        mStoryItems.value = this
                        getFavoriteItems()
                    }
                    else storiesRepository.getByEventId(eventId)?.stories
                mStoryItems.value = list
            } catch (e: Exception) {
                errorLiveData.value = e
            }
        }
    }

    private fun getFavoriteItems() {
        viewModelScope.launch {
            try {
                val list = storiesRepository.getFavorites()
                list?.let {
                    favoriteStoryItems.value = list
                }
            } catch (e: Exception) {
                errorLiveData.value = e
            }
        }
    }

    fun onPoll(storyId: String, sheet: Int, newpoll: String?) {
        viewModelScope.launch {
            try {
                val story = storiesRepository.poll(storyId, sheet, newpoll)
                story?.let {
                    postEvent(UpdateOnPollEvent(story))
                }
            } catch (e: Exception) {
                errorLiveData.value = e
            }
        }
    }

    fun onRelation(storyId: String, relation: String) {
        viewModelScope.launch {
            try {
                val story = storiesRepository.setRelation(storyId, relation)
                story?.let {
                    postEvent(UpdateOnRelationEvent(story))
                }
            } catch (e: Exception) {
                errorLiveData.value = e
            }
        }
    }

    fun onRead(storyId: String) {
        viewModelScope.launch {
            try {
                val story = storiesRepository.setRead(storyId)
                story?.let {
                    postEvent(UpdateOnReadEvent(story))
                }
            } catch (e: Exception) {
                errorLiveData.value = e
            }
        }
    }

    private fun postEvent(event: UpdateEvent) {
        updateList.add(event)
        updateLiveData.value = updateList
    }

    fun clearUpdated() {
        updateList.clear()
    }

    fun onFavorite(storyId: String, favorite: Boolean) {
        viewModelScope.launch {
            try {
                val story = storiesRepository.setFavorite(storyId, favorite)
                story?.let {
                    postEvent(UpdateOnFavoriteEvent(story))
                    getFavoriteItems()
                }
            } catch (e: Exception) {
                errorLiveData.value = e
            }
        }
    }

    fun publishEvents() {
        updateLiveData.value = updateList
    }

    fun observeUnreadStoryPosition(): LiveData<Int?> = object : MediatorLiveData<Int>() {
        init {
            addSource(mStoryItems) { items ->
                val indexOfFresh = items.indexOfFirst { it.fresh }
                value = if (!isOpenFromUnreadStory && indexOfFresh == -1) {
                    null
                } else {
                    indexOfFresh
                }
            }
        }
    }
}
