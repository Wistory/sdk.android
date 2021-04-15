package ru.vvdev.wistory.internal.presentation.callback

import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat
import kotlin.math.abs
import ru.vvdev.wistory.R

open class StoryTouchListener(context: Context) : View.OnTouchListener {

    companion object {
        private const val SWIPE_DISTANCE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
        private const val MAX_CLICK_DURATION = 500L
    }

    private val gestureDetector: GestureDetectorCompat

    init {
        gestureDetector = GestureDetectorCompat(context, GestureListener())
    }

    open fun onSwipeTop() = false

    open fun onSwipeBottom() = false

    open fun onCLickLeft() = false

    open fun onCLickRight() = false

    open fun onCLickStop() = false

    open fun onResume() = false

    private val resumeHandler = Handler()
    private val stopHandler = Handler()
    private val resumeRunnable = Runnable { onResume() }
    private val stopRunnable = Runnable { onCLickStop() }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        Log.i("TouchEvent", event.toString())
        if (gestureDetector.onTouchEvent(event)) {
            stopHandler.removeCallbacksAndMessages(null)
            if (v.id == R.id.skip) {
                onCLickRight()
            } else if (v.id == R.id.reverse) {
                onCLickLeft()
            }
        } else if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                stopHandler.postDelayed(stopRunnable, 500)
                resumeHandler.removeCallbacksAndMessages(null)
            } else {
                resumeHandler.postDelayed(resumeRunnable, MAX_CLICK_DURATION)
            }

        return true
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            return true
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            var result = false
            try {
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x

                if (abs(diffY) > SWIPE_DISTANCE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (abs(diffX) < 100)
                        result = if (diffY < 0) {
                            onSwipeTop()
                        } else {
                            onSwipeBottom()
                        }
                    else false
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            return !result
        }
    }
}
