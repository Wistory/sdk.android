package ru.vvdev.wistory.internal.presentation.videoplayer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.Log
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultAllocator
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

    private var trackSelector: DefaultTrackSelector? = null
    private val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory()
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
        val allocator = DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE)
        val loadControl = DefaultLoadControl(allocator, 360000, 600000, 2500, 5000, -1, true)
        trackSelector = DefaultTrackSelector(context, videoTrackSelectionFactory)
        mediaDataSourceFactory =
            DefaultDataSourceFactory(
                context,
                Util.getUserAgent(context, Wistory::class.java.simpleName)
            )
        trackSelector?.let {
            player =
                SimpleExoPlayer.Builder(context).setTrackSelector(it).setLoadControl(loadControl)
                    .build()
        }
        log("initializePlayer")

        val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory)
            .createMediaSource(Uri.parse(uri))

        player?.apply {
            prepare(mediaSource, true, true)
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
        mPlayerView.foreground = context.resources.getDrawable(R.color.wistory_black)
        trackSelector = null
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
        fun videoBuffering(playWhenReady: Boolean)
        fun videoEnd(playWhenReady: Boolean)
        fun videoIdle(playWhenReady: Boolean)
        fun videoReady(playWhenReady: Boolean)
    }

    private fun log(massage: String) {
        Log.d(TAG, massage)
    }
}