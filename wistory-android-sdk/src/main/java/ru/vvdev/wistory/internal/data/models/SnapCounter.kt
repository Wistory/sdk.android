package ru.vvdev.wistory.internal.data.models

internal interface SnapCounter {
    /**
     * @return текущую позицию историй
     * */
    fun getCurrentSnapPosition(): Int

    fun incrementSnapCounter(): Int

    fun decrementSnapCounter(): Int

    fun clearSnapCounter()
}
