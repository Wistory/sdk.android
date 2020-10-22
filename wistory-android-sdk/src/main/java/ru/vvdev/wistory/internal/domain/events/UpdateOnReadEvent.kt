package ru.vvdev.wistory.internal.domain.events

import ru.vvdev.wistory.internal.data.models.Story

internal class UpdateOnReadEvent(override val story: Story) : UpdateEvent(story)
