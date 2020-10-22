package ru.vvdev.wistory.internal.data.models

internal interface SnapCounter {

    fun getCurrentSnap(): Int

    fun incrementSnapCounter(): Int

    fun decrementSnapCounter(): Int

    fun clearSnapCounter()
}
