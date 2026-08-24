package com.phonepetocashew.receipt

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class ReceiptResultTest {

    @Test
    fun toNotesString_allFieldsPresent() {
        val result = ReceiptResult(
            amount = 30.0,
            dateTime = LocalDateTime.of(2026, 8, 23, 10, 43),
            merchant = "Merchant Store",
            upiId = "merchant@upi",
            txnId = "T2400000000000000000001",
            utr = "123456789012",
            debitAccount = "XXXXXXXX1234"
        )

        val expected = """
            Paid to: Merchant Store
            UPI: merchant@upi
            PhonePe Txn ID: T2400000000000000000001
            UTR: 123456789012
            Debited from: XXXXXXXX1234
        """.trimIndent()

        assertEquals(expected, result.toNotesString())
    }

    @Test
    fun toNotesString_partialFields() {
        val result = ReceiptResult(
            amount = 150.0,
            merchant = "Coffee Shop",
            upiId = "coffeeshop@upi"
        )

        val expected = """
            Paid to: Coffee Shop
            UPI: coffeeshop@upi
        """.trimIndent()

        assertEquals(expected, result.toNotesString())
    }

    @Test
    fun toNotesString_emptyFields() {
        val result = ReceiptResult(amount = 50.0)
        assertEquals("", result.toNotesString())
    }

    @Test
    fun isValidAmount_validation() {
        assertTrue(ReceiptResult(amount = 30.0).isValidAmount)
        assertFalse(ReceiptResult(amount = null).isValidAmount)
        assertFalse(ReceiptResult(amount = 0.0).isValidAmount)
        assertFalse(ReceiptResult(amount = -10.0).isValidAmount)
    }
}
