package ru.vvdev.wistory.internal.data.models

import java.io.Serializable

internal class Story(
    val content: ArrayList<SnapModel>,
    val title: String,
    val read: Boolean,
    val thumbnail: String,
    val _id: String,
    var relation: String,
    var favorite: Boolean,
    var fresh: Boolean
) : Serializable
