package com.scrap2stack.app.core.network

/**
 * Strips raw HTTP debug dumps (URLs, Supabase headers, JWT bearer tokens)
 * and maps backend/PostgreSQL exceptions to clean, user-friendly messages for the UI.
 */
fun Throwable.toUserFriendlyMessage(defaultMessage: String = "An unexpected error occurred. Please try again."): String {
    val raw = message ?: localizedMessage ?: return defaultMessage

    // 1. Extract the primary human-readable error before any "URL:" or "Headers:" lines
    val lines = raw.lines()
    val primaryLine = lines.firstOrNull { line ->
        val trimmed = line.trim()
        trimmed.isNotBlank() &&
            !trimmed.startsWith("URL:", ignoreCase = true) &&
            !trimmed.startsWith("Headers:", ignoreCase = true) &&
            !trimmed.startsWith("Body:", ignoreCase = true) &&
            !trimmed.contains("Authorization=", ignoreCase = true) &&
            !trimmed.contains("Bearer ", ignoreCase = true)
    }?.trim() ?: defaultMessage

    val clean = primaryLine
        .removePrefix("Failed to send invitation:")
        .removePrefix("Failed to send message:")
        .removePrefix("Failed to save profile:")
        .removePrefix("Exception:")
        .trim()

    // 2. Map database constraints and trigger messages to human-readable explanations
    return when {
        clean.contains("pending request already exists", ignoreCase = true) ||
        clean.contains("request already exists", ignoreCase = true) ||
        clean.contains("already exists", ignoreCase = true) && clean.contains("request", ignoreCase = true) ->
            "A collaboration invitation is already pending for this developer on this project."

        clean.contains("already a member", ignoreCase = true) ||
        clean.contains("already in project", ignoreCase = true) ->
            "This developer is already a member of this project."

        clean.contains("cannot invite yourself", ignoreCase = true) ->
            "You cannot invite yourself to collaborate on your own project."

        clean.contains("duplicate key", ignoreCase = true) ||
        clean.contains("23505", ignoreCase = true) ->
            "An invitation for this project and developer has already been recorded."

        clean.contains("User not authenticated", ignoreCase = true) ||
        clean.contains("JWT expired", ignoreCase = true) ||
        clean.contains("PGRST301", ignoreCase = true) ->
            "Your session has expired. Please sign in again."

        clean.contains("NetworkError", ignoreCase = true) ||
        clean.contains("Unable to resolve host", ignoreCase = true) ||
        clean.contains("ConnectException", ignoreCase = true) ->
            "Unable to connect to the server. Please check your internet connection."

        clean.isNotBlank() && clean.length < 140 && !clean.contains("http", ignoreCase = true) ->
            clean

        else -> defaultMessage
    }
}
