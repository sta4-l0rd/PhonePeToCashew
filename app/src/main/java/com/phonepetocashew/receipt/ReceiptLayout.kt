package com.phonepetocashew.receipt

/**
 * Fields extractable from a receipt.
 */
enum class ReceiptField {
    AMOUNT,         // "₹30" - parsed to Double
    DATETIME,       // "10:43 am on 23 Aug 2026" - parsed to LocalDateTime
    MERCHANT,       // "Merchant Name" - payee name
    UPI_ID,         // "merchant@upi" - UPI VPA
    TXN_ID,         // "T2400000000000000000001" - PhonePe Transaction ID
    UTR,            // "123456789012" - Bank reference number
    DEBIT_ACCOUNT   // "XXXXXXXX1234" - Masked account number
}

/**
 * Extensible interface for different receipt layouts.
 */
interface ReceiptLayoutParser {
    val name: String

    /**
     * Checks if the full OCR text matches this receipt layout template.
     */
    fun matches(fullOcrText: String): Boolean

    /**
     * Returns normalized crop regions for each supported field in this layout.
     */
    fun getRegions(): Map<ReceiptField, ReceiptRegion>
}
