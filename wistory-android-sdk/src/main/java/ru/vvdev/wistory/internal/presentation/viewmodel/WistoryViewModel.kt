package ru.vvdev.wistory.internal.presentation.viewmodel

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

internal class WistoryViewModel(private val storiesRepository: StoriesRepository) : ViewModel() {

    val storyItems = MutableLiveData<ArrayList<Story>>()
    val favoriteStoryItems = MutableLiveData<ArrayList<Story>>()
    var errorLiveData = MutableLiveData<Exception>()
    var updateLiveData = MediatorLiveData<ArrayList<UpdateEvent>>()

    var updateList = arrayListOf<UpdateEvent>()

    init {
        favoriteStoryItems.value = arrayListOf()
    }

    fun register() {
        viewModelScope.launch {
            try {
                when (storiesRepository.register()) {
                    is RegisterResponse -> storyItems.value.run { getItems() }
                }
            } catch (e: Exception) {
                errorLiveData.value = e
                storyItems.value.run { getItems() }
            }
        }
    }

    private fun getItems() {
        viewModelScope.launch {
            try {
                val list = storiesRepository.getStories()
                list?.let {
                    storyItems.value = list
                    getFavoriteItems()
                }
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
}
