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

    fun getContentResource() = when {
        !video.isNullOrBlank() -> video
        !gif.isNullOrBlank() -> gif
        else -> image
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SnapModel

        if (textBlock != other.textBlock) return false
        if (statusbar != other.statusbar) return false
        if (image != other.image) return false
        if (video != other.video) return false
        if (gif != other.gif) return false
        if (enableGradient != other.enableGradient) return false
        if (button != other.button) return false
        if (duration != other.duration) return false
        if (theme != other.theme) return false
        if (soundVideo != other.soundVideo) return false
        if (vote != other.vote) return false

        return true
    }

    override fun hashCode(): Int {
        var result = textBlock?.hashCode() ?: 0
        result = 31 * result + (statusbar?.hashCode() ?: 0)
        result = 31 * result + (image?.hashCode() ?: 0)
        result = 31 * result + (video?.hashCode() ?: 0)
        result = 31 * result + (gif?.hashCode() ?: 0)
        result = 31 * result + enableGradient.hashCode()
        result = 31 * result + (button?.hashCode() ?: 0)
        result = 31 * result + duration.hashCode()
        result = 31 * result + theme.hashCode()
        result = 31 * result + (soundVideo?.hashCode() ?: 0)
        result = 31 * result + (vote?.hashCode() ?: 0)
        return result
    }

}
