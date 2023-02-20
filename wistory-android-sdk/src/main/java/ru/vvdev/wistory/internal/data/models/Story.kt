package ru.vvdev.wistory.internal.data.models

import java.io.Serializable

/**
 * @param fresh отвечает за статус просмотра истории true - истории открывали, false - не открывали
 * */
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
