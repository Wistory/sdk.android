package ru.vvdev.wistory.internal.presentation.items

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import ru.vvdev.wistory.R
import ru.vvdev.wistory.internal.data.models.Story
import ru.vvdev.wistory.internal.presentation.decorators.GridSpacingItemDecoration
import ru.vvdev.wistory.internal.presentation.views.extentions.removeItemDecorations

internal class FavoriteStoryItem(
    val context: Context?,
    val data: ArrayList<Story>,
    val storyClickListener: StoryItem.OnStoryClickListener?
) : AbstractFlexibleItem<FavoriteStoryItem.ViewHolder>() {

    private var flexAdapter = FlexibleAdapter<IFlexible<*>>(listOf())

    override fun getLayoutRes() = R.layout.wistory_item_choose_favorite_story

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
        val dividerItemDecoration =
            GridSpacingItemDecoration(3, 8, false)

        holder?.apply {
            context?.let {
                flexAdapter.clear()
                rvFavorite.apply {
                    removeItemDecorations()
                    layoutManager = GridLayoutManager(
                        context, 3
                    )
                    this.adapter = flexAdapter.apply {
                        data.forEach {
                            addItem(
                                FavoriteGridItem(
                                    context,
                                    it,
                                    object : FavoriteGridItem.OnStoryClickListener {
                                        override fun onStoryFavoriteClick() {
                                            storyClickListener?.onFavoriteItemClick()
                                        }
                                    })
                            )
                        }
                    }
                    addItemDecoration(dividerItemDecoration)
                }
            }
            itemView.setOnClickListener {
                storyClickListener?.onFavoriteItemClick()
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as FavoriteStoryItem

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
        val rvFavorite = view.findViewById<RecyclerView>(R.id.rvFavorite)
    }
}
