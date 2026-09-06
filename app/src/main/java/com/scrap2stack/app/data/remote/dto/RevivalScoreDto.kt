package com.scrap2stack.app.data.remote.dto

data class RevivalScoreDto(
    val score: Int,
    val label: String,
    val factors: List<ScoreFactorDto>,
    val explanation: String
)
