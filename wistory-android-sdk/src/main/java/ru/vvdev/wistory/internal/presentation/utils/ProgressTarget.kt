package ru.vvdev.wistory.internal.presentation.utils

import android.graphics.drawable.Drawable
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition

abstract class ProgressTarget<T, Z>(private var model: T?, target: Target<Z>) :
    WrappingTarget<Z>(target), OkHttpProgressGlideModule.UIProgressListener {
    private var ignoreProgress = true

    override val granualityPercentage: Float
        get() = 1.0f

    constructor(target: Target<Z>) : this(null, target) {}

    fun getModel(): T? {
        return model
    }

    fun setModel(model: T) {
        this.model = model
    }

    /**
     * Convert a model into an Url string that is used to match up the OkHttp requests. For explicit
     * [GlideUrl][com.bumptech.glide.load.model.GlideUrl] loads this needs to return
     * [toStringUrl][com.bumptech.glide.load.model.GlideUrl.toStringUrl]. For custom models do the same as your
     * [BaseGlideUrlLoader][com.bumptech.glide.load.model.stream.BaseGlideUrlLoader] does.
     * @param model return the representation of the given model, DO NOT use [.getModel] inside this method.
     * @return a stable Url representation of the model, otherwise the progress reporting won't work
     */
    protected fun toUrlString(model: T?): String {
        return model.toString()
    }

    override fun onProgress(bytesRead: Long, expectedLength: Long) {
        if (ignoreProgress) {
            return
        }
        if (expectedLength == java.lang.Long.MAX_VALUE) {
            onConnecting()
        } else if (bytesRead == expectedLength) {
            onDownloaded()
        } else {
            onDownloading(bytesRead, expectedLength)
        }
    }

    /**
     * Called when the Glide load has started.
     * At this time it is not known if the Glide will even go and use the network to fetch the image.
     */
    protected abstract fun onConnecting()

    /**
     * Called when there's any progress on the download; not called when loading from cache.
     * At this time we know how many bytes have been transferred through the wire.
     */
    protected abstract fun onDownloading(bytesRead: Long, expectedLength: Long)

    /**
     * Called when the bytes downloaded reach the length reported by the server; not called when loading from cache.
     * At this time it is fairly certain, that Glide either finished reading the stream.
     * This means that the image was either already decoded or saved the network stream to cache.
     * In the latter case there's more work to do: decode the image from cache and transform.
     * These cannot be listened to for progress so it's unsure how fast they'll be, best to show indeterminate progress.
     */
    protected abstract fun onDownloaded()

    /**
     * Called when the Glide load has finished either by successfully loading the image or failing to load or cancelled.
     * In any case the best is to hide/reset any progress displays.
     */
    protected abstract fun onDelivered()

    private fun start() {
        OkHttpProgressGlideModule.expect(toUrlString(model), this)
        ignoreProgress = false
        onProgress(0, java.lang.Long.MAX_VALUE)
    }

    private fun cleanup() {
        ignoreProgress = true
        val model = this.model // save in case it gets modified
        OkHttpProgressGlideModule.forget(toUrlString(model))
        this.model = null
    }

    override fun onLoadStarted(placeholder: Drawable?) {
        start()
        super.onLoadStarted(placeholder)
    }

    override fun onResourceReady(resource: Z, transition: Transition<in Z>?) {
        cleanup()
        onDelivered()
        super.onResourceReady(resource, transition)
    }

    override fun onLoadFailed(errorDrawable: Drawable?) {
        cleanup()
        super.onLoadFailed(errorDrawable)
    }

    override fun onLoadCleared(placeholder: Drawable?) {
        cleanup()
        super.onLoadCleared(placeholder)
    }
}
