package ru.vvdev.storieslib.presentation.items

import android.app.Activity
import android.view.View
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.IFlexible
import ru.vvdev.storieslib.R
import ru.vvdev.storieslib.data.models.Story


class ReadedStoryItem(
    activity: Activity?,
    data: Story,
    storyClickListener: OnStoryClickListener? = null
) : StoryItem(activity, data, storyClickListener) {

    override fun getLayoutRes() = R.layout.item_choose_readed_story

}