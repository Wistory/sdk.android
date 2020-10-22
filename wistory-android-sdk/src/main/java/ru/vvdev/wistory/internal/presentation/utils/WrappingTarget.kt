package ru.vvdev.wistory.internal.presentation.utils

import android.graphics.drawable.Drawable
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition

open class WrappingTarget<Z>(val wrappedTarget: Target<in Z>) : Target<Z> {
    override fun onLoadStarted(placeholder: Drawable?) {
        wrappedTarget.onLoadStarted(placeholder)
    }

    override fun onLoadFailed(errorDrawable: Drawable?) {
        wrappedTarget.onLoadFailed(errorDrawable)
    }

    override fun onLoadCleared(placeholder: Drawable?) {
        wrappedTarget.onLoadCleared(placeholder)
    }

    override fun setRequest(request: Request?) {
        wrappedTarget.request = request
    }

    override fun removeCallback(cb: SizeReadyCallback) {
    }

    override fun getSize(cb: SizeReadyCallback) {
        wrappedTarget.getSize(cb)
    }

    override fun onResourceReady(resource: Z, transition: Transition<in Z>?) {
        wrappedTarget.onResourceReady(resource, transition as Transition<Any?>)
    }

    override fun getRequest(): Request? {
        return wrappedTarget.request
    }

    override fun onStart() {
        wrappedTarget.onStart()
    }

    override fun onStop() {
        wrappedTarget.onStop()
    }

    override fun onDestroy() {
        wrappedTarget.onDestroy()
    }
}
