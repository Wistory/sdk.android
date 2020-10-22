package ru.vvdev.wistory.internal.domain.events

import ru.vvdev.wistory.internal.data.models.Story

internal class FavoriteEvent(override val story: Story, val value: Boolean, override val position: Int) : UpdateEvent(story, position)
