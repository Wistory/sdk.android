package ru.vvdev.wistory

import java.io.Serializable

class UiConfig : Serializable {
    enum class Format : Serializable { FULLSCREEN, FIXED }
    enum class StoryTitleState : Serializable { VISIBLE, GONE }
    enum class Theme : Serializable { LIGHT, DARK, UNDEFINED }
    enum class HorizontalAlignment : Serializable { LEFT, RIGHT, CENTER, FULL_SCREEN }
    enum class VerticalAlignment : Serializable { TOP, CENTER, BOTTOM }

    var format: Format = Format.FIXED
        set
    var statusBarPosition: VerticalAlignment? = null
        set

    var storyTitleState: StoryTitleState = StoryTitleState.VISIBLE

    operator fun invoke(block: UiConfig.() -> Unit): UiConfig = apply(block)
}
