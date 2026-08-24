package com.phonepetocashew.receipt

object TextCleaners {

    private val UPI_REGEX = Regex("""([a-zA-Z0-9.\-_]{2,}@[a-zA-Z0-9]{2,})""")
    private val PHONEPE_TXN_ID_REGEX = Regex("""\b(T[0-9]{15,30})\b""")
    private val UTR_REGEX = Regex("""(?:UTR[:\s]+)?\b([0-9]{12})\b""")
    private val MASKED_ACCOUNT_REGEX = Regex("""\b([0-9]*X{2,}[0-9X]*)\b""", RegexOption.IGNORE_CASE)

    /**
     * Extracts all text lines in the Payee section (between "Paid to"/"Received from"
     * and the grey divider / "Transfer Details"), filtering out amount and avatar artifacts.
     */
    fun extractPayeeSection(fullText: String): List<String> {
        val rawLines = fullText.lines().map { it.trim() }.filter { it.isNotBlank() }
        val payeeLines = mutableListOf<String>()
        var recording = false

        for (line in rawLines) {
            val lower = line.lowercase()
            if (!recording) {
                if (lower.startsWith("paid to") || lower.startsWith("received from") ||
                    lower == "paid to" || lower == "received from"
                ) {
                    recording = true
                    val afterHeader = line.replace(Regex("""^(?i)(?:paid to|received from)[:\s]*"""), "").trim()
                    if (afterHeader.isNotBlank()) {
                        processPayeeLine(afterHeader, payeeLines)
                    }
                }
            } else {
                // Stop at the grey divider section
                if (lower.startsWith("transfer details") ||
                    lower.startsWith("phonepe transaction id") ||
                    lower.startsWith("debited from") ||
                    lower.startsWith("credited to") ||
                    lower.startsWith("message") ||
                    lower.startsWith("utr")
                ) {
                    break
                }

                processPayeeLine(line, payeeLines)
            }
        }

        return payeeLines
    }

    private fun processPayeeLine(line: String, payeeLines: MutableList<String>) {
        val cleaned = line.replace(Regex("""[|><•#~]"""), "").trim()
        if (cleaned.isBlank()) return

        // Skip amount lines (e.g. ₹30, Rs 30, 30.00, 7 30, > 30)
        if (isAmountLine(cleaned)) return

        // Skip avatar initials (e.g. "RK", "MM")
        if (cleaned.length <= 2 && cleaned.all { it.isLetter() && it.isUpperCase() }) return

        // Skip headers & timestamps
        if (cleaned.contains("Successful", ignoreCase = true)) return
        if (cleaned.contains("am on", ignoreCase = true) || cleaned.contains("pm on", ignoreCase = true)) return

        // Handle handles split across lines (e.g. "user" followed by "@ybl")
        if (cleaned.startsWith("@") && payeeLines.isNotEmpty()) {
            val lastIdx = payeeLines.size - 1
            val prev = payeeLines[lastIdx]
            if (!prev.contains("@")) {
                payeeLines[lastIdx] = "$prev$cleaned"
                return
            }
        }

        if (payeeLines.isNotEmpty() && payeeLines.last().endsWith("@")) {
            val lastIdx = payeeLines.size - 1
            payeeLines[lastIdx] = "${payeeLines[lastIdx]}$cleaned"
            return
        }

        payeeLines.add(cleaned)
    }

    private fun isAmountLine(line: String): Boolean {
        if (line.startsWith("₹") || line.startsWith("Rs") || line.startsWith("INR")) return true
        if (line.matches(Regex("""^(?:[₹?]|7\s+|>|\s)*[0-9]{1,3}(?:,[0-9]{2,3})*(?:\.[0-9]{1,2})?$"""))) return true
        if (line.matches(Regex("""^[0-9]+(?:\.[0-9]{1,2})?$"""))) return true
        return false
    }

