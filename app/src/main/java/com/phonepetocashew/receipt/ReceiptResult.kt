package com.phonepetocashew.receipt

import java.time.LocalDateTime

enum class TransactionDirection {
    EXPENSE,
    INCOME
}

/**
 * Result of parsing a receipt.
 */
data class ReceiptResult(
    val amount: Double? = null,
    val dateTime: LocalDateTime? = null,
    val merchant: String? = null,
    val upiId: String? = null,
    val payeeDetails: List<String> = emptyList(),
    val message: String? = null,
    val txnId: String? = null,
    val utr: String? = null,
    val debitAccount: String? = null,
    val direction: TransactionDirection = TransactionDirection.EXPENSE,
    val rawTexts: Map<ReceiptField, String> = emptyMap()
) {
    val isValidAmount: Boolean
        get() = amount != null && amount > 0.0

    /**
     * Formats the non-amount transaction metadata into structured Cashew notes.
     */
    fun toNotesString(): String {
        val lines = mutableListOf<String>()
        val personLabel = if (direction == TransactionDirection.INCOME) "Received from" else "Paid to"

        if (payeeDetails.isNotEmpty()) {
            val firstLine = payeeDetails.first()
            lines.add("$personLabel: $firstLine")
            for (i in 1 until payeeDetails.size) {
                val detail = payeeDetails[i]
                if (detail.contains("@") && !detail.startsWith("UPI", ignoreCase = true)) {
                    lines.add("UPI: $detail")
                } else {
                    lines.add(detail)
                }
            }
        } else {
            merchant?.takeIf { it.isNotBlank() }?.let { lines.add("$personLabel: $it") }
            upiId?.takeIf { it.isNotBlank() }?.let { lines.add("UPI: $it") }
        }

        message?.takeIf { it.isNotBlank() }?.let { lines.add("Message: $it") }
        txnId?.takeIf { it.isNotBlank() }?.let { lines.add("PhonePe Txn ID: $it") }
        utr?.takeIf { it.isNotBlank() }?.let { lines.add("UTR: $it") }
        val accountLabel = if (direction == TransactionDirection.INCOME) "Credited to" else "Debited from"
        debitAccount?.takeIf { it.isNotBlank() }?.let { lines.add("$accountLabel: $it") }
        return lines.joinToString("\n")
    }
}
