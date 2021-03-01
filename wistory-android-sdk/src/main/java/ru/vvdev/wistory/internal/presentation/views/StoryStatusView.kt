package ru.vvdev.wistory.internal.presentation.views

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ScaleDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import ru.vvdev.wistory.R

internal class StoryStatusView : LinearLayout {

    private val progressBars = ArrayList<ProgressBar>()
    private val animators = ArrayList<ObjectAnimator>()

    private var storiesCount = -1
    private var current = 0
    private var userInteractionListener: UserInteractionListener? = null
    private var isDark: Boolean = false
    private var isSkip: Boolean = false
    internal var isReverse: Boolean = false
    internal var isComplete: Boolean = false

    interface UserInteractionListener {
        fun onNext()
        fun onPrev()
        fun onComplete()
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private fun bindViews() {
        removeAllViews()
        isComplete = false
        progressBars.clear()
        for (i in 0 until storiesCount) {
            val p = createProgressBar()
            p.max = MAX_PROGRESS
            progressBars.add(p)
            addView(p)
            if (i + 1 < storiesCount) {
                addView(createSpace())
            }
        }
    }

    private fun createProgressBar(): ProgressBar {
        val p = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal)
        p.layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        p.progressDrawable = ContextCompat.getDrawable(
            context,
            if (isDark) R.drawable.wistory_progress_bg_black else R.drawable.wistory_progress_bg_white
        )
        return p
    }

    fun updateProgressBar(color: String) {
        for (i in 0 until childCount) {
            val v: View = getChildAt(i)
            if (v is ProgressBar) {
                v.progressDrawable = createProgressDrawable(color.replace("#", ""))
            }
        }
    }

    private fun createProgressDrawable(color: String): Drawable {
        val bckgrndDr =
            GradientDrawable().apply {
                setColor(Color.parseColor("#10$color"))
                cornerRadius = 8.0f
            }
        val progressDr =
            ScaleDrawable(GradientDrawable().apply {
                setColor(Color.parseColor("#$color"))
                cornerRadius = 8.0f
            }, Gravity.LEFT, 1f, -1f)
        val resultDr = LayerDrawable(arrayOf(bckgrndDr, progressDr))

        resultDr.setId(0, android.R.id.background)
        resultDr.setId(1, android.R.id.progress)

        return resultDr
    }

    private fun createSpace(): View {
        val v = View(context)
        v.layoutParams = LayoutParams(SPACE_BETWEEN_PROGRESS_BARS, LayoutParams.WRAP_CONTENT)
        return v
    }

    fun setUserInteractionListener(userInteractionListener: UserInteractionListener) {
        this.userInteractionListener = userInteractionListener
    }

    fun currentPosition() = current

    fun skip() {
        isSkip = true
        if (isComplete) return
        val p = progressBars[current]
        p.progress = p.max
        animators[current].cancel()
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    fun pause() {
        if (isComplete) return
        if (isSkip) {
            isSkip = false
            return
        }
        val p = progressBars[current]
        p.progress = p.progress
        animators[current].pause()
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    fun resume() {
        if (isComplete) return
        val p = progressBars[current]
        p.progress = p.progress
        animators[current].resume()
    }

    fun reverse() {
        if (isComplete) return
        var p = progressBars[current]
        p.progress = 0
        isReverse = true
        animators[current].cancel()
        if (0 <= current - 1) {
            p = progressBars[current - 1]
            p.progress = 0
            animators[--current].start()
        } else {
            animators[current].start()
        }
    }

    fun setStoriesCountWithDurations(durations: LongArray) {
        storiesCount = durations.size
        bindViews()
        animators.clear()
        for (i in progressBars.indices) {
            animators.add(createAnimator(i, durations[i]))
        }
    }

    fun playStories() {
        animators[0].start()
    }

    /**
     * Need to call when Activity or Fragment destroy
     */
    fun destroy() {
        for (a in animators) {
            a.removeAllListeners()
            a.cancel()
        }
    }

    private fun createAnimator(index: Int, duration: Long): ObjectAnimator {
        val animation = ObjectAnimator.ofInt(progressBars[index], "progress", MAX_PROGRESS)
        animation.interpolator = LinearInterpolator()
        animation.duration = duration
        animation.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                current = index
                if (isReverse) {
                    isReverse = false
                }
                animators[index].pause()
            }

            override fun onAnimationEnd(animation: Animator) {
                if (isReverse) {
                    if (userInteractionListener != null) userInteractionListener!!.onPrev()
                    return
                }
                val next = current + 1
                if (next <= animators.size - 1) {
                    if (userInteractionListener != null) userInteractionListener!!.onNext()
                    animators[next].start()
                } else {
                    isComplete = true
                    if (userInteractionListener != null) userInteractionListener!!.onComplete()
                }
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {
            }
        })
        return animation
    }

    companion object {

        private const val MAX_PROGRESS = 10000
        private const val SPACE_BETWEEN_PROGRESS_BARS = 20
    }
}
