package ru.vvdev.wistory.internal.presentation.callback

interface ItemSelected {
    fun itemSelected(storyId: String, newpoll: String, oldpoll: String, sheet: Int)
}
