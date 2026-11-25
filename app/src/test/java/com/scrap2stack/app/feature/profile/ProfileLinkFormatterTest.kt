package com.scrap2stack.app.feature.profile

import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileLinkFormatterTest {

    @Test
    fun `formatSocialDisplay strips linkedin UTM params and prefix`() {
        val raw = "https://www.linkedin.com/in/harsh-jadon-7ab7712b5?utm_source=share&utm_campaign=share_via&utm_content=profile&utm_medium=android_app"
        assertEquals("harsh-jadon-7ab7712b5", formatSocialDisplay("LinkedIn", raw))
    }

    @Test
    fun `formatSocialDisplay extracts github handle cleanly`() {
        assertEquals("HarshJadon1", formatSocialDisplay("GitHub", "https://github.com/HarshJadon1/"))
        assertEquals("HarshJadon1/Scrap2Stack", formatSocialDisplay("GitHub", "github.com/HarshJadon1/Scrap2Stack"))
    }

    @Test
    fun `formatSocialDisplay strips protocol and query for portfolio links`() {
        assertEquals("harshjadon.dev", formatSocialDisplay("Portfolio", "https://www.harshjadon.dev/?ref=mobile"))
        assertEquals("", formatSocialDisplay("Portfolio", "   "))
    }
}
