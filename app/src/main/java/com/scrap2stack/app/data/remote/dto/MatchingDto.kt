package com.scrap2stack.app.data.remote.dto

data class MatchingResponse(
    val projectId: String,
    val matches: List<MatchResultDto>
)

data class MatchResultDto(
    val developer: UserDto,
    val matchPercentage: Int,
    val label: String,
    val matchedSkills: List<String>,
    val partialSkills: List<String>,
    val missingSkills: List<String>,
    val matchedInterests: List<String>,
    val reasons: List<String>,
    val scoreBreakdown: Map<String, Int>
)
