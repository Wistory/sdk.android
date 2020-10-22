package ru.vvdev.storiesdemo

import android.app.Application
import ru.vvdev.wistory.Wistory

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Wistory.initialize(this)
    }
}
