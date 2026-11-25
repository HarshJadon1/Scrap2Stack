package com.scrap2stack.app.core.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ServiceProvenanceTest {

    @Test
    fun `LiveDataResult preserves provenance and statusNote`() {
        val live = LiveDataResult(data = "ok", isLiveData = true)
        assertTrue(live.isLiveData)
        assertNull(live.statusNote)

        val offline = LiveDataResult(
            data = "fallback",
            isLiveData = false,
            statusNote = "Offline heuristic"
        )
        assertFalse(offline.isLiveData)
        assertEquals("Offline heuristic", offline.statusNote)
    }

    @Test
    fun `GitHubService parseRepoUrl extracts owner and repo cleanly`() {
        val service = GitHubService()
        assertEquals(
            Pair("HarshJadon1", "Scrap2Stack"),
            service.parseRepoUrl("https://github.com/HarshJadon1/Scrap2Stack.git")
        )
        assertEquals(
            Pair("octocat", "Hello-World"),
            service.parseRepoUrl("github.com/octocat/Hello-World")
        )
        assertNull(service.parseRepoUrl("invalid-url"))
    }
}
