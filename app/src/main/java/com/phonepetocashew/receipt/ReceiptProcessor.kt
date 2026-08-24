package com.phonepetocashew.receipt

import android.graphics.Bitmap
import com.phonepetocashew.ocr.IOcrEngine

sealed class ProcessResult {
    data class Success(val result: ReceiptResult) : ProcessResult()
    data class AmountMissing(val result: ReceiptResult, val message: String) : ProcessResult()
    data class NotAReceipt(val reason: String) : ProcessResult()
    data class Error(val exception: Throwable) : ProcessResult()
}

class ReceiptProcessor(
    private val ocrEngine: IOcrEngine,
    private val layouts: List<ReceiptLayoutParser> = listOf(PhonePeLayoutV1())
) {

    suspend fun process(bitmap: Bitmap): ProcessResult {
        return try {
            // 1. Run full-image OCR for receipt layout detection
            val fullText = ocrEngine.recognizeFullImage(bitmap)

            // 2. Identify the receipt layout
            val matchedLayout = layouts.firstOrNull { it.matches(fullText) }
                ?: return ProcessResult.NotAReceipt("This image does not appear to be a supported PhonePe receipt.")

            // 3. OCR on each defined region
            val regions = matchedLayout.getRegions()
            val rawTexts = mutableMapOf<ReceiptField, String>()

            for ((field, region) in regions) {
                val regionText = ocrEngine.recognizeRegion(bitmap, region)
                rawTexts[field] = regionText
            }

            // 4. Parse fields with region-first, fullText-fallback strategy
            val amount = AmountParser.parse(rawTexts[ReceiptField.AMOUNT] ?: "")
                ?: AmountParser.parse(fullText)

            val dateTime = DateTimeParser.parse(rawTexts[ReceiptField.DATETIME] ?: "")
                ?: DateTimeParser.parse(fullText)

            val payeeDetails = TextCleaners.extractPayeeSection(fullText)
                .ifEmpty { TextCleaners.extractPayeeSection(rawTexts[ReceiptField.MERCHANT] ?: "") }

            val merchant = payeeDetails.firstOrNull { !it.contains("@") }
                ?: TextCleaners.cleanMerchant(rawTexts[ReceiptField.MERCHANT] ?: "")
                ?: TextCleaners.cleanMerchant(fullText)

            val upiId = payeeDetails.firstOrNull { it.contains("@") }
                ?: TextCleaners.cleanUpiId(rawTexts[ReceiptField.UPI_ID] ?: "")
                ?: TextCleaners.cleanUpiId(fullText)

            val message = TextCleaners.extractMessage(fullText)

            val txnId = TextCleaners.cleanTxnId(rawTexts[ReceiptField.TXN_ID] ?: "")
                ?: TextCleaners.cleanTxnId(fullText)

            val utr = TextCleaners.cleanUtr(rawTexts[ReceiptField.UTR] ?: "")
                ?: TextCleaners.cleanUtr(fullText)

            val debitAccount = TextCleaners.cleanDebitAccount(rawTexts[ReceiptField.DEBIT_ACCOUNT] ?: "")
                ?: TextCleaners.cleanDebitAccount(fullText)

            // Determine direction (Received from / Credited to = Income, Paid to / Debited from = Expense)
            val fullTextLower = fullText.lowercase()
            val merchantRegionLower = (rawTexts[ReceiptField.MERCHANT] ?: "").lowercase()
            val debitRegionLower = (rawTexts[ReceiptField.DEBIT_ACCOUNT] ?: "").lowercase()

            val isIncome = fullTextLower.contains("received from") ||
                    fullTextLower.contains("credited to") ||
                    merchantRegionLower.contains("received from") ||
                    debitRegionLower.contains("credited to")

            val direction = if (isIncome) TransactionDirection.INCOME else TransactionDirection.EXPENSE

            val result = ReceiptResult(
                amount = amount,
                dateTime = dateTime,
                merchant = merchant,
                upiId = upiId,
                payeeDetails = payeeDetails,
                message = message,
                txnId = txnId,
                utr = utr,
                debitAccount = debitAccount,
                direction = direction,
                rawTexts = rawTexts
            )

            if (result.isValidAmount) {
                ProcessResult.Success(result)
            } else {
                ProcessResult.AmountMissing(
                    result = result,
                    message = "Could not determine transaction amount from receipt. Please enter manually."
                )
            }
        } catch (t: Throwable) {
            ProcessResult.Error(t)
        }
    }
}
