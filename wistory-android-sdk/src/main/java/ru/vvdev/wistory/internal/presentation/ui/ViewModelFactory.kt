package ru.vvdev.wistory.internal.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.vvdev.wistory.internal.data.repository.StoriesRepository

internal class ViewModelFactory(
    private val repository: StoriesRepository,
    private val isOpenFromUnreadStory: Boolean
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(StoriesRepository::class.java, Boolean::class.java)
            .newInstance(repository, isOpenFromUnreadStory)
    }
}
