@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.appqlchitieu.view

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.appqlchitieu.database.DatabaseProvider
import com.example.appqlchitieu.model.Category
import com.example.appqlchitieu.model.Expense
import com.example.appqlchitieu.model.Wallet
import com.example.appqlchitieu.repository.TransactionRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.example.appqlchitieu.utils.SessionManager
import com.example.appqlchitieu.utils.UserSession

private val FieldShape = RoundedCornerShape(12.dp)
private val FieldBorderColor = Color(0xFFCFE0EB)
private val FieldFocusColor = Color(0xFF79C4F9)
private val FieldContainerWhite = Color.White

@Composable
fun TransactionAddScreen(
    onSaved: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    onManageCategory: () -> Unit = {},
    onManageWallet: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { DatabaseProvider.getDatabase(context) }
    val sessionManager = remember { SessionManager(context) }
    val userSession = remember { UserSession(sessionManager) }
    val userId = userSession.userIdOrNull()

    if (userId == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Bạn chưa đăng nhập")
        }
        return
    }

    val repo = remember { TransactionRepository(db) }
    val dateFmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    var type by remember { mutableStateOf("expense") }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    val categories by db.categoryDao().getCategoriesByType(userId, type).collectAsState(initial = emptyList())
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var expandCategory by remember { mutableStateOf(false) }

    val wallets by db.walletDao().getAllWallets(userId).collectAsState(initial = emptyList())
    var selectedWallet by remember { mutableStateOf<Wallet?>(null) }
    var expandWallet by remember { mutableStateOf(false) }

    var error by remember { mutableStateOf<String?>(null) }

    fun openDatePicker() {
        val c = Calendar.getInstance().apply { timeInMillis = dateMillis }
        DatePickerDialog(
            context,
            { _, y, m, d ->
                val picked = Calendar.getInstance().apply {
                    set(Calendar.YEAR, y)
                    set(Calendar.MONTH, m)
                    set(Calendar.DAY_OF_MONTH, d)
                    set(Calendar.HOUR_OF_DAY, 12)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                dateMillis = picked.timeInMillis
            },
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    LaunchedEffect(type) { selectedCategory = null }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB)))
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0)
        ) { inner ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onBack?.invoke() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1976D2))
                    }
                    Spacer(Modifier.width(4.dp))
                    Text("Thêm giao dịch", style = MaterialTheme.typography.titleLarge)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == "expense",
                        onClick = { type = "expense" },
                        label = { Text("Chi tiêu") }
                    )
                    FilterChip(
                        selected = type == "income",
                        onClick = { type = "income" },
                        label = { Text("Thu nhập") }
                    )
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Số tiền") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = FieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FieldFocusColor,
                        unfocusedBorderColor = FieldBorderColor,
                        focusedContainerColor = FieldContainerWhite,
                        unfocusedContainerColor = FieldContainerWhite,
                        cursorColor = Color(0xFF1976D2)
                    )
                )

                Spacer(Modifier.height(10.dp))

                /** CATEGORY COMBOBOX */
                ExposedDropdownMenuBox(
                    expanded = expandCategory,
                    onExpandedChange = { expandCategory = !expandCategory }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name
                            ?: if (categories.isEmpty()) "Chưa có danh mục" else "Chọn danh mục",
                        onValueChange = {},
                        label = { Text("Danh mục") },
                        readOnly = true,
                        enabled = categories.isNotEmpty(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandCategory) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = FieldShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FieldFocusColor,
                            unfocusedBorderColor = FieldBorderColor,
                            focusedContainerColor = FieldContainerWhite,
                            unfocusedContainerColor = FieldContainerWhite,
                            cursorColor = Color(0xFF1976D2)
                        )

                    )

                    ExposedDropdownMenu(
                        expanded = expandCategory,
                        onDismissRequest = { expandCategory = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCategory = cat
                                    expandCategory = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                /** WALLET COMBOBOX */
                ExposedDropdownMenuBox(
                    expanded = expandWallet,
                    onExpandedChange = { expandWallet = !expandWallet }
                ) {
                    OutlinedTextField(
                        value = selectedWallet?.name
                            ?: if (wallets.isEmpty()) "Chưa có ví" else "Chọn ví",
                        onValueChange = {},
                        readOnly = true,
                        enabled = wallets.isNotEmpty(),
                        label = { Text("Ví") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandWallet) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = FieldShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FieldFocusColor,
                            unfocusedBorderColor = FieldBorderColor,
                            focusedContainerColor = FieldContainerWhite,
                            unfocusedContainerColor = FieldContainerWhite,
                            cursorColor = Color(0xFF1976D2)
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expandWallet,
                        onDismissRequest = { expandWallet = false }
                    ) {
                        wallets.forEach { wal ->
                            DropdownMenuItem(
                                text = { Text(wal.name) },
                                onClick = {
                                    selectedWallet = wal
                                    expandWallet = false
                                }
                            )
                        }

                    }
                }

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Ghi chú") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = FieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FieldFocusColor,
                        unfocusedBorderColor = FieldBorderColor,
                        focusedContainerColor = FieldContainerWhite,
                        unfocusedContainerColor = FieldContainerWhite,
                        cursorColor = Color(0xFF1976D2)
                    )
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = dateFmt.format(Date(dateMillis)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ngày") },
                    trailingIcon = {
                        IconButton(onClick = { openDatePicker() }) {
                            Icon(Icons.Filled.CalendarMonth, contentDescription = "Chọn ngày")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FieldFocusColor,
                        unfocusedBorderColor = FieldBorderColor,
                        focusedContainerColor = FieldContainerWhite,
                        unfocusedContainerColor = FieldContainerWhite,
                        cursorColor = Color(0xFF1976D2)
                    )
                )

                if (error != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        error = null
                        val amt = amountText.toDoubleOrNull()
                        val cat = selectedCategory
                        val wal = selectedWallet

                        when {
                            amt == null || amt <= 0 -> error = "Số tiền không hợp lệ"
                            cat == null -> error = "Hãy chọn danh mục"
                            wal == null -> error = "Hãy chọn ví"
                            else -> {
                                val expense = Expense(
                                    userId = userId,
                                    title = note.ifBlank { cat.name },
                                    amount = amt,
                                    categoryId = cat.id,
                                    walletId = wal.id,
                                    date = dateMillis,
                                    type = type
                                )
                                scope.launch {
                                    repo.addExpenseAndAffectWallet(expense)
                                    onSaved?.invoke()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF388E3C),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Lưu")
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}
