package com.scrap2stack.app.core.network

/**
 * Wraps network/AI payloads with explicit provenance so UI layers can surface
 * offline/fallback heuristics instead of presenting mock data as live responses.
 */
data class LiveDataResult<T>(
    val data: T,
    val isLiveData: Boolean,
    val statusNote: String? = null
)
