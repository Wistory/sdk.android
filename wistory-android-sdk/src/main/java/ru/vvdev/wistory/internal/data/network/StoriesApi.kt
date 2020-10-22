package ru.vvdev.wistory.internal.data.network

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.provider.Settings
import com.google.gson.GsonBuilder
import java.util.concurrent.TimeUnit
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.vvdev.wistory.BuildConfig
import ru.vvdev.wistory.ServerConfig

internal object StoriesApi {

    private var instance: StoriesService? = null
    private var userId: String? = null

    @SuppressLint("HardwareIds")
    private fun getClient(context: Context, serverConfig: ServerConfig): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val cacheSize = (10 * 1024 * 1024).toLong()
        val cache = Cache(context.cacheDir, cacheSize)

        return OkHttpClient.Builder()
            .apply {
                cache(cache)
                connectTimeout(2, TimeUnit.MINUTES)
                readTimeout(2, TimeUnit.MINUTES)
                addInterceptor { chain ->
                    val request = chain.request().newBuilder().apply {
                        addHeader(
                            "x-access-token",
                            serverConfig.token.toString()
                        )
                        addHeader(
                            "user-token",
                            serverConfig.registrationId ?: "5${Settings.Secure.getString(
                                context.contentResolver,
                                Settings.Secure.ANDROID_ID
                            )}"
                        )
                        addHeader(
                            "screen-height",
                            Resources.getSystem().displayMetrics.heightPixels.toString()
                        )
                        addHeader(
                            "screen-width",
                            Resources.getSystem().displayMetrics.widthPixels.toString()
                        )
                    }.build()
                    chain.proceed(request)
                }
                if (BuildConfig.DEBUG) {
                    addInterceptor(logging)
                }
            }.build()
    }

    fun getRegistrationId() = userId ?: Settings.Secure.ANDROID_ID

    private fun setRegistrationId(id: String) {
        userId = id
    }

    fun createService(context: Context, serverConfig: ServerConfig): StoriesService {
        val retrofit = Retrofit.Builder()
            .baseUrl(serverConfig.serverUrl)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .client(getClient(context, serverConfig))
            .build()

        serverConfig.registrationId?.let {
            setRegistrationId(it)
        }

        instance = retrofit.create(StoriesService::class.java)
        return instance as StoriesService
    }

    fun getStoryService() = instance
}
