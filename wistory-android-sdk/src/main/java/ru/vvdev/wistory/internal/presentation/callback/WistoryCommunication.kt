package ru.vvdev.wistory.internal.presentation.callback

import ru.vvdev.wistory.internal.domain.events.*
import ru.vvdev.wistory.internal.domain.events.FavoriteEvent
import ru.vvdev.wistory.internal.domain.events.NavigateEvent
import ru.vvdev.wistory.internal.domain.events.ReadStoryEvent
import ru.vvdev.wistory.internal.domain.events.RelationEvent
import ru.vvdev.wistory.internal.domain.events.StoryNextEvent
import ru.vvdev.wistory.internal.domain.events.VoteEvent

internal class WistoryCommunication private constructor() {
    private var callbacks: ArrayList<StoryEventListener> = arrayListOf()

    fun addCallBackListener(callback: StoryEventListener) {
        callbacks.add(callback)
    }

    fun removeAllCallbackListeners() {
        callbacks.clear()
    }

    fun removeCallbackListener(callback: StoryEventListener) {
        callbacks.remove(callback)
    }

    fun handleEvent(event: BaseEvent) {
        callbacks.forEach { mainCallback ->
            when (event) {
                is ReadStoryEvent -> {
                    mainCallback.onRead(event.storyId, event.snapHash)
                }
                is VoteEvent -> {
                    mainCallback.onPoll(event.storyId, event.sheet, event.newpoll)
                }
                is NavigateEvent -> {
                    mainCallback.onNavigate(event.type, event.value)
                }
                is RelationEvent -> {
                    mainCallback.onRelation(event.story._id, event.value)
                }
                is FavoriteEvent -> {
                    mainCallback.onFavorite(event.story._id, event.value)
                }
                is StoryNextEvent -> {
                    mainCallback.onNextSnap(event.story._id)
                }
                is ErrorEvent -> {
                    mainCallback.onError(event.e)
                }
            }
        }
    }

    companion object {
        private var instance: WistoryCommunication? = null

        fun getInstance(): WistoryCommunication {
            if (instance == null) {
                instance = WistoryCommunication()
            }
            return instance as WistoryCommunication
        }
    }
}
