package com.scrap2stack.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MatchingResponse(
    @SerialName("project_id")
    val projectId: String,
    val matches: List<MatchResultDto> = emptyList()
)

@Serializable
data class MatchResultDto(
    val developer: UserDto,
    @SerialName("match_percentage")
    val matchPercentage: Int = 0,
    val label: String = "MATCH",
    @SerialName("matched_skills")
    val matchedSkills: List<String> = emptyList(),
    @SerialName("partial_skills")
    val partialSkills: List<String> = emptyList(),
    @SerialName("missing_skills")
    val missingSkills: List<String> = emptyList(),
    @SerialName("matched_interests")
    val matchedInterests: List<String> = emptyList(),
    val reasons: List<String> = emptyList(),
    @SerialName("score_breakdown")
    val scoreBreakdown: Map<String, Int> = emptyMap()
)
