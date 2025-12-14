@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.appqlchitieu.view

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.appqlchitieu.database.DatabaseProvider
import com.example.appqlchitieu.model.Budget
import com.example.appqlchitieu.model.Category
import com.example.appqlchitieu.utils.SessionManager
import com.example.appqlchitieu.utils.UserSession
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@Composable
fun BudgetScreen(onBack: (() -> Unit)? = null) {

    val context = LocalContext.current
    val db = remember { DatabaseProvider.getDatabase(context) }
    val scope = rememberCoroutineScope()

    val userSession = remember { UserSession(SessionManager(context)) }
    val userId = userSession.userIdOrNull()

    if (userId == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Bạn chưa đăng nhập")
        }
        return
    }

    val nf = remember { NumberFormat.getInstance(Locale("vi", "VN")) }
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val budgets by db.budgetDao().getAllBudgets(userId).collectAsState(emptyList())
    val categories by db.categoryDao().getAllCategories(userId).collectAsState(emptyList())

    val yearMillis = 366L * 24 * 60 * 60 * 1000
    val now = System.currentTimeMillis()
    val expenses by db.expenseDao()
        .getExpensesByDateRange(userId, now - yearMillis, now + yearMillis)
        .collectAsState(emptyList())

    var showAdd by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<Budget?>(null) }
    var deleteTarget by remember { mutableStateOf<Budget?>(null) }
    val snackbar = remember { SnackbarHostState() }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0),
            snackbarHost = { SnackbarHost(snackbar) }
        ) { inner ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
                    .padding(horizontal = 16.dp)
            ) {

                Text(
                    text = "Ngân sách",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                if (budgets.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Chưa có ngân sách nào", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        items(budgets, key = { it.id }) { b ->
                            val spent = remember(b, expenses) {
                                expenses
                                    .filter { it.type == "expense" }
                                    .filter { it.categoryId == b.categoryId }
                                    .filter { it.date in b.startDate..b.endDate }
                                    .sumOf { it.amount }
                            }

                            BudgetCardRow(
                                budget = b,
                                spent = spent,
                                category = categories.find { it.id == b.categoryId },
                                nf = nf,
                                sdf = sdf,
                                onEdit = { editTarget = b },
                                onDelete = { deleteTarget = b } // ✅ CLICK ĐƯỢC
                            )
                        }
                    }
                }

                // ✅ Button NẰM TRONG COLUMN
                Button(
                    onClick = { showAdd = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF388E3C),
                        contentColor = Color.White
                    )
                ) {
                    Text("Thêm ngân sách")
                }

                Spacer(Modifier.height(12.dp))
            }

        }
    }

    /* ================= ADD ================= */

    if (showAdd) {
        BudgetEditDialog(
            title = "Thêm ngân sách",
            init = Budget(
                userId = userId,
                categoryId = 0,
                amountLimit = 0.0,
                startDate = startOfToday(),
                endDate = endOfThisMonth()
            ),
            categories = categories,
            existingBudgets = budgets,
            onDismiss = { showAdd = false },
            onConfirm = {
                scope.launch {
                    db.budgetDao().insertBudget(it)
                    showAdd = false
                }
            }
        )

    }

    /* ================= EDIT ================= */

    editTarget?.let { tgt ->
        BudgetEditDialog(
            title = "Sửa ngân sách",
            init = tgt,
            categories = categories,
            existingBudgets = budgets,
            onDismiss = { editTarget = null },
            onConfirm = {
                scope.launch {
                    db.budgetDao().updateBudget(it.copy(id = tgt.id))
                    editTarget = null
                }
            }
        )
    }
    /* ================= DELETE ================= */

    deleteTarget?.let { tgt ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Xoá ngân sách") },
            text = {
                Text("Bạn có chắc muốn xoá ngân sách cho danh mục này?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            db.budgetDao().deleteBudget(tgt)
                            deleteTarget = null
                            snackbar.showSnackbar("Đã xoá ngân sách")
                        }
                    }
                ) {
                    Text("Xoá", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Huỷ")
                }
            }
        )
    }

}


/* ================= EDIT DIALOG ================= */

