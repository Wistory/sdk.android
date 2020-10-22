package ru.vvdev.wistory.internal.domain.events

internal class VoteEvent(val storyId: String, val newpoll: String, val sheet: Int) : BaseEvent
