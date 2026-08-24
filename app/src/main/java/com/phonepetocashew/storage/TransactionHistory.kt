package com.phonepetocashew.storage

import android.content.Context
import android.content.SharedPreferences

/**
 * Tracks forwarded PhonePe Transaction IDs to warn user about duplicate submissions.
 */
class TransactionHistory(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun isDuplicate(txnId: String?): Boolean {
        if (txnId.isNullOrBlank()) return false
        val sentIds = getSentIds()
        return sentIds.contains(txnId.trim())
    }

    fun recordTransaction(txnId: String?) {
        if (txnId.isNullOrBlank()) return
        val sentIds = getSentIds().toMutableSet()
        sentIds.add(txnId.trim())

        // Prune if exceeds max limit
        val pruned = if (sentIds.size > MAX_STORED_IDS) {
            sentIds.toList().takeLast(MAX_STORED_IDS).toSet()
        } else {
            sentIds
        }

        prefs.edit().putStringSet(KEY_SENT_TXN_IDS, pruned).apply()
    }

    private fun getSentIds(): Set<String> {
        return prefs.getStringSet(KEY_SENT_TXN_IDS, emptySet()) ?: emptySet()
    }

    companion object {
        private const val PREFS_NAME = "phonepe_cashew_history"
        private const val KEY_SENT_TXN_IDS = "sent_txn_ids"
        private const val MAX_STORED_IDS = 500
    }
}
