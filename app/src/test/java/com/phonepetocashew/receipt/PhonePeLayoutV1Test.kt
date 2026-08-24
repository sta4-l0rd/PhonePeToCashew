package com.phonepetocashew.receipt

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhonePeLayoutV1Test {

    private val layout = PhonePeLayoutV1()

    @Test
    fun matches_validPhonePeReceiptText() {
        val sampleReceiptText = """
            Transaction Successful
            10:43 am on 23 Aug 2026
            Paid to
            Merchant Store
            merchant@upi
            Transfer Details
            PhonePe Transaction ID
            T2400000000000000000001
            Debited from
            XXXXXXXX1234
            UTR: 123456789012
            Powered by UPI | Bank Name
        """.trimIndent()

        assertTrue(layout.matches(sampleReceiptText))
    }

    @Test
    fun matches_alternativeSuccessfulText() {
        val text = "Payment Successful\nPaid to Merchant\nPhonePe"
        assertTrue(layout.matches(text))
    }

    @Test
    fun matches_nonReceiptText() {
        assertFalse(layout.matches("Hey, how are you? Check out this photo."))
        assertFalse(layout.matches("Google Maps navigation to destination"))
        assertFalse(layout.matches(""))
    }

    @Test
    fun getRegions_coversAllEssentialFields() {
        val regions = layout.getRegions()
        assertTrue(regions.containsKey(ReceiptField.AMOUNT))
        assertTrue(regions.containsKey(ReceiptField.DATETIME))
        assertTrue(regions.containsKey(ReceiptField.MERCHANT))
        assertTrue(regions.containsKey(ReceiptField.UPI_ID))
        assertTrue(regions.containsKey(ReceiptField.TXN_ID))
        assertTrue(regions.containsKey(ReceiptField.UTR))
        assertTrue(regions.containsKey(ReceiptField.DEBIT_ACCOUNT))
    }
}
