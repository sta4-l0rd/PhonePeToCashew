package com.phonepetocashew.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phonepetocashew.receipt.ReceiptResult
import com.phonepetocashew.receipt.TransactionDirection
import com.phonepetocashew.ui.theme.AccentGreen
import com.phonepetocashew.ui.theme.DarkBackground
import com.phonepetocashew.ui.theme.DarkSurface
import com.phonepetocashew.ui.theme.DarkSurfaceVariant
import com.phonepetocashew.ui.theme.ErrorRed
import com.phonepetocashew.ui.theme.PhonePePurple
import com.phonepetocashew.ui.theme.PhonePePurpleLight
import com.phonepetocashew.ui.theme.TextPrimary
import com.phonepetocashew.ui.theme.TextSecondary
import com.phonepetocashew.ui.theme.WarningAmber
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

sealed class ScreenUiState {
    object Idle : ScreenUiState()
    data class Loading(val message: String = "Analyzing PhonePe receipt...") : ScreenUiState()
    data class Error(val title: String, val message: String) : ScreenUiState()
    object CashewNotInstalled : ScreenUiState()
    data class Ready(
        val bitmap: Bitmap?,
        val initialResult: ReceiptResult,
        val isDuplicate: Boolean
    ) : ScreenUiState()
}

@Composable
fun ReceiptConfirmationScreen(
    state: ScreenUiState,
    onAddToCashew: (amount: Double, direction: TransactionDirection, title: String?, dateTime: LocalDateTime?, notes: String, txnId: String?) -> Unit,
    onDismiss: () -> Unit
) {
    Scaffold(
        containerColor = DarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                is ScreenUiState.Idle -> {
                    Box(modifier = Modifier.padding(20.dp)) {
                        IdleView()
                    }
                }
                is ScreenUiState.Loading -> {
                    Box(modifier = Modifier.padding(20.dp)) {
                        LoadingView(message = state.message)
                    }
                }
                is ScreenUiState.Error -> {
                    Box(modifier = Modifier.padding(20.dp)) {
                        ErrorView(
                            title = state.title,
                            message = state.message,
                            onDismiss = onDismiss
                        )
                    }
                }
                is ScreenUiState.CashewNotInstalled -> {
                    Box(modifier = Modifier.padding(20.dp)) {
                        CashewNotInstalledView(onDismiss = onDismiss)
                    }
                }
                is ScreenUiState.Ready -> {
                    ReadyView(
                        state = state,
                        onAddToCashew = onAddToCashew,
                        onCancel = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun IdleView() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(24.dp)
    ) {
        Icon(
            imageVector = Icons.Default.AccountBalanceWallet,
            contentDescription = null,
            tint = PhonePePurpleLight,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Add to Cashew",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Share a PhonePe payment receipt screenshot to automatically forward the transaction to Cashew.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun LoadingView(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = PhonePePurpleLight,
            modifier = Modifier.size(52.dp),
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Running on-device OCR...",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun ErrorView(title: String, message: String, onDismiss: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = ErrorRed,
            modifier = Modifier.size(60.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
        ) {
            Text("Close", color = TextPrimary)
        }
    }
}

@Composable
private fun CashewNotInstalledView(onDismiss: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = WarningAmber,
            modifier = Modifier.size(60.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Cashew Not Installed",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Please install Cashew (Budget & Expense Tracker) to automatically record transactions.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = PhonePePurple)
        ) {
            Text("Close", color = TextPrimary)
        }
    }
}

@Composable
private fun ReadyView(
    state: ScreenUiState.Ready,
    onAddToCashew: (Double, TransactionDirection, String?, LocalDateTime?, String, String?) -> Unit,
    onCancel: () -> Unit
) {
    val scrollState = rememberScrollState()

    var direction by remember {
        mutableStateOf(state.initialResult.direction)
    }

    var amountText by remember {
        mutableStateOf(state.initialResult.amount?.let { "%.2f".format(it) } ?: "")
    }
    var notesText by remember {
        mutableStateOf(state.initialResult.toNotesString())
    }
    val dateTime = state.initialResult.dateTime

    val formattedDate = remember(dateTime) {
        dateTime?.format(DateTimeFormatter.ofPattern("d MMM yyyy, hh:mm a"))
            ?: "Current date & time"
    }

    val parsedAmount = amountText.toDoubleOrNull()
    val isAmountValid = parsedAmount != null && parsedAmount > 0

    val isIncome = direction == TransactionDirection.INCOME

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Scrollable content area
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "PhonePe Receipt",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Review extracted details before opening Cashew",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                if (state.bitmap != null) {
                    Image(
                        bitmap = state.bitmap.asImageBitmap(),
                        contentDescription = "Receipt Thumbnail",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(8.dp))
                    )
                }
            }

            // Transaction Type Segmented Pill (Expense vs Income)
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "TRANSACTION TYPE",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceVariant)
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Expense Tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (!isIncome) PhonePePurple else DarkSurfaceVariant)
                                .clickable {
                                    if (direction != TransactionDirection.EXPENSE) {
                                        direction = TransactionDirection.EXPENSE
                                        notesText = state.initialResult.copy(direction = TransactionDirection.EXPENSE).toNotesString()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.CallMade,
                                    contentDescription = null,
                                    tint = if (!isIncome) TextPrimary else TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Expense",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (!isIncome) FontWeight.Bold else FontWeight.Medium,
                                    color = if (!isIncome) TextPrimary else TextSecondary
                                )
                            }
                        }

                        // Income Tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isIncome) AccentGreen else DarkSurfaceVariant)
                                .clickable {
                                    if (direction != TransactionDirection.INCOME) {
                                        direction = TransactionDirection.INCOME
                                        notesText = state.initialResult.copy(direction = TransactionDirection.INCOME).toNotesString()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.CallReceived,
                                    contentDescription = null,
                                    tint = if (isIncome) DarkBackground else TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Income",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isIncome) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isIncome) DarkBackground else TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Duplicate Warning Banner
            if (state.isDuplicate) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "This receipt was already sent to Cashew previously.",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarningAmber
                        )
                    }
                }
            }

            // Amount Input Card
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isIncome) "AMOUNT (INCOME)" else "AMOUNT (EXPENSE)",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isIncome) AccentGreen else TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        leadingIcon = {
                            Text(
                                text = "₹",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isIncome) AccentGreen else PhonePePurpleLight,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        },
                        placeholder = { Text("0.00", color = TextSecondary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = !isAmountValid,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isIncome) AccentGreen else PhonePePurpleLight,
                            unfocusedBorderColor = DarkSurfaceVariant,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    if (!isAmountValid) {
                        Text(
                            text = "Please enter a valid amount greater than 0",
                            color = ErrorRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Date & Time Card
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = if (isIncome) AccentGreen else PhonePePurpleLight,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "DATE & TIME",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Notes Preview Card (Sent to Cashew notes) - Scrollable text area
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Notes,
                            contentDescription = null,
                            tint = if (isIncome) AccentGreen else PhonePePurpleLight,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RECEIPT DETAILS (CASHEW NOTES)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(115.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isIncome) AccentGreen else PhonePePurpleLight,
                            unfocusedBorderColor = DarkSurfaceVariant,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    Text(
                        text = "Title and Category will be selected by you in Cashew.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Fixed Pinned Bottom Action Bar (always visible!)
        Surface(
            color = DarkBackground,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = {
                        if (parsedAmount != null && isAmountValid) {
                            onAddToCashew(
                                parsedAmount,
                                direction,
                                state.initialResult.merchant,
                                dateTime,
                                notesText,
                                state.initialResult.txnId
                            )
                        }
                    },
                    enabled = isAmountValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isIncome) AccentGreen else PhonePePurple,
                        disabledContainerColor = DarkSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isAmountValid) {
                            if (isIncome) DarkBackground else TextPrimary
                        } else TextSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isIncome) "Add Income to Cashew" else "Add Expense to Cashew",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isAmountValid) {
                            if (isIncome) DarkBackground else TextPrimary
                        } else TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
