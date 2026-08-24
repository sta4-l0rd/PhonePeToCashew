package com.phonepetocashew.receipt

/**
 * Layout parser for PhonePe receipt layout (V1 - Standard Dark/Light theme).
 */
class PhonePeLayoutV1 : ReceiptLayoutParser {
    override val name: String = "PhonePe Layout V1"

    override fun matches(fullOcrText: String): Boolean {
        val normalized = fullOcrText.lowercase()
        val hasSuccessMarker = normalized.contains("transaction successful") ||
                normalized.contains("payment successful") ||
                normalized.contains("successful")
        val hasPhonePeMarker = normalized.contains("phonepe") ||
                normalized.contains("paid to") ||
                normalized.contains("received from") ||
                normalized.contains("transfer details") ||
                normalized.contains("utr") ||
                normalized.contains("debited from") ||
                normalized.contains("credited to")

        return hasSuccessMarker && hasPhonePeMarker
    }

    override fun getRegions(): Map<ReceiptField, ReceiptRegion> {
        return mapOf(
            ReceiptField.DATETIME to ReceiptRegion(
                left = 0.12f,
                top = 0.03f,
                right = 0.95f,
                bottom = 0.14f
            ),
            ReceiptField.AMOUNT to ReceiptRegion(
                left = 0.55f,
                top = 0.18f,
                right = 0.98f,
                bottom = 0.35f
            ),
            ReceiptField.MERCHANT to ReceiptRegion(
                left = 0.18f,
                top = 0.19f,
                right = 0.70f,
                bottom = 0.28f
            ),
            ReceiptField.UPI_ID to ReceiptRegion(
                left = 0.18f,
                top = 0.26f,
                right = 0.75f,
                bottom = 0.36f
            ),
            ReceiptField.TXN_ID to ReceiptRegion(
                left = 0.05f,
                top = 0.48f,
                right = 0.95f,
                bottom = 0.60f
            ),
            ReceiptField.DEBIT_ACCOUNT to ReceiptRegion(
                left = 0.16f,
                top = 0.62f,
                right = 0.75f,
                bottom = 0.73f
            ),
            ReceiptField.UTR to ReceiptRegion(
                left = 0.16f,
                top = 0.69f,
                right = 0.90f,
                bottom = 0.82f
            )
        )
    }
}
