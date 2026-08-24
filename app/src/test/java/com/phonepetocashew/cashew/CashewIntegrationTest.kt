package com.phonepetocashew.cashew

import com.phonepetocashew.receipt.TransactionDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URLDecoder
import java.time.LocalDateTime

class CashewIntegrationTest {

    @Test
    fun buildDeepLinkUrl_formatsAmountAsNegativeExpense() {
        val url = CashewIntegration.buildDeepLinkUrl(
            amount = 30.0,
            direction = TransactionDirection.EXPENSE,
            dateTime = LocalDateTime.of(2026, 8, 23, 10, 43),
            notes = "Paid to: Merchant Store\nUTR: 123456789012"
        )

        assertTrue(url.startsWith("https://cashewapp.web.app/addTransactionRoute?"))
        assertTrue(url.contains("amount=-30.0"))
        assertTrue(url.contains("income=false"))
        assertTrue(url.contains("date=2026-08-23T10%3A43%3A00"))
        assertTrue(url.contains("notes=Paid+to%3A+Merchant+Store%0A"))

        // Decode and verify
        val decoded = URLDecoder.decode(url, "UTF-8")
        assertTrue(decoded.contains("amount=-30.0"))
        assertTrue(decoded.contains("date=2026-08-23T10:43:00"))
        assertTrue(decoded.contains("Paid to: Merchant Store\nUTR: 123456789012"))
    }

    @Test
    fun buildDeepLinkUrl_formatsAmountAsPositiveIncome() {
        val url = CashewIntegration.buildDeepLinkUrl(
            amount = 404.0,
            direction = TransactionDirection.INCOME,
            title = "Sender Name",
            dateTime = LocalDateTime.of(2026, 8, 6, 21, 20),
            notes = "Received from: Sender Name\nUTR: 987654321098"
        )

        assertTrue(url.startsWith("https://cashewapp.web.app/addTransactionRoute?"))
        assertTrue(url.contains("amount=404.0"))
        assertTrue(url.contains("title=Sender+Name"))
        assertTrue(url.contains("income=true"))
        assertTrue(url.contains("date=2026-08-06T21%3A20%3A00"))
    }

    @Test
    fun buildDeepLinkUrl_handlesAlreadyNegativeAmount() {
        val url = CashewIntegration.buildDeepLinkUrl(
            amount = -450.50,
            direction = TransactionDirection.EXPENSE
        )
        assertTrue(url.contains("amount=-450.5"))
    }
}
