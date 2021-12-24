package ru.vvdev.wistory.internal.data.models

import java.io.Serializable
import ru.vvdev.wistory.UiConfig

internal data class ButtonModel(
    private var alignment: String,
    var color: String?,
    val textColor: String?,
    val text: String,
    val action: String,
    val value: String?,
    val valueUrl: String
) : Serializable {

    var alignmentConfig: UiConfig.HorizontalAlignment
        get() {
            return when (alignment) {
                "left" -> UiConfig.HorizontalAlignment.LEFT
                "right" -> UiConfig.HorizontalAlignment.RIGHT
                "fullScreen" -> UiConfig.HorizontalAlignment.FULL_SCREEN
                else -> UiConfig.HorizontalAlignment.CENTER
            }
        }
        set(value) {
            alignment = when (value) {
                UiConfig.HorizontalAlignment.LEFT -> "left"
                UiConfig.HorizontalAlignment.RIGHT -> "right"
                UiConfig.HorizontalAlignment.FULL_SCREEN -> "fullScreen"
                else -> "center"
            }
        }
}
