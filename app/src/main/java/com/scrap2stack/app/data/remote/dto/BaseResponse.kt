package com.scrap2stack.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<T>(
    val success: Boolean = true,
    val message: String = "",
    val data: T? = null,
    val error: ErrorData? = null
)

@Serializable
data class ErrorData(
    val code: String = "",
    val details: String? = null
)
