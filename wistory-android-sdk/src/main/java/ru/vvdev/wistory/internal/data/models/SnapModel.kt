package ru.vvdev.wistory.internal.data.models

import java.io.Serializable
import ru.vvdev.wistory.UiConfig

internal class SnapModel(
    val textBlock: TextModel?,
    var statusbar: ProgressbarModel?,
    val image: String,
    val video: String?,
    var enableGradient: Boolean,
    val button: ButtonModel?,
    val duration: String,
    private var theme: String,
    val soundVideo: Boolean?,
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

    fun getContentResource() = takeIf { !video.isNullOrEmpty() }?.let { video }?:let { image }
}