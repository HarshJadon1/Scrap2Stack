package com.scrap2stack.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val name: String,
    val username: String,
    val email: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String = "",
    val user: UserSummaryDto? = null
)

@Serializable
data class UserSummary(
    val id: String = "",
    val name: String = "",
    val username: String = "",
    val email: String = ""
)

typealias UserSummaryDto = UserSummary
