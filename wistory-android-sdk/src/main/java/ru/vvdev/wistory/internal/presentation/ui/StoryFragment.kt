package ru.vvdev.wistory.internal.presentation.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.bumptech.glide.request.target.Target
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.wistory_fragment.*
import ru.vvdev.wistory.R
import ru.vvdev.wistory.UiConfig
import ru.vvdev.wistory.internal.data.models.*
import ru.vvdev.wistory.internal.domain.events.*
import ru.vvdev.wistory.internal.presentation.callback.ItemSelected
import ru.vvdev.wistory.internal.presentation.callback.StoryFragmentCallback
import ru.vvdev.wistory.internal.presentation.callback.StoryTouchListener
import ru.vvdev.wistory.internal.presentation.utils.ProgressTarget
import ru.vvdev.wistory.internal.presentation.videoplayer.VideoPlayer
import ru.vvdev.wistory.internal.presentation.views.StoryStatusView
import ru.vvdev.wistory.internal.presentation.views.extentions.parseHexColor
import ru.vvdev.wistory.internal.presentation.views.extentions.setColor

internal class StoryFragment : Fragment(), StoryStatusView.UserInteractionListener,
    VideoPlayer.VideoPlayerCallBack,
    ItemSelected, SnapCounter {

    private var list = mutableListOf<OptionModel>()
    private var isFragmentEnabled = false
    private var target: ProgressTarget<String, Drawable>? = null
    private var counter = 0
    private var statusResources: MutableList<String> = mutableListOf()
    private lateinit var story: Story
    private lateinit var uiConfig: UiConfig
    private var storyPlayAgain = false
    private var typeStory = TypeStory.IMAGE_TYPE
    private var videoPrepared = false
    private var videoPlayer: VideoPlayer? = null
    private var storyFragmentCallback: StoryFragmentCallback? = null
    private var audioService: AudioManager? = null
    private val options = RequestOptions()
        .skipMemoryCache(false)
        .transform(CenterCrop(), RoundedCorners(16))
        .diskCacheStrategy(DiskCacheStrategy.ALL)

    companion object {
        private const val ARG_STORY = "STORY"
        private const val ARG_STORY_POSITION = "POS"
        private const val ARG_STORY_SETTINGS = "SETTINGS"
        private const val STORY_FIXED_RATIO = "9:14.6"
        private const val STORY_RELATION_LIKE = "like"
        private const val STORY_RELATION_DISLIKE = "dislike"
        private const val STORY_HEADER_LENGTH = 43
        private const val STATUSBAR_VERTICAL_BOTTOM_BIAS = 0.97f
        private const val STATUSBAR_VERTICAL_TOP_BIAS = 0.04f
        private const val statusMargin: Int = 16
        private const val avatarMargin: Int = 24
        private const val closeParentTopMargin: Int = 26
        private const val closeTopMargin: Int = 10
        private const val closeEndMargin: Int = 8
        private const val buttonMargin: Int = 24
        private const val buttonBetaMargin: Int = 96

        fun newInstance(
            story: Story,
            settings: UiConfig? = null,
            pos: Int
        ): StoryFragment {
            val args = Bundle()
            args.putSerializable(ARG_STORY, story)
            args.putSerializable(ARG_STORY_POSITION, pos)
            args.putSerializable(ARG_STORY_SETTINGS, settings)
            val fragment = StoryFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.wistory_fragment, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        audioService = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        close.setOnClickListener {
            requireActivity().finish()
        }

        val touchListener = object : StoryTouchListener(requireContext()) {

            override fun onCLickLeft(): Boolean {
                storiesStatus.reverse()
                return true
            }

            override fun onCLickRight(): Boolean {
                storiesStatus.skip()
                return true
            }

            override fun onCLickStop(): Boolean {
                if (image.drawable != null || videoPlayer?.isPlaying() == true)
                    storyPlayAgain = true
                if (typeStory == TypeStory.VIDEO_TYPE)
                    videoPlayer?.pause()
                storiesStatus.pause()
                return true
            }

            override fun onResume(): Boolean {
                if (storyPlayAgain) {
                    if (typeStory == TypeStory.VIDEO_TYPE)
                        videoPlayer?.play()
                    storiesStatus?.resume()
                    storyPlayAgain = false
                }
                return true
            }

            override fun onSwipeTop(): Boolean {
                requireActivity().finish()
                return true
            }

            override fun onSwipeBottom(): Boolean {
                requireActivity().finish()
                return true
            }
        }

        reverse.setOnTouchListener(touchListener)
        skip.setOnTouchListener(touchListener)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is StoryFragmentCallback) {
            storyFragmentCallback = activity as StoryFragmentCallback
        }

        if (isFragmentEnabled)
            readStory()
    }

    override fun onComplete() {
        createStoryView()
        storyFragmentCallback?.storyEvent(StoryCompleteEvent())
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onPrev() {
        storiesStatus.pause()
        if (getCurrentSnap() - 1 < 0) {
            Handler().postDelayed({
                storiesStatus.resume()
            }, 300)
            if (typeStory == TypeStory.VIDEO_TYPE && videoPlayer?.isPlaying() == true) {
                videoPlayer?.seekTo(0)
            }
            return
        }
        decrementSnapCounter()
        setValues(story, uiConfig)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onNext() {
        storiesStatus.pause()
        incrementSnapCounter()
        setValues(story, uiConfig)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (storiesStatus != null)
            storiesStatus.destroy()
    }

    private fun setFormat(format: UiConfig.Format) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(baseLayout)
        constraintSet.connect(cardview.id, ConstraintSet.TOP, baseLayout.id, ConstraintSet.TOP)
        constraintSet.connect(cardview.id, ConstraintSet.LEFT, baseLayout.id, ConstraintSet.LEFT)
        constraintSet.connect(cardview.id, ConstraintSet.RIGHT, baseLayout.id, ConstraintSet.RIGHT)
        if (format == UiConfig.Format.FIXED) {
            constraintSet.setDimensionRatio(cardview.id, STORY_FIXED_RATIO)
            constraintSet.clear(cardview.id, ConstraintSet.BOTTOM)
        } else {
            constraintSet.connect(
                cardview.id,
                ConstraintSet.BOTTOM,
                baseLayout.id,
                ConstraintSet.BOTTOM
            )
        }

        storiesStatus.setPadding(pxToDp(statusMargin), 0, pxToDp(statusMargin), 0)

        constraintSet.applyTo(baseLayout)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setTheme(model: SnapModel) {

        model.apply {
            themeConfig.let { theme ->
                resources.apply {
                    context?.let {
                        val isLight = theme == UiConfig.Theme.LIGHT
                        gradient.background = if (model.enableGradient)
                            getDrawable(if (isLight) R.drawable.wistory_gradient_lignt else R.drawable.wistory_gradient_dark) else getDrawable(
                            android.R.color.transparent
                        )
                        tvStoryHeader.setTextColor(
                            resources.getColor(
                                if (isLight) R.color.wistory_black else R.color.wistory_white
                            )
                        )
                        footer.setColor(
                            it,
                            if (isLight) R.color.wistory_white else R.color.wistory_black
                        )

                        colorButton(ColorStateList.valueOf(getColor(if (isLight) R.color.wistory_black else R.color.wistory_white)))

                        action_button.backgroundTintList =
                            ColorStateList.valueOf(getColor(if (isLight) R.color.wistory_white else R.color.wistory_black))
                        action_button.setTextColor(ColorStateList.valueOf(getColor(if (isLight) R.color.wistory_black else R.color.wistory_white)))
                        close.imageTintList =
                            ColorStateList.valueOf(getColor(if (isLight) R.color.wistory_black else R.color.wistory_white))
                    }
                }
            }
        }
    }

    private fun colorButton(color: ColorStateList) {
        like.imageTintList = color
        dislike.imageTintList = color
        favorite.imageTintList = color
        share.imageTintList = color
        sound.imageTintList = color
        setupBottomButtons(story, color)
    }

    private fun setStoryHeaderText(title: String) {
        tvStoryHeader.text = if (title.length > STORY_HEADER_LENGTH)
            "${title.trim('\n', ' ').substring(0, STORY_HEADER_LENGTH)}..."
        else
            title
    }

    private fun setValues(story: Story, uiConfig: UiConfig) {
        videoPrepared = false

        createStoryHeader(story)

        story.content[getCurrentSnap()].apply {

            uiConfig.statusBarPosition?.let {
                statusbar?.alignmentConfig = it
            }
            setStatusBarConfig(this)
            setTheme(this)
            createButton(this)
            if (vote?.options?.isNotEmpty() == true)
                setVoteStory(this)
            else
                setTextStory(this)

            setFormat(uiConfig.format)

            videoPlayer?.releasePlayer()

            getContentResource().let {
                if (it.contains(".mp4")) {
                    setVideoContent(it)
                    setVolume(this.soundVideo)
                    sound.setOnClickListener {
                        setSound()
                    }
                } else {
                    setImageContent(it)
                }
            }
        }
    }

    private fun setSound() {
        if (sound.tag == R.drawable.ic_sound_on_white) {
            updateImageView(sound, R.drawable.ic_sound_off_white)
            setVolume(false)
        } else {
            updateImageView(sound, R.drawable.ic_sound_on_white)
            setVolume(true)
        }
    }

    private fun setVolume(soundVideo: Boolean? = true) {
        sound.visibility = View.VISIBLE
        if (soundVideo == false) {
            videoPlayer?.setVolume(0f)
            updateImageView(sound, R.drawable.ic_sound_off_white)
        } else if (soundVideo == true) {
            videoPlayer?.setVolume(
                audioService?.getStreamVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 0.5f
            )
            updateImageView(sound, R.drawable.ic_sound_on_white)
        }
    }

    private fun createStoryHeader(story: Story) {
        if (story.thumbnail.isNotEmpty() && story.title.isNotEmpty()) {
            Glide.with(requireContext())
                .load(story.thumbnail)
                .apply(RequestOptions().transforms(CenterCrop(), RoundedCorners(60)))
                .into(headerAvatar)
            setStoryHeaderText(story.title)
        }
    }

    private fun setupBottomButtons(story: Story, color: ColorStateList) {

        val position: Int = arguments?.getSerializable(ARG_STORY_POSITION) as Int

        setLike(story.relation)
        setFavoriteIcon(story.favorite)

        like.setOnClickListener {
            updateRelation(story, position, STORY_RELATION_LIKE)
        }
        dislike.setOnClickListener {
            updateRelation(story, position, STORY_RELATION_DISLIKE)
        }
        favorite.setOnClickListener {
            val isFavorite =
                favorite.tag == R.drawable.wistory_ic_not_favorite
            setFavoriteIcon(isFavorite)
            storyFragmentCallback?.storyEvent(
                FavoriteEvent(
                    story.apply { favorite = isFavorite },
                    isFavorite,
                    position
                )
            )
        }
    }

    private fun updateRelation(
        story: Story,
        position: Int,
        relation: String
    ) {
        setLike(relation)

        storyFragmentCallback?.storyEvent(RelationEvent(story.apply {
            this.relation = getRelationValue().toString()
        }, getRelationValue().toString(), position))
    }

    private fun getRelationValue(): String? {
        return when (like.tag) {
            R.drawable.wistory_ic_like -> STORY_RELATION_LIKE
            R.drawable.wistory_ic_dislike -> STORY_RELATION_DISLIKE
            else -> null
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setFavoriteIcon(fav: Boolean) {
        if (fav) {
            favorite.setImageDrawable(resources.getDrawable(R.drawable.wistory_ic_favorite))
            favorite.tag = R.drawable.wistory_ic_favorite
        } else {
            favorite.setImageDrawable(resources.getDrawable(R.drawable.wistory_ic_not_favorite))
            favorite.tag = R.drawable.wistory_ic_not_favorite
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setLike(liked: String?) {

        when (liked) {
            STORY_RELATION_LIKE -> if (like.tag == R.drawable.wistory_ic_like)
                updateImageView(
                    like,
                    R.drawable.wistory_ic_not_like,
                    dislike,
                    R.drawable.wistory_ic_not_dislike
                )
            else
                updateImageView(
                    like,
                    R.drawable.wistory_ic_like,
                    dislike,
                    R.drawable.wistory_ic_not_dislike
                )

            STORY_RELATION_DISLIKE -> if (dislike.tag == R.drawable.wistory_ic_dislike)
                updateImageView(
                    like,
                    R.drawable.wistory_ic_not_like,
                    dislike,
                    R.drawable.wistory_ic_not_dislike
                )
            else
                updateImageView(
                    like,
                    R.drawable.wistory_ic_not_like,
                    dislike,
                    R.drawable.wistory_ic_dislike
                )
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun updateImageView(
        firstView: ImageView,
        resUpdateFirstView: Int,
        lastView: ImageView? = null,
        resUpdateLastView: Int? = null
    ) {
        firstView.tag = resUpdateFirstView
        firstView.setImageDrawable(resources.getDrawable(resUpdateFirstView))
        lastView?.tag = resUpdateLastView
        lastView?.setImageDrawable(resUpdateLastView?.let { resources.getDrawable(it) })
    }

    private fun setVideoContent(content: String) {
        typeStory = TypeStory.VIDEO_TYPE
        imageProgressBar.visibility = View.VISIBLE
        setVideoVisible()
        videoPlayer?.initializePlayer(content)
    }

    private fun setImageContent(content: String) {
        typeStory = TypeStory.IMAGE_TYPE
        setImageVisible()
        if (videoPlayer?.isPlaying() == true) {
            videoPlayer?.pause()
        }
        target!!.setModel(content)
        Glide.with(image.context)
            .load(content)
            .apply(options)
            .into(target!!)
    }

    private fun startVideo() {
        try {
            if (storiesStatus != null) {
                videoPlayer?.play()
                imageProgressBar?.visibility = View.INVISIBLE
                storiesStatus.resume()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setVideoVisible() {
        mPlayerView.visibility = View.VISIBLE
        image.visibility = View.GONE
    }

    private fun setImageVisible() {
        image.visibility = View.VISIBLE
        mPlayerView.visibility = View.GONE
    }

    private fun createStoryView() {
        statusResources.clear()
        clearSnapCounter()
        try {
            target = MyProgressTarget(DrawableImageViewTarget(image))
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        story = requireArguments().getSerializable(ARG_STORY) as Story
        uiConfig = requireArguments().getSerializable(ARG_STORY_SETTINGS) as UiConfig

        storyFragmentCallback?.storyEvent(StoryNextEvent(story))

        for (s in story.content) {
            statusResources.add(s.getContentResource())
        }

        var arr = ArrayList<Long>()
        story.content.forEachIndexed { _, it ->
            arr.add(if (it.duration.toInt() < 10) 1000 else it.duration.toLong())
        }

        storiesStatus.setStoriesCountWithDurations(arr.toLongArray())
        storiesStatus.setUserInteractionListener(this)

        storiesStatus.playStories()
        storiesStatus.pause()

        setValues(story, uiConfig)
    }

    private fun setButtonAlignment(alignment: UiConfig.HorizontalAlignment) {
        action_button.apply {
            val constraintSet = ConstraintSet()
            constraintSet.clone(baseLayout)
            constraintSet.clear(id, ConstraintSet.LEFT)
            constraintSet.clear(id, ConstraintSet.RIGHT)
            when (alignment) {
                UiConfig.HorizontalAlignment.RIGHT -> {
                    constraintSet.connect(
                        id,
                        ConstraintSet.RIGHT,
                        baseLayout.id,
                        ConstraintSet.RIGHT,
                        pxToDp(buttonMargin)
                    )
                    constraintSet.connect(
                        id,
                        ConstraintSet.LEFT,
                        baseLayout.id,
                        ConstraintSet.LEFT,
                        pxToDp(buttonBetaMargin)
                    )
                }
                UiConfig.HorizontalAlignment.LEFT -> {
                    constraintSet.connect(
                        id,
                        ConstraintSet.LEFT,
                        baseLayout.id,
                        ConstraintSet.LEFT,
                        pxToDp(buttonMargin)
                    )
                    constraintSet.connect(
                        id,
                        ConstraintSet.RIGHT,
                        baseLayout.id,
                        ConstraintSet.RIGHT,
                        pxToDp(buttonBetaMargin)
                    )
                }
                UiConfig.HorizontalAlignment.CENTER -> {
                    constraintSet.connect(
                        id,
                        ConstraintSet.LEFT,
                        baseLayout.id,
                        ConstraintSet.LEFT,
                        pxToDp(buttonMargin)
                    )
                    constraintSet.connect(
                        id,
                        ConstraintSet.RIGHT,
                        baseLayout.id,
                        ConstraintSet.RIGHT,
                        pxToDp(buttonMargin)
                    )
                }
            }
            constraintSet.applyTo(baseLayout)
        }
    }

    private fun setStatusBarConfig(snapModel: SnapModel) {
        storiesStatus.apply {
            snapModel.statusbar?.apply {

                storiesStatus.updateProgressBar(color)

                val constraintSet = ConstraintSet()
                constraintSet.clone(baseLayout)
                when (alignmentConfig) {
                    UiConfig.VerticalAlignment.TOP -> {
                        constraintSet.setVerticalBias(id, STATUSBAR_VERTICAL_TOP_BIAS)
                        constraintSet.connect(close.id, ConstraintSet.TOP, id, ConstraintSet.TOP)
                        constraintSet.setMargin(
                            close.id,
                            ConstraintSet.END,
                            pxToDp(closeEndMargin)
                        )
                        constraintSet.setMargin(
                            close.id,
                            ConstraintSet.TOP,
                            pxToDp(closeTopMargin)
                        )
                        constraintSet.setMargin(
                            headerAvatar.id,
                            ConstraintSet.START,
                            pxToDp(avatarMargin)
                        )

                    }
                    UiConfig.VerticalAlignment.BOTTOM -> {
                        constraintSet.setVerticalBias(id, STATUSBAR_VERTICAL_BOTTOM_BIAS)
                        constraintSet.connect(
                            close.id,
                            ConstraintSet.TOP,
                            baseLayout.id,
                            ConstraintSet.TOP
                        )
                        constraintSet.setMargin(
                            close.id,
                            ConstraintSet.TOP,
                            pxToDp(closeParentTopMargin)
                        )
                        constraintSet.setMargin(
                            headerAvatar.id,
                            ConstraintSet.START,
                            pxToDp(avatarMargin)
                        )
                        constraintSet.setMargin(
                            close.id,
                            ConstraintSet.END,
                            pxToDp(closeEndMargin)
                        )
                    }
                }
                constraintSet.applyTo(baseLayout)
            }
        }
    }

    private fun createButton(snap: SnapModel) {

        snap.button?.let { button ->
            action_button.visibility = View.VISIBLE
            setButtonAlignment(button.alignmentConfig)
            action_button.text = button.text
            action_button.backgroundTintList =
                ColorStateList.valueOf(button.color.parseHexColor())
            action_button.setTextColor(
                ColorStateList.valueOf(button.textColor.parseHexColor())
            )
            action_button.setOnClickListener {
                storyFragmentCallback?.storyEvent(
                    NavigateEvent(
                        button.action,
                        button.value ?: button.valueUrl,
                        title.toString()
                    )
                )
            }
        } ?: removeButton()
    }

    private fun removeButton() {
        action_button.visibility = View.GONE
    }

    private fun setVoteStory(snapModel: SnapModel) {
        snapModel.vote?.run {
            list = options as MutableList<OptionModel>
            storyVotingView.apply {
                visibility = View.VISIBLE
                setCallback(this@StoryFragment)
                setVotingViewTheme(
                    StoryVotingAttr(
                        snapModel.vote?.title,
                        snapModel.themeConfig
                    )
                )
                val pos = getCurrentSnap()
                setVotingViewList(list, voted, story._id, pos, replay)
            }
            this@StoryFragment.title.visibility = View.GONE
            content.visibility = View.GONE
        }
    }

    private fun setTextStory(snapModel: SnapModel) {
        snapModel.textBlock?.let { textItem ->
            val constraintSet = ConstraintSet()
            constraintSet.clone(baseLayout)

            when (textItem.alignmentOverlay) {
                UiConfig.VerticalAlignment.TOP -> {
                    constraintSet.clear(textBlock.id, ConstraintSet.BOTTOM)
                    constraintSet.connect(
                        textBlock.id,
                        ConstraintSet.TOP,
                        close.id,
                        ConstraintSet.BOTTOM
                    )
                }
                UiConfig.VerticalAlignment.BOTTOM -> {
                    constraintSet.clear(textBlock.id, ConstraintSet.TOP)
                    constraintSet.connect(
                        textBlock.id,
                        ConstraintSet.BOTTOM,
                        action_button.id,
                        ConstraintSet.TOP
                    )
                }
                UiConfig.VerticalAlignment.CENTER -> {
                    constraintSet.connect(
                        textBlock.id,
                        ConstraintSet.BOTTOM,
                        action_button.id,
                        ConstraintSet.TOP
                    )
                    constraintSet.connect(
                        textBlock.id,
                        ConstraintSet.TOP,
                        close.id,
                        ConstraintSet.BOTTOM
                    )
                }
            }

            title.visibility = View.VISIBLE
            content.visibility = View.VISIBLE
            title.text = textItem.text
            content.text = textItem.subtext

            textItem.color?.let {
                ColorStateList.valueOf(Color.parseColor(it)).apply {
                    title.setTextColor(this)
                    content.setTextColor(this)
                }
            }

            constraintSet.applyTo(baseLayout)
        } ?: run {
            title.visibility = View.INVISIBLE
            content.visibility = View.INVISIBLE
            title.text = ""
            content.text = ""
        }
        storyVotingView.visibility = View.GONE
    }

    private fun pxToDp(dps: Int): Int {
        return (dps * requireContext().resources.displayMetrics.density + 0.5f).toInt()
    }

    override fun incrementSnapCounter(): Int {
        return counter++
    }

    override fun decrementSnapCounter(): Int {
        return counter--
    }

    override fun clearSnapCounter() {
        counter = 0
    }

    override fun getCurrentSnap() = counter

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {

        isFragmentEnabled = isVisibleToUser

        @RequiresApi(Build.VERSION_CODES.KITKAT)
        if (isVisibleToUser) {
            readStory()
            Handler().postDelayed({
                if (typeStory == TypeStory.VIDEO_TYPE && videoPlayer?.isPlaying() == false) {
                    if (videoPrepared)
                        startVideo()

                } else {
                    if (storiesStatus != null && image?.drawable != null) {
                        storiesStatus.resume()
                    }
                }
            }, 300)
        } else {
            Handler().postDelayed({
                if (storiesStatus != null) {
                    videoPlayer?.pause()
                    storiesStatus.pause()
                }
            }, 300)
        }
    }

    private fun readStory() {
        val story: Story? = arguments?.getSerializable(ARG_STORY) as Story
        story?.let {
            storyFragmentCallback?.run {
                if (story.fresh) {
                    story.fresh = false
                    storyEvent(ReadStoryEvent(story))
                }
            }
        }
    }

    inner class MyProgressTarget<Z>(target: Target<Z>) : ProgressTarget<String, Z>(target) {

        override val granualityPercentage: Float
            get() = 0.1f

        @RequiresApi(Build.VERSION_CODES.KITKAT)
        override fun onConnecting() {
            if (imageProgressBar != null) {
                imageProgressBar.isIndeterminate = true
                imageProgressBar.visibility = View.VISIBLE
                try {
                    storiesStatus.pause()
                } catch (e: Exception) {
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.KITKAT)
        override fun onDownloading(bytesRead: Long, expectedLength: Long) {
            if (imageProgressBar != null) {
                imageProgressBar.isIndeterminate = false
                imageProgressBar.progress = (100 * bytesRead / expectedLength).toInt()
                storiesStatus?.pause()
            }
        }

        @RequiresApi(Build.VERSION_CODES.KITKAT)
        override fun onDownloaded() {
            if (imageProgressBar != null) {
                imageProgressBar.isIndeterminate = true
                storiesStatus?.pause()
            }
        }

        @RequiresApi(Build.VERSION_CODES.KITKAT)
        override fun onDelivered() {

            if (imageProgressBar != null) {
                imageProgressBar.visibility = View.INVISIBLE
                if (isFragmentEnabled)
                    Handler().postDelayed({
                        storiesStatus?.resume()
                    }, 300)
            }
        }
    }

    enum class TypeStory {
        VIDEO_TYPE, IMAGE_TYPE
    }

    override fun videoBuffering(playWhenReady: Boolean) {}

    override fun videoEnd(playWhenReady: Boolean) {}

    override fun videoIdle(playWhenReady: Boolean) {}

    override fun videoReady(playWhenReady: Boolean) {

        if (isFragmentEnabled && !storyPlayAgain) {
            startVideo()
        }
        videoPrepared = true
    }

    override fun onStart() {
        super.onStart()

        var storyHaveVideo = false
        val story = requireArguments().getSerializable(ARG_STORY) as Story
        story.content.map {
            if (it.video?.contains(".mp4") == true) {
                storyHaveVideo = true
            }
        }
        if (storyHaveVideo) {
            videoPlayer = VideoPlayer(requireContext(), mPlayerView, this)
        }
    }

    override fun onResume() {
        super.onResume()
        createStoryView()
    }

    override fun onPause() {
        super.onPause()
        storiesStatus?.destroy()
        videoPlayer?.destroy()
        if (Util.SDK_INT <= 23) videoPlayer?.releasePlayer()
    }

    override fun onStop() {
        super.onStop()

        if (Util.SDK_INT > 23) videoPlayer?.releasePlayer()
        videoPlayer = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (storiesStatus != null)
            storiesStatus.destroy()
    }

    override fun itemSelected(storyId: String, newpoll: String, oldpoll: String, sheet: Int) {
        story.content[sheet].vote?.voted = newpoll
        (activity as StoryFragmentCallback).storyEvent(VoteEvent(storyId, newpoll, sheet))
    }
}
