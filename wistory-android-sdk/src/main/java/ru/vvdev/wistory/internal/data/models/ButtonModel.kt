package ru.vvdev.wistory.internal.data.models

import java.io.Serializable
import ru.vvdev.wistory.UiConfig

internal data class ButtonModel(
    private var alignment: String,
    val color: String?,
    val textColor: String?,
    val text: String,
    val action: String,
    val value: String?,
    val valueUrl: String
) : Serializable {

    companion object {
        const val ALIGNMENT_LEFT = "left"
        const val ALIGNMENT_RIGHT = "right"
        const val ALIGNMENT_CENTER = "center"
        const val ALIGNMENT_FULL_SCREEN = "fullScreen"
    }

    var alignmentConfig: UiConfig.HorizontalAlignment
        get() {
            return when (alignment) {
                ALIGNMENT_LEFT -> UiConfig.HorizontalAlignment.LEFT
                ALIGNMENT_RIGHT -> UiConfig.HorizontalAlignment.RIGHT
                else -> UiConfig.HorizontalAlignment.CENTER
            }
        }
        set(value) {
            alignment = when (value) {
                UiConfig.HorizontalAlignment.LEFT -> ALIGNMENT_LEFT
                UiConfig.HorizontalAlignment.RIGHT -> ALIGNMENT_RIGHT
                else -> ALIGNMENT_CENTER
            }
        }

    fun isFullScreenButton() = alignment == ALIGNMENT_FULL_SCREEN
}
