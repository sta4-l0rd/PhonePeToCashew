package com.phonepetocashew

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.phonepetocashew.cashew.CashewIntegration
import com.phonepetocashew.ocr.MlKitOcrEngine
import com.phonepetocashew.receipt.ProcessResult
import com.phonepetocashew.receipt.ReceiptProcessor
import com.phonepetocashew.storage.TransactionHistory
import com.phonepetocashew.ui.ReceiptConfirmationScreen
import com.phonepetocashew.ui.ScreenUiState
import com.phonepetocashew.ui.theme.PhonePeToCashewTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {

    private var uiState by mutableStateOf<ScreenUiState>(ScreenUiState.Idle)
    private val transactionHistory by lazy { TransactionHistory(this) }
    private val ocrEngine by lazy { MlKitOcrEngine() }
    private val receiptProcessor by lazy { ReceiptProcessor(ocrEngine) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PhonePeToCashewTheme {
                ReceiptConfirmationScreen(
                    state = uiState,
                    onAddToCashew = { amount, direction, title, dateTime, notes, txnId ->
                        handleAddTransaction(amount, direction, title, dateTime, notes, txnId)
                    },
                    onDismiss = { finish() }
                )
            }
        }

        handleIncomingIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return

        when (intent.action) {
            Intent.ACTION_SEND -> {
                if (intent.type?.startsWith("image/") == true) {
                    val imageUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                    if (imageUri != null) {
                        processSharedImage(imageUri)
                    } else {
                        uiState = ScreenUiState.Error(
                            title = "Invalid Image",
                            message = "No receipt image was provided in the share intent."
                        )
                    }
                } else {
                    uiState = ScreenUiState.Error(
                        title = "Unsupported Content",
                        message = "Only image receipts are supported."
                    )
                }
            }
            Intent.ACTION_MAIN -> {
                uiState = ScreenUiState.Idle
            }
        }
    }

    private fun processSharedImage(uri: Uri) {
        uiState = ScreenUiState.Loading("Reading receipt image...")

        lifecycleScope.launch {
            val bitmap = loadBitmapFromUri(uri)
            if (bitmap == null) {
                uiState = ScreenUiState.Error(
                    title = "Failed to Load Image",
                    message = "Could not load the shared receipt image."
                )
                return@launch
            }

            uiState = ScreenUiState.Loading("Analyzing PhonePe receipt...")

            when (val result = receiptProcessor.process(bitmap)) {
                is ProcessResult.Success -> {
                    val isDuplicate = transactionHistory.isDuplicate(result.result.txnId)
                    uiState = ScreenUiState.Ready(
                        bitmap = bitmap,
                        initialResult = result.result,
                        isDuplicate = isDuplicate
                    )
                }
                is ProcessResult.AmountMissing -> {
                    val isDuplicate = transactionHistory.isDuplicate(result.result.txnId)
                    uiState = ScreenUiState.Ready(
                        bitmap = bitmap,
                        initialResult = result.result,
                        isDuplicate = isDuplicate
                    )
                }
                is ProcessResult.NotAReceipt -> {
                    uiState = ScreenUiState.Error(
                        title = "Not a PhonePe Receipt",
                        message = result.reason
                    )
                }
                is ProcessResult.Error -> {
                    uiState = ScreenUiState.Error(
                        title = "OCR Failed",
                        message = result.exception.localizedMessage ?: "Unknown error occurred."
                    )
                }
            }
        }
    }

    private suspend fun loadBitmapFromUri(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            contentResolver.openInputStream(uri)?.use { stream: InputStream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun handleAddTransaction(
        amount: Double,
        direction: com.phonepetocashew.receipt.TransactionDirection,
        title: String?,
        dateTime: LocalDateTime?,
        notes: String,
        txnId: String?
    ) {
        val cashewInstalled = CashewIntegration.isCashewInstalled(this)
        val cashewIntent = CashewIntegration.createAddTransactionIntent(
            amount = amount,
            direction = direction,
            title = title,
            dateTime = dateTime,
            notes = notes
        )

        // Record txn ID to prevent duplicates in future
        txnId?.let { transactionHistory.recordTransaction(it) }

        try {
            startActivity(cashewIntent)
            finish()
        } catch (e: Exception) {
            if (!cashewInstalled) {
                uiState = ScreenUiState.CashewNotInstalled
            } else {
                Toast.makeText(this, "Could not open Cashew: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
