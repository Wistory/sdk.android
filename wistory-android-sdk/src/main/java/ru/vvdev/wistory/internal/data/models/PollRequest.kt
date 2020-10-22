package ru.vvdev.wistory.internal.data.models

internal data class PollRequest(
    val storyId: String,
    val sheet: Int,
    val newpoll: String? = null,
    val oldpoll: String? = null
)
