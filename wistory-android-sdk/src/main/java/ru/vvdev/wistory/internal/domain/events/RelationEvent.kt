package ru.vvdev.wistory.internal.domain.events

import ru.vvdev.wistory.internal.data.models.Story

internal class RelationEvent(override val story: Story, val value: String, override val position: Int) : UpdateEvent(story, position)
