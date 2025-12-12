@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.appqlchitieu.view

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.appqlchitieu.database.DatabaseProvider
import com.example.appqlchitieu.model.Category
import com.example.appqlchitieu.model.Wallet
import com.example.appqlchitieu.model.Expense
import kotlinx.coroutines.flow.firstOrNull
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
fun TransactionUpdateScreen(
    expenseId: Int,
    onSaved: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null
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

    val dateFmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    var loaded by remember { mutableStateOf<Expense?>(null) }
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("expense") }
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var error by remember { mutableStateOf<String?>(null) }

    // ⬇️ Danh mục
    val categories by db.categoryDao().getCategoriesByType(userId, type).collectAsState(initial = emptyList())

    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var expandCategory by remember { mutableStateOf(false) }

    // ⬇️ Ví
    val wallets by db.walletDao().getAllWallets(userId)
        .collectAsState(initial = emptyList())

    var selectedWallet by remember { mutableStateOf<Wallet?>(null) }
    var expandWallet by remember { mutableStateOf(false) }

    // Load dữ liệu thật từ DB
    LaunchedEffect(expenseId) {
        val e = db.expenseDao().getExpenseById(userId, expenseId)
        loaded = e

        if (e != null) {
            title = e.title
            amountText = e.amount.toString()
            type = e.type
            dateMillis = e.date

            selectedCategory = db.categoryDao()
                .getCategoriesByType(userId, e.type)
                .firstOrNull()
                ?.firstOrNull { it.id == e.categoryId }

            selectedWallet = db.walletDao().getWalletById(userId, e.walletId)

        } else {
            error = "Không tìm thấy giao dịch #$expenseId"
        }
    }

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

    Scaffold(

    ) { innerPadding ->

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB)))
                )
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onBack?.invoke() }) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1976D2)
                    )
                }
                Text(
                    "Sửa giao dịch",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
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

            // ============================
            // CATEGORY COMBOBOX
            // ============================
            ExposedDropdownMenuBox(
                expanded = expandCategory,
                onExpandedChange = { expandCategory = !expandCategory }
            ) {
                OutlinedTextField(
                    value = selectedCategory?.name ?: "Chọn danh mục",
                    onValueChange = {},
                    readOnly = true,
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

            // ============================
            // WALLET COMBOBOX
            // ============================
            ExposedDropdownMenuBox(
                expanded = expandWallet,
                onExpandedChange = { expandWallet = !expandWallet }
            ) {
                OutlinedTextField(
                    value = selectedWallet?.name ?: "Chọn ví",
                    onValueChange = {},
                    readOnly = true,
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

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
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

            OutlinedTextField(
                value = dateFmt.format(Date(dateMillis)),
                onValueChange = {},
                label = { Text("Ngày") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { openDatePicker() }) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = "Chọn ngày")
                    }
                },
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

            Button(
                onClick = {
                    error = null
                    val old = loaded ?: return@Button
                    val newAmount = amountText.toDoubleOrNull()
                    if (title.isBlank() || newAmount == null || newAmount <= 0) {
                        error = "Vui lòng nhập ghi chú và số tiền hợp lệ."
                        return@Button
                    }
                    if (selectedWallet == null || selectedCategory == null) {
                        error = "Vui lòng chọn danh mục và ví."
                        return@Button
                    }

                    scope.launch {
                        val updated = old.copy(
                            title = title.trim(),
                            amount = newAmount,
                            type = type,
                            categoryId = selectedCategory!!.id,
                            walletId = selectedWallet!!.id,
                            date = dateMillis
                        )
                        db.expenseDao().updateExpense(updated)
                        onSaved?.invoke()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF388E3C),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Lưu thay đổi")
            }
        }
    }
}
