package ru.vvdev.wistory.internal.domain.events

import ru.vvdev.wistory.internal.data.models.Story

internal class ReadStoryEvent(val story: Story) : BaseEvent
