package ru.vvdev.wistory.internal.domain.events

import ru.vvdev.wistory.internal.data.models.Story

internal class StoryNextEvent(val story: Story) : BaseEvent
