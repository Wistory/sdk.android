package ru.vvdev.wistory.internal.data.models

import java.io.Serializable
import ru.vvdev.wistory.UiConfig

internal data class ProgressbarModel(
    private var alignment: String? = null,
    var color: String
) : Serializable {

    var alignmentConfig: UiConfig.VerticalAlignment
        get() {
            return when (alignment) {
                "bottom" -> UiConfig.VerticalAlignment.BOTTOM
                else -> UiConfig.VerticalAlignment.TOP
            }
        }
        set(value) {
            alignment = if (value == UiConfig.VerticalAlignment.BOTTOM)
                "bottom"
            else
                "top"
        }
}
