package ru.vvdev.wistory.internal.domain.events

internal class ReadStoryEvent(val storyId: String, val snapHash: Int) : BaseEvent
