package com.example.myapplication.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.model.Transaction
import com.example.myapplication.ui.theme.Black
import com.example.myapplication.ui.theme.White
import com.example.myapplication.ui.viewmodel.TransactionViewModel
import com.example.myapplication.ui.viewmodel.ViewModelFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// ─── Color palette ───────────────────────────────────────────────
private val GradientStart  = Color(0xFF1A1A2E)
private val GradientMid    = Color(0xFF16213E)
private val GradientEnd    = Color(0xFF0F3460)
private val AccentGreen    = Color(0xFF4ADE80)
private val AccentRed      = Color(0xFFFF6B6B)
private val CardBg         = Color(0xFF1E2A45)
private val SurfaceLight   = Color(0xFFF8FAFF)
private val TextSecondary  = Color(0xFF94A3B8)

@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    viewModel: TransactionViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSetIncomeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.fetchAll() }

    // Show set-income dialog if balance is not yet set
    if (showSetIncomeDialog) {
        SetIncomeDialog(
            isLoading = uiState.isSettingIncome,
            onDismiss = { showSetIncomeDialog = false },
            onConfirm = { amount ->
                viewModel.setMonthlyIncome(amount)
                showSetIncomeDialog = false
            }
        )
    }

    // Auto-dismiss error snack
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SurfaceLight
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {

            // ── 1. Wallet Header Card ──────────────────────────────────
            item {
                WalletHeaderCard(
                    totalIncome   = uiState.monthlyFinance?.total_income ?: 0.0,
                    totalExpense  = uiState.monthlyFinance?.total_expense ?: 0.0,
                    balance       = uiState.monthlyFinance?.balance ?: 0.0,
                    isLoading     = uiState.isMonthlyLoading,
                    onProfileClick = onNavigateToProfile
                )
            }

            // ── 2. Action Buttons ──────────────────────────────────────
            item {
                ActionButtonsRow(
                    onAddClick       = onNavigateToAddTransaction,
                    onSetIncomeClick = { showSetIncomeDialog = true }
                )
            }

            // ── 3. Transactions Header ─────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Transactions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )
                    Text(
                        "View all",
                        fontSize = 13.sp,
                        color = AccentGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ── 4. Transaction List ────────────────────────────────────
            if (uiState.isLoading && uiState.transactions.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GradientEnd)
                    }
                }
            } else if (uiState.transactions.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("💸", fontSize = 40.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("No transactions yet", color = TextSecondary, fontSize = 15.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("Tap + Add to record your first entry", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            } else {
                items(uiState.transactions) { tx ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 }
                    ) {
                        TransactionItem(transaction = tx)
                    }
                }
            }
        }
    }
}

// ─── Wallet Header ────────────────────────────────────────────────────────────
@Composable
fun WalletHeaderCard(
    totalIncome: Double,
    totalExpense: Double,
    balance: Double,
    isLoading: Boolean,
    onProfileClick: () -> Unit
) {
    val fmt = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    val now = Calendar.getInstance()
    val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(now.time)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientMid, GradientEnd)
                )
            )
            .padding(bottom = 32.dp)
    ) {
        Column {
            // ── Top bar ───────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onProfileClick) {
                    Icon(Icons.Default.Person, contentDescription = "Profile", tint = White)
                }
                Text(
                    "My Cashflow",
                    color = White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                // Placeholder for symmetry
                Box(Modifier.size(48.dp))
            }

            // ── Balance display ────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Balance",
                    color = Color(0xFFCBD5E1),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
                if (isLoading) {
                    CircularProgressIndicator(color = White, modifier = Modifier.size(32.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        text = fmt.format(balance),
                        color = White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    monthName,
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Income / Expense chips ─────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FinanceSummaryChip(
                    label = "Income",
                    amount = fmt.format(totalIncome),
                    icon = Icons.Default.ArrowDownward,
                    iconColor = AccentGreen,
                    modifier = Modifier.weight(1f)
                )
                FinanceSummaryChip(
                    label = "Expense",
                    amount = fmt.format(totalExpense),
                    icon = Icons.Default.ArrowUpward,
                    iconColor = AccentRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun FinanceSummaryChip(
    label: String,
    amount: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, color = Color(0xFF94A3B8), fontSize = 11.sp)
            Text(amount, color = White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

// ─── Action Buttons Row ───────────────────────────────────────────────────────
@Composable
fun ActionButtonsRow(
    onAddClick: () -> Unit,
    onSetIncomeClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .offset(y = (-20).dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Add Transaction button
        Button(
            onClick = onAddClick,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Black),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Add", color = White, fontWeight = FontWeight.SemiBold)
        }

        // Set Income button
        Button(
            onClick = onSetIncomeClick,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color(0xFF1A1A2E), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Set Income", fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
        }
    }
}

// ─── Transaction Item ─────────────────────────────────────────────────────────
@Composable
fun TransactionItem(transaction: Transaction) {
    val fmt        = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    val isIncome   = transaction.type.lowercase() == "income"
    val amountColor = if (isIncome) AccentGreen else AccentRed
    val prefix     = if (isIncome) "+" else "–"
    val icon       = if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward
    val iconBg     = if (isIncome) AccentGreen.copy(alpha = 0.12f) else AccentRed.copy(alpha = 0.12f)

    val dateFormatted = try {
        val parsed = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.parse(transaction.date)
        SimpleDateFormat("dd MMM · HH:mm", Locale.getDefault()).format(parsed!!)
    } catch (e: Exception) {
        transaction.date.take(10)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon bubble
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = amountColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    transaction.description,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    dateFormatted,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            Text(
                text = "$prefix${fmt.format(transaction.amount)}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}

// ─── Set Income Dialog ────────────────────────────────────────────────────────
@Composable
fun SetIncomeDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val amount = inputText.toDoubleOrNull()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Set Monthly Income",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Set your income for this month. Expenses will be validated against it.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Spacer(Modifier.height(20.dp))

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("Income Amount (Rp)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GradientEnd,
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    ),
                    prefix = { Text("Rp ") }
                )

                Spacer(Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Cancel") }

                    Button(
                        onClick = { amount?.let { onConfirm(it) } },
                        modifier = Modifier.weight(1f),
                        enabled = amount != null && amount > 0 && !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GradientEnd)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save", color = White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
