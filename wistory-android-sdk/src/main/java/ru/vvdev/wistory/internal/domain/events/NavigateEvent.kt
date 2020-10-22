package ru.vvdev.wistory.internal.domain.events

internal class NavigateEvent(val type: String, val value: String) : BaseEvent {
    var text: String = ""

    constructor(type: String, value: String, title: String) : this(type, value) {
        text = title
    }
}
