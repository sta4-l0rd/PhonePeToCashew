package com.phonepetocashew.receipt

object AmountParser {

    // Regex for amounts prefixed with currency symbols or words, including OCR misreads (e.g. "7 30", "?30", "₹30")
    private val CURRENCY_AMOUNT_REGEX = Regex(
        """(?:[₹\u20B9?]|Rs\.?|INR|\bRs\b|\bRe\b|(?:\b7\s+)|(?:[>~*^|]\s*))\s*([0-9]{1,3}(?:,[0-9]{2,3})+(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)""",
        RegexOption.IGNORE_CASE
    )

    // Regex for standalone decimal amounts (e.g. "347.00" or "1,234.50")
    private val DECIMAL_AMOUNT_REGEX = Regex(
        """\b([0-9]{1,3}(?:,[0-9]{2,3})+\.[0-9]{2}|[0-9]+\.[0-9]{2})\b"""
    )

    // Regex for simple integer amounts on a dedicated line (e.g., "30" or "347")
    private val STANDALONE_NUMBER_REGEX = Regex(
        """^\s*([0-9]{1,3}(?:,[0-9]{2,3})+|[0-9]{1,7})\s*$"""
    )

    /**
     * Parses an amount from OCR text.
     * Returns a positive Double or null if no valid amount could be extracted.
     */
    fun parse(text: String): Double? {
        if (text.isBlank()) return null

        val cleanText = text.trim()
        val singleLineText = cleanText.replace(Regex("""[\r\n]+"""), " ")

        // 1. Try matching with explicit currency symbol (₹, Rs, INR) or OCR misread symbols ("7 50", "?50")
        val currencyMatch = CURRENCY_AMOUNT_REGEX.find(singleLineText)
        if (currencyMatch != null) {
            val amountStr = currencyMatch.groupValues[1].replace(",", "")
            val parsed = amountStr.toDoubleOrNull()
            if (parsed != null && parsed > 0) {
                return parsed
            }
        }

        // 2. Try matching decimal format "XXX.XX"
        val decimalMatch = DECIMAL_AMOUNT_REGEX.find(singleLineText)
        if (decimalMatch != null) {
            val amountStr = decimalMatch.groupValues[1].replace(",", "")
            val parsed = amountStr.toDoubleOrNull()
            if (parsed != null && parsed > 0) {
                return parsed
            }
        }

        // 3. Line-by-line fallback for cropped region
        val lines = cleanText.lines().map { it.trim() }.filter { it.isNotBlank() }

        // Filter out single-character artifact lines like "7", "?", "₹", ">" if followed by valid numbers
        val candidateLines = if (lines.size > 1 && (lines[0] in listOf("7", "?", "₹", ">", "*", "Rs", "INR", "F", "z"))) {
            lines.drop(1)
        } else {
            lines
        }

        for (line in candidateLines) {
            // Ignore lines that look like UTR (12+ digits) or Transaction ID
            if (line.length > 9 && !line.contains(".")) continue
            if (line.startsWith("T") || line.contains("UTR", ignoreCase = true)) continue

            // Check if line matches currency prefix first
            val lineCurrencyMatch = CURRENCY_AMOUNT_REGEX.find(line)
            if (lineCurrencyMatch != null) {
                val amountStr = lineCurrencyMatch.groupValues[1].replace(",", "")
                val parsed = amountStr.toDoubleOrNull()
                if (parsed != null && parsed > 0) {
                    return parsed
                }
            }

            val standaloneMatch = STANDALONE_NUMBER_REGEX.find(line)
            if (standaloneMatch != null) {
                val amountStr = standaloneMatch.groupValues[1].replace(",", "")
                val parsed = amountStr.toDoubleOrNull()
                if (parsed != null && parsed > 0 && parsed < 100_000_000) {
                    return parsed
                }
            }
        }

        return null
    }
}
