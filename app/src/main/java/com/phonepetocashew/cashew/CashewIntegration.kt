package com.phonepetocashew.cashew

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.phonepetocashew.receipt.TransactionDirection
import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

object CashewIntegration {

    const val CASHEW_PACKAGE_NAME = "com.budget.tracker_app"
    const val CASHEW_BASE_URL = "https://cashewapp.web.app/addTransactionRoute"

    /**
     * Builds the deep link URL with properly encoded query parameters.
     * Expenses are negative (-amount), Incomes are positive (+amount) in Cashew.
     */
    fun buildDeepLinkUrl(
        amount: Double,
        direction: TransactionDirection = TransactionDirection.EXPENSE,
        title: String? = null,
        dateTime: LocalDateTime? = null,
        notes: String? = null
    ): String {
        val signedAmount = if (direction == TransactionDirection.INCOME) abs(amount) else -abs(amount)
        val sb = StringBuilder(CASHEW_BASE_URL)
        sb.append("?amount=").append(signedAmount)

        if (!title.isNullOrBlank()) {
            sb.append("&title=").append(URLEncoder.encode(title, "UTF-8"))
        }

        if (dateTime != null) {
            val formattedDate = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            sb.append("&date=").append(URLEncoder.encode(formattedDate, "UTF-8"))
        }

        if (!notes.isNullOrBlank()) {
            sb.append("&notes=").append(URLEncoder.encode(notes, "UTF-8"))
        }

        if (direction == TransactionDirection.INCOME) {
            sb.append("&income=true")
        } else {
            sb.append("&income=false")
        }

        return sb.toString()
    }

    /**
     * Constructs the Intent to open Cashew's Add Transaction page
     * with pre-filled amount, date/time, and notes.
     */
    fun createAddTransactionIntent(
        amount: Double,
        direction: TransactionDirection = TransactionDirection.EXPENSE,
        title: String? = null,
        dateTime: LocalDateTime? = null,
        notes: String? = null
    ): Intent {
        val url = buildDeepLinkUrl(amount, direction, title, dateTime, notes)
        android.util.Log.d("PhonePeToCashew", "Opening Cashew deep link: $url (direction=$direction, title=$title)")
        return Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            setPackage(CASHEW_PACKAGE_NAME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
    }

    /**
     * Checks if the Cashew app is installed on this device.
     */
    fun isCashewInstalled(context: Context): Boolean {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    CASHEW_PACKAGE_NAME,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(CASHEW_PACKAGE_NAME, 0)
            }
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }
}
