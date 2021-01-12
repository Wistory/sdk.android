package ru.vvdev.wistory.internal.data.models

import java.io.Serializable

internal data class StoryVotingModel(
    var options: List<OptionModel>,
    val title: String,
    val replay: Boolean,
    var voted: String = "-1"
) : Serializable
