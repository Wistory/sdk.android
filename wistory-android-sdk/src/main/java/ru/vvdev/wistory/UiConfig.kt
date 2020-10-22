package ru.vvdev.wistory

import java.io.Serializable

class UiConfig : Serializable {
    enum class Format : Serializable { FULLSCREEN, FIXED }
    enum class Theme : Serializable { LIGHT, DARK, UNDEFINED }
    enum class HorizontalAlignment : Serializable { LEFT, RIGHT, CENTER }
    enum class VerticalAlignment : Serializable { TOP, CENTER, BOTTOM }

    var format: Format = Format.FIXED
        set
    var statusBarPosition: VerticalAlignment? = VerticalAlignment.TOP
        set

    operator fun invoke(block: UiConfig.() -> Unit): UiConfig = apply(block)
}
