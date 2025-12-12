// app/src/main/java/com/example/appqlchitieu/view/TransactionScreen.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.appqlchitieu.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.appqlchitieu.database.DatabaseProvider
import com.example.appqlchitieu.model.Expense
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import com.example.appqlchitieu.utils.SessionManager
import com.example.appqlchitieu.utils.UserSession

// ✅ Đưa enum ra top-level & đổi tên để tránh đụng với material3.Tab
enum class TxTab { ALL, EXPENSE, INCOME }

/**
 * Danh sách giao dịch theo THÁNG:
 *  - Mũi tên ← → chuyển tháng
 *  - Tổng thu/chi của tháng
 *  - Bộ lọc: Tất cả | Chi | Thu
 *  - Thêm/Sửa/Xoá (dùng DB thật)
 */
@Composable
fun TransactionScreen(
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit,
    onEdit: (expenseId: Int) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
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
    val dateFmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // THÁNG/NĂM hiện tại
    val now = remember { Calendar.getInstance() }
    var year by rememberSaveable { mutableStateOf(now.get(Calendar.YEAR)) }
    var month by rememberSaveable { mutableStateOf(now.get(Calendar.MONTH)) } // 0..11

    // Mốc đầu/cuối tháng
    val (startMillis, endMillis) = remember(year, month) { monthBounds(year, month) }
    val monthLabel = remember(startMillis) {
        SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date(startMillis))
    }

    // Lấy dữ liệu DB theo khoảng ngày
    val expensesThisMonth by remember(startMillis, endMillis) {
        db.expenseDao().getExpensesByDateRange(userId, startMillis, endMillis)
    }.collectAsState(initial = emptyList())

    // Bộ lọc
    var tab by rememberSaveable { mutableStateOf(TxTab.ALL) }
    val filtered = remember(expensesThisMonth, tab) {
        val src = when (tab) {
            TxTab.ALL -> expensesThisMonth
            TxTab.EXPENSE -> expensesThisMonth.filter { it.type == "expense" }
            TxTab.INCOME -> expensesThisMonth.filter { it.type == "income" }
        }
        src.sortedByDescending { it.date }
    }

    // Tổng thu/chi
    val totalExpense = remember(expensesThisMonth) { expensesThisMonth.filter { it.type == "expense" }.sumOf { it.amount } }
    val totalIncome  = remember(expensesThisMonth) { expensesThisMonth.filter { it.type == "income" }.sumOf { it.amount } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))))
            .padding(16.dp,16.dp,16.dp,0.dp)
    ) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // Thanh chọn tháng
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(6.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        if (month == 0) { month = 11; year -= 1 } else month -= 1
                    }) { Icon(Icons.Filled.ChevronLeft, contentDescription = "Tháng trước") }

                    Text(monthLabel, style = MaterialTheme.typography.titleMedium)

                    IconButton(onClick = {
                        if (month == 11) { month = 0; year += 1 } else month += 1
                    }) { Icon(Icons.Filled.ChevronRight, contentDescription = "Tháng sau") }
                }
            }

            // Tổng thu/chi
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Tổng chi", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        Text("${nf.format(totalExpense.toLong())}₫", color = Color(0xFFE53935), style = MaterialTheme.typography.titleLarge)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Tổng thu", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        Text("${nf.format(totalIncome.toLong())}₫", color = Color(0xFF1E88E5), style = MaterialTheme.typography.titleLarge)
                    }
                }
            }

            // Bộ lọc chip
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = tab == TxTab.ALL,
                    onClick = { tab = TxTab.ALL },
                    label = { Text("Tất cả") }
                )
                FilterChip(
                    selected = tab == TxTab.EXPENSE,
                    onClick = { tab = TxTab.EXPENSE },
                    label = { Text("Chi") }
                )
                FilterChip(
                    selected = tab == TxTab.INCOME,
                    onClick = { tab = TxTab.INCOME },
                    label = { Text("Thu") }
                )
            }

            // Nút Thêm giao dịch
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF388E3C),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Thêm giao dịch")
            }

            // Danh sách
            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Chưa có giao dịch phù hợp", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(filtered, key = { it.id }) { e ->
                        TransactionItemCard(
                            expense = e,
                            dateText = dateFmt.format(Date(e.date)),
                            onEdit = { onEdit(e.id) },
                            onDelete = { scope.launch { db.expenseDao().deleteExpense(e) } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionItemCard(
    expense: Expense,
    dateText: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isExpense = expense.type == "expense"
    val color = if (isExpense) Color(0xFFE53935) else Color(0xFF2E7D32)
    val prefix = if (isExpense) "-" else "+"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(expense.title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text(dateText, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
            Text("$prefix${formatVnd(expense.amount)}", color = color, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Sửa") }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Xoá") }
        }
    }
}

/* ---------- Helpers ---------- */
private fun monthBounds(year: Int, month0Based: Int): Pair<Long, Long> {
    val c1 = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month0Based)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val start = c1.timeInMillis
    val c2 = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month0Based)
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
    }
    return start to c2.timeInMillis
}

private fun formatVnd(value: Double): String =
    NumberFormat.getInstance(Locale("vi", "VN")).format(value.toLong()) + "₫"
