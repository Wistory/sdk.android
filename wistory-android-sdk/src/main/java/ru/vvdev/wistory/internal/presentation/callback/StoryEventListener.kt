package ru.vvdev.wistory.internal.presentation.callback

import java.lang.Exception

interface StoryEventListener {
    fun onRead(storyId: String, snapHash: Int) {}
    fun onPoll(storyId: String, sheet: Int, newpoll: String? = null, oldpoll: String? = null) {}
    fun onItemsLoaded() {}
    fun onRelation(storyId: String, relation: String) {}
    fun onFavorite(storyId: String, favorite: Boolean) {}
    fun onNextSnap(storyId: String) {}
    fun onNavigate(action: String, value: String) {}
    fun onPreviousSnap(storyIn: String) {}
    fun onError(e: Exception) {}
}
