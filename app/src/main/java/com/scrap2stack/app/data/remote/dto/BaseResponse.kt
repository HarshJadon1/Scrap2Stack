package com.scrap2stack.app.data.remote.dto

data class BaseResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val error: ErrorData? = null
)

data class ErrorData(
    val code: String,
    val details: Any? = null
)
