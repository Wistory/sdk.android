package ru.vvdev.wistory.internal.domain.events

import ru.vvdev.wistory.internal.data.models.Story

internal open class UpdateEvent(open val story: Story, open val position: Int? = null) : BaseEvent
