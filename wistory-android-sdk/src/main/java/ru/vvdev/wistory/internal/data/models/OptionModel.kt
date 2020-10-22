package ru.vvdev.wistory.internal.data.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

internal data class OptionModel(
    val text: String,
    @SerializedName("_id") val optionId: String,
    var votes: Int
) : Serializable
