package ru.vvdev.wistory.internal.data.models

import java.io.Serializable
import ru.vvdev.wistory.UiConfig

internal data class TextModel(
    private val alignment: String,
    val color: String?,
    val text: String,
    val subtext: String
) : Serializable {

    var alignmentOverlay: UiConfig.VerticalAlignment =
        UiConfig.VerticalAlignment.CENTER
        get() {
            return when (alignment) {
                "top" -> UiConfig.VerticalAlignment.TOP
                "bottom" -> UiConfig.VerticalAlignment.BOTTOM
                else -> UiConfig.VerticalAlignment.CENTER
            }
        }
}
