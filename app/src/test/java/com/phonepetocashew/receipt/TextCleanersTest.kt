package com.phonepetocashew.receipt

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TextCleanersTest {

    @Test
    fun cleanMerchant_extractsName() {
        val raw = """
            Paid to
            MS
            Merchant Store
            merchant@upi
        """.trimIndent()
        assertEquals("Merchant Store", TextCleaners.cleanMerchant(raw))

        assertEquals("ABC Food Court", TextCleaners.cleanMerchant("ABC Food Court"))
        assertEquals("Sample Business", TextCleaners.cleanMerchant("Sample Business\nupi@bank"))
    }

    @Test
    fun cleanUpiId_extractsVpa() {
        val raw = "merchant@upi"
        assertEquals("merchant@upi", TextCleaners.cleanUpiId(raw))

        val multiline = "Merchant Store\nmerchant@upi\n₹30"
        assertEquals("merchant@upi", TextCleaners.cleanUpiId(multiline))

        assertNull(TextCleaners.cleanUpiId("No upi address here"))
    }

    @Test
    fun cleanTxnId_extractsPhonePeId() {
        val raw = """
            PhonePe Transaction ID
            T2400000000000000000001
        """.trimIndent()
        assertEquals("T2400000000000000000001", TextCleaners.cleanTxnId(raw))

        assertNull(TextCleaners.cleanTxnId("Random text without txn id"))
    }

    @Test
    fun cleanUtr_extracts12DigitNumber() {
        val raw = "UTR: 123456789012"
        assertEquals("123456789012", TextCleaners.cleanUtr(raw))

        val standalone = "123456789012"
        assertEquals("123456789012", TextCleaners.cleanUtr(standalone))

        assertNull(TextCleaners.cleanUtr("Short 12345"))
    }

    @Test
    fun cleanDebitAccount_extractsMaskedNumber() {
        val raw = "XXXXXXXX1234"
        assertEquals("XXXXXXXX1234", TextCleaners.cleanDebitAccount(raw))

        val withBank = "Bank XXXXXXXX1234"
        assertEquals("XXXXXXXX1234", TextCleaners.cleanDebitAccount(withBank))
    }

    @Test
    fun extractPayeeSection_keepsAllPayeeDetailsExceptAmount() {
        val raw = """
            Transaction Successful
            01:37 pm on 24 Aug 2026
            Paid to
            MM
            Pharmacy Store
            pharmacyoffline
            @upi
            ₹30
            Transfer Details
            Message
            Payment for order #12345
            PhonePe Transaction ID
            T2400000000000000000001
        """.trimIndent()

        val section = TextCleaners.extractPayeeSection(raw)
        assertEquals(listOf("Pharmacy Store", "pharmacyoffline@upi"), section)
        assertEquals("Payment for order #12345", TextCleaners.extractMessage(raw))
    }
}
