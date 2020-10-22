package ru.vvdev.wistory.internal.presentation.views.extentions

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Color.parseColor
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

internal fun ViewGroup.setColor(context: Context, color: Int) {
    val newColorBg = ContextCompat.getColor(context, color)

    var oldColorBg = Color.TRANSPARENT
    val background = background
    if (background is ColorDrawable)
        oldColorBg = background.color

    val valueAnimator = ValueAnimator.ofArgb(oldColorBg, newColorBg)
    valueAnimator.duration = 200
    valueAnimator.interpolator = LinearInterpolator()
    valueAnimator.addUpdateListener { valueAnimator ->
        setBackgroundColor(
            valueAnimator.animatedValue as Int
        )
    }
    valueAnimator.start()
}

internal fun View.avoidDoubleClicks() {
    val delayMillis: Long = 900
    if (!isClickable) {
        return
    }
    isClickable = false
    postDelayed({ isClickable = true }, delayMillis)
}

internal fun String.parseHexColor(): Int {
    var str = this
    if (str.length == 4) { // #XXX
        str = str.replace("#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])".toRegex(), "#$1$1$2$2$3$3")
    }
    return parseColor(str)
}

internal fun <T : RecyclerView> T.removeItemDecorations() {
    while (itemDecorationCount > 0) {
        removeItemDecorationAt(0)
    }
}
