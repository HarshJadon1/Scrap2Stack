package com.scrap2stack.app.data.remote.dto

data class RegisterRequest(
    val name: String,
    val username: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val user: UserSummaryDto
)

data class UserSummary(
    val id: String,
    val name: String,
    val username: String,
    val email: String
)

// Alias for consistency with backend DTO naming
typealias UserSummaryDto = UserSummary
