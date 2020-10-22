package ru.vvdev.wistory.internal.domain.events

data class ErrorEvent(var e: Exception) : BaseEvent
