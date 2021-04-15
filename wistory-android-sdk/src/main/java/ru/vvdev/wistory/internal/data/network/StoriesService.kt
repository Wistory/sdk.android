package ru.vvdev.wistory.internal.data.network

import retrofit2.http.*
import ru.vvdev.wistory.internal.data.models.FavoriteRequest
import ru.vvdev.wistory.internal.data.models.PollRequest
import ru.vvdev.wistory.internal.data.models.ReadRequest
import ru.vvdev.wistory.internal.data.models.RegisterResponse
import ru.vvdev.wistory.internal.data.models.RelationRequest
import ru.vvdev.wistory.internal.data.models.Story
import ru.vvdev.wistory.internal.data.models.StoryByEvent

internal interface StoriesService {

    @GET("users/story")
    suspend fun listRepos(): ArrayList<Story>

    @GET("users/favorite")
    suspend fun getFavorites(): ArrayList<Story>

    @GET("users/story/event/{eventId}")
    suspend fun getByEventId(
        @Path("eventId") eventId: Int,
        @Header("screen-height") screenHeight: Int? = null,
        @Header("screen-width") screenWidth: Int? = null
    ): StoryByEvent

    @PUT("users/relation")
    suspend fun setRelation(@Body body: RelationRequest): Story

    @PUT("users/favorite")
    suspend fun setFavorite(@Body body: FavoriteRequest): Story

    @PUT("users/new")
    suspend fun setRead(@Body body: ReadRequest): Story

    @POST("users/registration")
    suspend fun registration(): RegisterResponse

    @POST("vote/poll")
    suspend fun poll(@Body pollRequest: PollRequest): Story
}
