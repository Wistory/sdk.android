package ru.vvdev.wistory.internal.presentation.items

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import ru.vvdev.wistory.R

internal class PlaceholderStoryItem : AbstractFlexibleItem<PlaceholderStoryItem.ViewHolder>() {

    override fun bindViewHolder(
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>?,
        holder: ViewHolder?,
        position: Int,
        payloads: MutableList<Any>?
    ) {}

    override fun equals(other: Any?) = false

    override fun createViewHolder(
        view: View?,
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>?
    ): ViewHolder {
        return ViewHolder(view!!, adapter!!)
    }

    inner class ViewHolder(
        view: View,
        adapter: FlexibleAdapter<out IFlexible<*>>
    ) : FlexibleViewHolder(view, adapter), View.OnClickListener

    override fun getLayoutRes() = R.layout.wistory_item_placeholder_choose_story
}