@Composable
private fun BudgetEditDialog(
    title: String,
    init: Budget,
    categories: List<Category>,
    existingBudgets: List<Budget>,
    onDismiss: () -> Unit,
    onConfirm: (Budget) -> Unit
) {
    val context = LocalContext.current

    var catId by remember { mutableStateOf(init.categoryId) }
    var amountText by remember {
        mutableStateOf(
            if (init.amountLimit > 0) init.amountLimit.toLong().toString() else ""
        )
    }

    var start by remember { mutableStateOf(init.startDate) }
    var end by remember { mutableStateOf(init.endDate) }

    var errorText by remember { mutableStateOf<String?>(null) }

    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val usedCategoryIds = remember(existingBudgets) {
        existingBudgets.map { it.categoryId }.toSet()
    }

    val selectableCategories = categories
        .filter { it.type == "expense" }
        .filter { it.id == catId || it.id !in usedCategoryIds }

    fun showDatePicker(current: Long, onPicked: (Long) -> Unit) {
        val cal = Calendar.getInstance().apply { timeInMillis = current }
        DatePickerDialog(
            context,
            { _, y, m, d ->
                val c = Calendar.getInstance().apply {
                    set(y, m, d, 12, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                onPicked(c.timeInMillis)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                var expanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = {
                    expanded = it
                }) {
                    OutlinedTextField(
                        value = selectableCategories
                            .find { it.id == catId }
                            ?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Danh mục") },
                        placeholder = { Text("Chọn danh mục") },
                        isError = errorText != null && catId == 0,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        selectableCategories.forEach {
                            DropdownMenuItem(
                                text = { Text(it.name) },
                                onClick = {
                                    catId = it.id
                                    expanded = false
                                    errorText = null
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it.filter(Char::isDigit)
                        errorText = null
                    },
                    label = { Text("Hạn mức (đ)") },
                    singleLine = true,
                    isError = errorText != null && amountText.isBlank()
                )

                DateRow("Bắt đầu", start, sdf) {
                    showDatePicker(start) { start = it }
                }

                DateRow("Kết thúc", end, sdf) {
                    showDatePicker(end) { end = it }
                }

                errorText?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val limit = amountText.toDoubleOrNull()
                errorText = null

                when {
                    catId == 0 -> {
                        errorText = "Vui lòng chọn danh mục"
                        return@TextButton
                    }
                    limit == null || limit <= 0 -> {
                        errorText = "Vui lòng nhập hạn mức hợp lệ"
                        return@TextButton
                    }
                    start > end -> {
                        errorText = "Ngày bắt đầu phải trước ngày kết thúc"
                        return@TextButton
                    }
                }

                onConfirm(
                    init.copy(
                        categoryId = catId,
                        amountLimit = limit,
                        startDate = start,
                        endDate = end
                    )
                )
            }) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Huỷ") }
        }
    )
}


/* ================= CARD ================= */

@Composable
private fun BudgetCardRow(
    budget: Budget,
    spent: Double,
    category: Category?,
    nf: NumberFormat,
    sdf: SimpleDateFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val limit = budget.amountLimit.coerceAtLeast(1.0)
    val ratio = (spent / limit).toFloat().coerceIn(0f, 1f)

    val barColor = when {
        ratio >= 1f -> Color(0xFFE53935)
        ratio >= 0.8f -> Color(0xFFFFA000)
        else -> Color(0xFF43A047)
    }

    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        category?.name ?: "Danh mục #${budget.categoryId}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "${sdf.format(Date(budget.startDate))} - ${sdf.format(Date(budget.endDate))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Sửa")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Xoá", tint = Color(0xFFE53935))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { ratio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = barColor,
                trackColor = Color(0xFFE0E0E0)
            )

            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Đã chi: ${nf.format(spent.toLong())}₫")
                val remain = budget.amountLimit - spent
                Text(
                    (if (remain >= 0) "Còn lại: " else "Vượt: ") +
                            "${nf.format(abs(remain).toLong())}₫",
                    color = if (remain >= 0) Color.Gray else Color(0xFFE53935)
                )
            }
        }
    }
}

/* ================= HELPERS ================= */

@Composable
private fun DateRow(
    label: String,
    value: Long,
    sdf: SimpleDateFormat,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFFF7F7F7), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$label: ", Modifier.width(80.dp), color = Color.Gray)
        TextButton(onClick = onClick) {
            Text(sdf.format(Date(value)))
        }
    }
}

private fun startOfToday(): Long = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun endOfThisMonth(): Long = Calendar.getInstance().apply {
    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
    set(Calendar.HOUR_OF_DAY, 23)
    set(Calendar.MINUTE, 59)
    set(Calendar.SECOND, 59)
    set(Calendar.MILLISECOND, 999)
}.timeInMillis


