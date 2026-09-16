package com.scrap2stack.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RevivalScoreDto(
    val score: Int,
    val label: String,
    val factors: List<ScoreFactorDto>,
    val explanation: String
)

@Serializable
data class ScoreFactorDto(
    val name: String,
    val score: Int,
    val description: String? = null
)
