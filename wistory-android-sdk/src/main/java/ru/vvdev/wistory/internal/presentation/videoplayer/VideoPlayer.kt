package ru.vvdev.wistory.internal.presentation.videoplayer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.Log
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.video.VideoListener
import ru.vvdev.wistory.R
import ru.vvdev.wistory.Wistory


@Suppress("IMPLICIT_BOXING_IN_IDENTITY_EQUALS")
internal class VideoPlayer(
    private val context: Context,
    private val mPlayerView: PlayerView,
    private val callbacks: VideoPlayerCallBack
) : Player.EventListener, VideoListener {
    companion object {
        const val TAG = "VideoPlayerTAG"
    }

    private var player: SimpleExoPlayer? = null
    private lateinit var mediaDataSourceFactory: DataSource.Factory
    private var currentWindow: Int = 0
    private var playbackPosition: Long = 0

    /*
        init {
            trackSelector = DefaultTrackSelector(context, videoTrackSelectionFactory)
            mediaDataSourceFactory =
                DefaultDataSourceFactory(context, Util.getUserAgent(context, "mediaPlayerSample"))
            trackSelector?.let {
                player = SimpleExoPlayer.Builder(context).setTrackSelector(it).build()
            }
        }

        */
    private fun updateStartPosition() {
        log("updateStartPosition")
        player?.apply {
            playbackPosition = currentPosition
            currentWindow = currentWindowIndex
            playWhenReady = playWhenReady
        }
    }

    fun initializePlayer(uri: String) {
        mediaDataSourceFactory =
            DefaultDataSourceFactory(
                context,
                Util.getUserAgent(context, Wistory::class.java.simpleName)
            )


        val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory)
            .createMediaSource(MediaItem.fromUri(uri))
        player = SimpleExoPlayer.Builder(context).build()

        log("initializePlayer")

        player?.apply {
            setMediaSource(mediaSource)
            prepare()
            playWhenReady = true
            addListener(this@VideoPlayer)
        }
        mPlayerView.setShutterBackgroundColor(Color.TRANSPARENT)
        mPlayerView.player = player
        mPlayerView.requestFocus()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun releasePlayer() {
        log("releasePlayer")
        updateStartPosition()
        player?.clearVideoSurface()
        player?.release()
        mPlayerView.foreground = context.resources.getDrawable(R.color.wistory_black)
    }

    fun isPlaying(): Boolean {
        return (player != null &&
                player?.playbackState !== Player.STATE_ENDED &&
                player?.playbackState !== Player.STATE_IDLE &&
                player?.playWhenReady == true)
    }

    fun pause() {
        log("pause")
        player?.playWhenReady = false
    }

    fun destroy() {
        log("destroy")
        player?.playWhenReady = false
        player?.stop()
        player?.seekTo(0)
        player?.release()
    }

    fun play() {
        log("play")
        player?.playWhenReady = true
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {

        when (playbackState) {
            Player.STATE_READY -> {
                mPlayerView.foreground = null
                callbacks.videoReady(playWhenReady)
            }
            Player.STATE_IDLE -> {
                callbacks.videoIdle(playWhenReady)
            }
            Player.STATE_BUFFERING -> {
                callbacks.videoBuffering(playWhenReady)
            }
            Player.STATE_ENDED -> {
                callbacks.videoEnd(playWhenReady)
            }
            else -> {
            }
        }
        super.onPlayerStateChanged(playWhenReady, playbackState)
    }

    fun seekTo(time: Long) {
        player?.seekTo(time)
    }

    interface VideoPlayerCallBack {
        fun videoBuffering(playWhenReady: Boolean) {}
        fun videoEnd(playWhenReady: Boolean) {}
        fun videoIdle(playWhenReady: Boolean) {}
        fun videoReady(playWhenReady: Boolean) {}
    }

    private fun log(massage: String) {
        Log.d(TAG, massage)
    }
}