package ru.vvdev.wistory.internal.data.models

import java.io.Serializable
import ru.vvdev.wistory.UiConfig

internal class SnapModel(
    val textBlock: TextModel?,
    var statusbar: ProgressbarModel?,
    val image: String?,
    val video: String?,
    val gif: String?,
    var enableGradient: Boolean,
    val button: ButtonModel?,
    val duration: String,
    private var theme: String,
    var vote: StoryVotingModel? = null
) : Serializable {

    var themeConfig: UiConfig.Theme
        get() {
            return if (theme == "dark")
                UiConfig.Theme.DARK
            else
                UiConfig.Theme.LIGHT
        }
        set(value) {
            theme = if (value == UiConfig.Theme.DARK)
                "dark"
            else
                "light"
        }


    fun getContentResource() = when {
        !video.isNullOrBlank() -> video
        !gif.isNullOrBlank() -> gif
        else -> image
    }
}