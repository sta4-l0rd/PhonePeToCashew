package com.phonepetocashew.receipt

import android.graphics.Bitmap
import com.phonepetocashew.ocr.IOcrEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class ReceiptProcessorTest {

    private class FakeOcrEngine(
        val fullText: String,
        val regionTexts: Map<ReceiptField, String> = emptyMap()
    ) : IOcrEngine {
        override suspend fun recognizeFullImage(bitmap: Bitmap): String = fullText

        override suspend fun recognizeRegion(bitmap: Bitmap, region: ReceiptRegion): String {
            return regionTexts.entries.firstOrNull { it.value.isNotBlank() }?.value ?: ""
        }
    }

    @Test
    fun process_validReceipt_extractsAllData() = runBlocking {
        val sampleFullText = """
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
            ₹30
        """.trimIndent()

        val mockBitmap = org.mockito.Mockito.mock(Bitmap::class.java)
        val ocr = FakeOcrEngine(fullText = sampleFullText)
        val processor = ReceiptProcessor(ocr)

        val result = processor.process(mockBitmap)
        assertTrue(result is ProcessResult.Success)

        val success = (result as ProcessResult.Success).result
        assertEquals(30.0, success.amount ?: 0.0, 0.001)
        assertEquals(TransactionDirection.EXPENSE, success.direction)
        assertEquals(LocalDateTime.of(2026, 8, 23, 10, 43), success.dateTime)
        assertEquals("Merchant Store", success.merchant)
        assertEquals("merchant@upi", success.upiId)
        assertEquals("T2400000000000000000001", success.txnId)
        assertEquals("123456789012", success.utr)
        assertEquals("XXXXXXXX1234", success.debitAccount)
    }

    @Test
    fun process_receivedFromReceipt_extractsIncomeDirection() = runBlocking {
        val incomeFullText = """
            Transaction Successful
            08:12 pm on 20 Aug 2026
            Received from
            Sender Name
            sender@upi
            ₹100
            Transfer Details
            PhonePe Transaction ID
            T2400000000000000000002
            Credited to
            XXXXXXXX5678
            ₹100
            UTR: 987654321098
        """.trimIndent()

        val mockBitmap = org.mockito.Mockito.mock(Bitmap::class.java)
        val ocr = FakeOcrEngine(fullText = incomeFullText)
        val processor = ReceiptProcessor(ocr)

        val result = processor.process(mockBitmap)
        assertTrue(result is ProcessResult.Success)

        val success = (result as ProcessResult.Success).result
        assertEquals(100.0, success.amount ?: 0.0, 0.001)
        assertEquals(TransactionDirection.INCOME, success.direction)
        assertEquals(LocalDateTime.of(2026, 8, 20, 20, 12), success.dateTime)
        assertEquals("Sender Name", success.merchant)
        assertEquals("sender@upi", success.upiId)
        assertEquals("T2400000000000000000002", success.txnId)
        assertEquals("987654321098", success.utr)
        assertEquals("XXXXXXXX5678", success.debitAccount)
        assertTrue(success.toNotesString().contains("Received from: Sender Name"))
        assertTrue(success.toNotesString().contains("Credited to: XXXXXXXX5678"))
    }

    @Test
    fun process_payeeWithSubtitleAndMessage_extractsAllNotes() = runBlocking {
        val sampleText = """
            Transaction Successful
            01:37 pm on 24 Aug 2026
            Paid to
            Pharmacy Store
            pharmacyconsumer
            @upi
            ₹30
            Transfer Details
            Message
            Payment for order #12345
            PhonePe Transaction ID
            T2400000000000000000003
            Debited from
            XXXXXXXX2873
            ₹30
            UTR: 959824705188
        """.trimIndent()

        val mockBitmap = org.mockito.Mockito.mock(Bitmap::class.java)
        val ocr = FakeOcrEngine(fullText = sampleText)
        val processor = ReceiptProcessor(ocr)

        val result = processor.process(mockBitmap)
        assertTrue(result is ProcessResult.Success)

        val success = (result as ProcessResult.Success).result
        assertEquals(30.0, success.amount ?: 0.0, 0.001)
        assertEquals("Pharmacy Store", success.merchant)
        assertEquals("pharmacyconsumer@upi", success.upiId)
        assertEquals("Payment for order #12345", success.message)
        val notes = success.toNotesString()
        assertTrue(notes.contains("Paid to: Pharmacy Store"))
        assertTrue(notes.contains("UPI: pharmacyconsumer@upi"))
        assertTrue(notes.contains("Message: Payment for order #12345"))
        assertTrue(notes.contains("PhonePe Txn ID: T2400000000000000000003"))
        assertTrue(notes.contains("UTR: 959824705188"))
        assertTrue(notes.contains("Debited from: XXXXXXXX2873"))
    }

    @Test
    fun process_nonReceipt_returnsNotAReceipt() = runBlocking {
        val mockBitmap = org.mockito.Mockito.mock(Bitmap::class.java)
        val ocr = FakeOcrEngine(fullText = "A random photo of a landscape")
        val processor = ReceiptProcessor(ocr)

        val result = processor.process(mockBitmap)
        assertTrue(result is ProcessResult.NotAReceipt)
    }
}
