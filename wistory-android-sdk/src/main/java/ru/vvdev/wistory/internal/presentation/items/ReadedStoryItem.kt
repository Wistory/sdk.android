package ru.vvdev.wistory.internal.presentation.items

import android.content.Context
import ru.vvdev.wistory.R
import ru.vvdev.wistory.internal.data.models.Story

internal class ReadedStoryItem(
    context: Context?,
    data: Story,
    storyClickListener: OnStoryClickListener?
) : StoryItem(context, data, storyClickListener) {

    override fun getLayoutRes() = R.layout.wistory_item_choose_readed_story
}
