package ru.vvdev.wistory.internal.presentation.items

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import ru.vvdev.wistory.R
import ru.vvdev.wistory.internal.data.models.Story
import ru.vvdev.wistory.internal.presentation.views.extentions.avoidDoubleClicks

internal open class StoryItem(
    val context: Context?,
    var data: Story,
    val storyClickListener: OnStoryClickListener? = null
) : AbstractFlexibleItem<StoryItem.ViewHolder>() {

    override fun getLayoutRes() = R.layout.wistory_item_choose_story

    override fun createViewHolder(
        view: View?,
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>?
    ): ViewHolder {
        return ViewHolder(view!!, adapter!!)
    }

    override fun bindViewHolder(
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>?,
        holder: StoryItem.ViewHolder?,
        position: Int,
        payloads: MutableList<Any>?
    ) {
        holder?.apply {
            context?.let {
                Glide.with(it)
                    .load(data.thumbnail)
                    .apply(RequestOptions().transforms(CenterCrop(), RoundedCorners(24)))
                    .into(image)
                Glide.with(it)
                    .load(R.drawable.wistory_gradient_item)
                    .apply(RequestOptions().transforms(CenterCrop(), RoundedCorners(24)))
                    .into(gradient)
            }
            historyText.text = data.title

            historyText.setTypeface(historyText.typeface, Typeface.BOLD)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as StoryItem

        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    inner class ViewHolder(
        view: View,
        adapter: FlexibleAdapter<out IFlexible<*>>
    ) : FlexibleViewHolder(view, adapter), View.OnClickListener {
        val image = view.findViewById<ImageView>(R.id.image)
        val gradient = view.findViewById<ImageView>(R.id.gradient)
        val historyText = view.findViewById<TextView>(R.id.historyText)

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            super.onClick(view)
            storyClickListener?.onStoryClick(adapterPosition)
            view?.avoidDoubleClicks()
        }
    }

    interface OnStoryClickListener {
        fun onStoryClick(position: Int)
        fun onFavoriteItemClick()
    }
}
