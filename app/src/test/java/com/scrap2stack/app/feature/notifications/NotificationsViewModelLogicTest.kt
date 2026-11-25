package com.scrap2stack.app.feature.notifications

import com.scrap2stack.app.domain.model.NotificationItem
import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationsViewModelLogicTest {

    @Test
    fun `countUnread returns 0 for loading and error states`() {
        assertEquals(0, NotificationsViewModel.countUnread(NotificationsUiState.Loading))
        assertEquals(0, NotificationsViewModel.countUnread(NotificationsUiState.Error("Offline")))
    }

    @Test
    fun `countUnread counts only unread notifications`() {
        val items = listOf(
            NotificationItem(id = "1", title = "Invite", content = "Join", read = false),
            NotificationItem(id = "2", title = "Charms", content = "+10", read = true),
            NotificationItem(id = "3", title = "Shipped", content = "+100", read = false)
        )
        val state = NotificationsUiState.Success(items)
        assertEquals(2, NotificationsViewModel.countUnread(state))
    }
}
