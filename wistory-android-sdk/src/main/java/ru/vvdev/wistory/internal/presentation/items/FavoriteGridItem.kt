package ru.vvdev.wistory.internal.presentation.items

import android.content.Context
import android.view.View
import android.widget.ImageView
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

internal class FavoriteGridItem(
    val context: Context?,
    val data: Story,
    val storyClickListener: OnStoryClickListener
) : AbstractFlexibleItem<FavoriteGridItem.ViewHolder>() {

    override fun getLayoutRes() = R.layout.wistory_item_grid_favorite_story

    override fun createViewHolder(
        view: View?,
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>?
    ): ViewHolder {
        return ViewHolder(view!!, adapter!!)
    }

    override fun bindViewHolder(
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>?,
        holder: ViewHolder?,
        position: Int,
        payloads: MutableList<Any>?
    ) {
        holder?.apply {
            context?.let {
                Glide.with(it)
                    .load(data.thumbnail)
                    .apply(RequestOptions().transforms(CenterCrop(), RoundedCorners(8)))
                    .into(image)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as FavoriteGridItem

        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    inner class ViewHolder(
        view: View,
        adapter: FlexibleAdapter<out IFlexible<*>>
    ) : FlexibleViewHolder(view, adapter) {
        val image = view.findViewById<ImageView>(R.id.image)

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            super.onClick(view)
            storyClickListener.onStoryFavoriteClick()
        }
    }

    interface OnStoryClickListener {
        fun onStoryFavoriteClick()
    }
}
