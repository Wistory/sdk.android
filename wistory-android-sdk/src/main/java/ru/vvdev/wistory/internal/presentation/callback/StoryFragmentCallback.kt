package ru.vvdev.wistory.internal.presentation.callback

import ru.vvdev.wistory.internal.domain.events.BaseEvent

interface StoryFragmentCallback {
    fun storyEvent(event: BaseEvent)
}
