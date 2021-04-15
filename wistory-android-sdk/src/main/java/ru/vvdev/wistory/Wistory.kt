package ru.vvdev.wistory

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

object Wistory {

    private lateinit var applicationContext: Context

    private val app: ApplicationInfo by lazy {
        applicationContext.packageManager.getApplicationInfo(
            applicationContext.packageName,
            PackageManager.GET_META_DATA
        )
    }

    fun initialize(application: Application) {
        this.applicationContext = application.applicationContext
    }

    fun singleStory(context: Context): SingleStory = SingleStory(context)

    val token: String?
        get() {
            return app.metaData.getString("WISTORY_TOKEN")
        }

    val serverUrl: String?
        get() {
            return "${app.metaData.getString("WISTORY_SERVER_URL") ?: applicationContext.resources.getString(R.string.wistory_base_url)}"
        }
}
