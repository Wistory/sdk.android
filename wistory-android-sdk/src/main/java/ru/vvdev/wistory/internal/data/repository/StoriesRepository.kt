package ru.vvdev.wistory.internal.data.repository

import ru.vvdev.wistory.internal.data.models.FavoriteRequest
import ru.vvdev.wistory.internal.data.models.PollRequest
import ru.vvdev.wistory.internal.data.models.ReadRequest
import ru.vvdev.wistory.internal.data.models.RelationRequest
import ru.vvdev.wistory.internal.data.models.Story
import ru.vvdev.wistory.internal.data.models.StoryByEvent
import ru.vvdev.wistory.internal.data.network.StoriesApi
import java.util.UUID

internal class StoriesRepository {

    suspend fun getStories(): ArrayList<Story>? {
        return StoriesApi.getStoryService()?.listRepos()
    }

    suspend fun getByEventId(eventId: Int): StoryByEvent? {
        return StoriesApi.getStoryService()?.getByEventId(eventId)
    }

    suspend fun register(): Any? {
        return StoriesApi.getStoryService()?.registration()
    }

    suspend fun getFavorites(): ArrayList<Story>? {
        return StoriesApi.getStoryService()?.getFavorites()
    }

    suspend fun poll(
        storyId: String,
        sheet: Int,
        newpoll: String? = null,
        oldpoll: String? = null
    ): Story? {
        return StoriesApi.getStoryService()?.poll(PollRequest(storyId, sheet, newpoll, oldpoll))
    }

    suspend fun setFavorite(storyId: String, favorite: Boolean): Story? {
        return StoriesApi.getStoryService()?.setFavorite(FavoriteRequest(favorite, storyId))
    }

    suspend fun setRelation(storyId: String, relation: String): Story? {
        return StoriesApi.getStoryService()?.setRelation(RelationRequest(relation, storyId))
    }

    suspend fun setRead(storyId: String): Story? {
        return StoriesApi.getStoryService()?.setRead(ReadRequest(storyId, false))
    }
}
