
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appqlchitieu.database.DatabaseProvider
import com.example.appqlchitieu.model.Budget
import com.example.appqlchitieu.model.Category
import com.example.appqlchitieu.view.ui.theme.AppQLChiTieuTheme
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import com.example.appqlchitieu.utils.SessionManager
import com.example.appqlchitieu.utils.UserSession

/** Nếu app 1 user thì để mặc định 1; sau này truyền userId thực vào. */

@Composable
fun BudgetScreen(onBack: (() -> Unit)? = null) { // giữ signature cũ nhưng KHÔNG dùng back
    val context = LocalContext.current
    val db = remember { DatabaseProvider.getDatabase(context) }
    val scope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }
    val userSession = remember { UserSession(sessionManager) }
    val userId = userSession.userIdOrNull()

    if (userId == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Bạn chưa đăng nhập")
        }
        return
    }

    val nf = remember { NumberFormat.getInstance(Locale("vi", "VN")) }
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // Dữ liệu DB
    val budgets by db.budgetDao().getAllBudgets(userId).collectAsState(initial = emptyList())
    val categories by db.categoryDao().getAllCategories(userId).collectAsState(initial = emptyList())


    // Lấy expense trong 1 năm quanh hiện tại để tính nhanh
    val yearMillis = 366L * 24 * 60 * 60 * 1000
    val now = System.currentTimeMillis()
    val expenses by db.expenseDao()
        .getExpensesByDateRange(userId, now - yearMillis, now + yearMillis)
        .collectAsState(initial = emptyList())

    var showAdd by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<Budget?>(null) }
    var deleteTarget by remember { mutableStateOf<Budget?>(null) }
    val snackbar = remember { SnackbarHostState() }

    // Nền gradient (khớp các màn khác)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))))
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
                // Chỉ tiêu đề – không còn nút Back
                Text(
                    text = "Ngân sách",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF212121),
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )

                if (budgets.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) { Text("Chưa có ngân sách nào", color = Color.Gray) }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 4.dp)
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
                                onDelete = { deleteTarget = b }
                            )
                        }
                    }
                }

                // Thẻ “Thêm ngân sách” ở cuối
                Button(
                    onClick = { showAdd = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF388E3C),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) { Text("Thêm ngân sách") }

                Spacer(Modifier.height(12.dp))
            }
        }
    }

    // Dialog thêm
    if (showAdd) {
        BudgetEditDialog(
            title = "Thêm ngân sách",
            init = Budget(
                userId = userId,
                categoryId = categories.firstOrNull()?.id ?: 0,
                amountLimit = 0.0,
                startDate = startOfToday(),
                endDate = endOfThisMonth()
            ),
            categories = categories,
            onDismiss = { showAdd = false },
            onConfirm = { newB ->
                scope.launch {
                    db.budgetDao().insertBudget(newB)
                    showAdd = false
                }
            }
        )
    }

    // Dialog sửa
    editTarget?.let { tgt ->
        BudgetEditDialog(
            title = "Sửa ngân sách",
            init = tgt,
            categories = categories,
            onDismiss = { editTarget = null },
            onConfirm = { up ->
                scope.launch {
                    db.budgetDao().updateBudget(up.copy(id = tgt.id))
                    editTarget = null
                }
            }
        )
    }

    // Xác nhận xoá
    deleteTarget?.let { tgt ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Xoá ngân sách") },
            text = { Text("Xoá ngân sách cho danh mục này?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        db.budgetDao().deleteBudget(tgt)
                        deleteTarget = null
                        snackbar.showSnackbar("Đã xoá ngân sách")
                    }
                }) { Text("Xoá") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Hủy") } }
        )
    }
}

/* ---------- UI items ---------- */

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
        ratio >= 1f   -> Color(0xFFE53935)
        ratio >= 0.8f -> Color(0xFFFFA000)
        else          -> Color(0xFF43A047)
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
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row {
                    IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Sửa") }
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
                val remainText = if (remain >= 0) "Còn lại: " else "Vượt: "
                Text(
                    remainText + "${nf.format(abs(remain).toLong())}₫",
                    color = if (remain >= 0) Color.Gray else Color(0xFFE53935)
                )
            }
        }
    }
}

@Composable
private fun BudgetEditDialog(
    title: String,
    init: Budget,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (Budget) -> Unit
) {
    val context = LocalContext.current
    var catId by remember { mutableStateOf(init.categoryId) }
    var amountText by remember {
        mutableStateOf(if (init.amountLimit > 0) init.amountLimit.toLong().toString() else "")
    }
    var start by remember { mutableStateOf(init.startDate) }
    var end by remember { mutableStateOf(init.endDate) }
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

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
                // Danh mục
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = categories.find { it.id == catId }?.name ?: "Chọn danh mục",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Danh mục") },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.filter { it.type == "expense" }.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c.name) },
                                onClick = {
                                    catId = c.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Hạn mức (đ)") },
                    singleLine = true
                )

                // ngày bắt đầu
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF7F7F7), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text("Bắt đầu: ", modifier = Modifier.width(80.dp), color = Color.Gray)
                    TextButton(onClick = { showDatePicker(start) { start = it } }) {
                        Text(sdf.format(Date(start)))
                    }
                }

                // ngày kết thúc
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF7F7F7), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text("Kết thúc: ", modifier = Modifier.width(80.dp), color = Color.Gray)
                    TextButton(onClick = { showDatePicker(end) { end = it } }) {
                        Text(sdf.format(Date(end)))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val limit = amountText.toDoubleOrNull() ?: 0.0
                if (limit <= 0.0 || catId == 0) return@TextButton
                if (start > end) return@TextButton
                onConfirm(
                    init.copy(
                        userId = init.userId,
                        categoryId = catId,
                        amountLimit = limit,
                        startDate = start,
                        endDate = end
                    )
                )
            }) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}

/* ---------- Helpers ---------- */
private fun startOfToday(): Long = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun endOfThisMonth(): Long {
    val c = Calendar.getInstance()
    c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
    c.set(Calendar.HOUR_OF_DAY, 23); c.set(Calendar.MINUTE, 59); c.set(Calendar.SECOND, 59); c.set(Calendar.MILLISECOND, 999)
    return c.timeInMillis
}

