package ru.vvdev.wistory.internal.data.models

import ru.vvdev.wistory.UiConfig
import java.io.Serializable

internal data class ButtonModel(
    private var alignment: String,
    var color: String,
    val textColor: String,
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
                else -> UiConfig.HorizontalAlignment.CENTER
            }
        }
        set(value) {
            alignment = when (value) {
                UiConfig.HorizontalAlignment.LEFT -> "left"
                UiConfig.HorizontalAlignment.RIGHT -> "right"
                else -> "center"
            }
        }

    var format: UiConfig.Format
        get() {
            return if (alignment == "fullScreen")
                UiConfig.Format.FULLSCREEN
            else
                UiConfig.Format.FIXED
        }
        set(value) {
            alignment = when (value) {
                UiConfig.Format.FULLSCREEN -> "fullScreen"
                else -> "fixed"
            }
        }
}
