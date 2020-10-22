package ru.vvdev.wistory.internal.data.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import ru.vvdev.wistory.internal.data.models.FavoriteRequest
import ru.vvdev.wistory.internal.data.models.PollRequest
import ru.vvdev.wistory.internal.data.models.ReadRequest
import ru.vvdev.wistory.internal.data.models.RegisterResponse
import ru.vvdev.wistory.internal.data.models.RelationRequest
import ru.vvdev.wistory.internal.data.models.Story

internal interface StoriesService {

    @GET("users/story")
    suspend fun listRepos(): ArrayList<Story>

    @GET("users/favorite")
    suspend fun getFavorites(): ArrayList<Story>

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
