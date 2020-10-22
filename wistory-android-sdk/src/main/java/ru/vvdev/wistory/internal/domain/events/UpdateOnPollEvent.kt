package ru.vvdev.wistory.internal.domain.events

import ru.vvdev.wistory.internal.data.models.Story

internal class UpdateOnPollEvent(override val story: Story) : UpdateEvent(story)
