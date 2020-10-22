package ru.vvdev.wistory.internal.domain.events

import ru.vvdev.wistory.internal.data.models.Story

internal class UpdateOnFavoriteEvent(override val story: Story) : UpdateEvent(story)