    /**
     * Cleans merchant name while preserving the actual business/recipient name.
     */
    fun cleanMerchant(raw: String): String? {
        val payeeSection = extractPayeeSection(raw)
        if (payeeSection.isNotEmpty()) {
            return payeeSection.firstOrNull { !it.contains("@") } ?: payeeSection.first()
        }

        if (raw.isBlank()) return null
        val lines = raw.lines().map { it.trim() }.filter { it.isNotBlank() }

        for (line in lines) {
            // Skip headers or UPI IDs or amounts
            if (line.contains("Successful", ignoreCase = true)) continue
            if (line.contains("Paid to", ignoreCase = true)) continue
            if (line.contains("Received from", ignoreCase = true)) continue
            if (line.contains("Transfer Details", ignoreCase = true)) continue
            if (line.contains("Debited from", ignoreCase = true)) continue
            if (line.contains("Credited to", ignoreCase = true)) continue
            if (line.contains("Transaction ID", ignoreCase = true)) continue
            if (line.contains("PhonePe", ignoreCase = true)) continue
            if (line.contains("@")) continue
            if (line.contains("am on", ignoreCase = true) || line.contains("pm on", ignoreCase = true)) continue
            if (isAmountLine(line)) continue
            if (line.length <= 2 && line.all { it.isLetter() && it.isUpperCase() }) continue

            val cleaned = line.replace(Regex("""[|><•#~]"""), "").trim()
            if (cleaned.length >= 2) {
                return cleaned
            }
        }
        return null
    }

    /**
     * Extracts UPI Virtual Payment Address (e.g. "merchant@upi").
     */
    fun cleanUpiId(raw: String): String? {
        val direct = UPI_REGEX.find(raw)?.groupValues?.get(1)
        if (direct != null) return direct

        val payee = extractPayeeSection(raw)
        return payee.firstOrNull { it.contains("@") }
    }

    /**
     * Extracts optional message text from Transfer Details.
     */
    fun extractMessage(fullText: String): String? {
        val rawLines = fullText.lines().map { it.trim() }.filter { it.isNotBlank() }
        val messageLines = mutableListOf<String>()
        var inMessage = false

        for (line in rawLines) {
            val lower = line.lowercase()
            if (!inMessage) {
                if (lower == "message" || lower.startsWith("message:")) {
                    inMessage = true
                    val after = line.replace(Regex("""^(?i)message[:\s]*"""), "").trim()
                    if (after.isNotBlank()) {
                        messageLines.add(after)
                    }
                }
            } else {
                if (lower.startsWith("phonepe transaction id") ||
                    lower.startsWith("debited from") ||
                    lower.startsWith("credited to") ||
                    lower.startsWith("utr") ||
                    lower.startsWith("transfer details")
                ) {
                    break
                }
                messageLines.add(line)
            }
        }

        return if (messageLines.isNotEmpty()) messageLines.joinToString(" ") else null
    }

    /**
     * Extracts PhonePe Transaction ID (e.g. "T2400000000000000000001").
     */
    fun cleanTxnId(raw: String): String? {
        val directMatch = PHONEPE_TXN_ID_REGEX.find(raw)
        if (directMatch != null) return directMatch.groupValues[1]

        // Fallback: look for alphanumeric string after "Transaction ID"
        for (line in raw.lines()) {
            val trimmed = line.trim()
            if (trimmed.startsWith("T") && trimmed.length >= 16) {
                return trimmed
            }
        }
        return null
    }

    /**
     * Extracts 12-digit UTR number.
     */
    fun cleanUtr(raw: String): String? {
        return UTR_REGEX.find(raw)?.groupValues?.get(1)
    }

    /**
     * Extracts debited or credited account information (e.g. "XXXXXXXX1234").
     */
    fun cleanDebitAccount(raw: String): String? {
        val match = MASKED_ACCOUNT_REGEX.find(raw)
        if (match != null) return match.groupValues[1]

        val lines = raw.lines().map { it.trim() }
        val debitedIndex = lines.indexOfFirst {
            it.contains("Debited from", ignoreCase = true) || it.contains("Credited to", ignoreCase = true)
        }
        if (debitedIndex != -1 && debitedIndex + 1 < lines.size) {
            val candidate = lines[debitedIndex + 1]
            if (candidate.matches(Regex("""[0-9X]{4,24}"""))) {
                return candidate
            }
        }
        return null
    }
}
