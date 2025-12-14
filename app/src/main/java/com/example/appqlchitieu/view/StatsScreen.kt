@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.appqlchitieu.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appqlchitieu.database.DatabaseProvider
import com.example.appqlchitieu.model.Category
import com.example.appqlchitieu.utils.SessionManager
import com.example.appqlchitieu.utils.UserSession
import java.text.NumberFormat
import java.util.*

enum class StatMode { EXPENSE, INCOME }

@Composable
fun StatsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // Khởi tạo DB an toàn
    val db = remember { DatabaseProvider.getDatabase(context) }
    val sessionManager = remember { SessionManager(context) }

    // Lấy userId an toàn
    val userId = try {
        UserSession(sessionManager).userIdOrNull()
    } catch (e: Exception) { null }

    if (userId == null) {
        Box(modifier.height(200.dp), contentAlignment = Alignment.Center) {
            Text("Vui lòng đăng nhập để xem thống kê", color = Color.Gray)
        }
        return
    }

    val nf = remember { NumberFormat.getInstance(Locale("vi", "VN")) }
    val now = remember { Calendar.getInstance() }

    var year by rememberSaveable { mutableStateOf(now.get(Calendar.YEAR)) }
    var month by rememberSaveable { mutableStateOf(now.get(Calendar.MONTH)) }

    val (start, end) = remember(year, month) { monthBounds(year, month) }
    val monthLabel = remember(year, month) { "Tháng ${month + 1}, $year" }

    var mode by rememberSaveable { mutableStateOf(StatMode.EXPENSE) }

    val categories by db.categoryDao().getAllCategories(userId).collectAsState(initial = emptyList())
    val expenses by db.expenseDao().getExpensesByDateRange(userId, start, end).collectAsState(initial = emptyList())

    // Xử lý dữ liệu (Try-catch để tránh crash khi tính toán)
    val filteredData = remember(expenses, mode, categories) {
        try {
            val type = if (mode == StatMode.EXPENSE) "expense" else "income"
            val rawList = expenses.filter { it.type == type }

            val grouped = rawList.groupBy { it.categoryId }
                .mapValues { (_, list) -> list.sumOf { it.amount } }

            grouped.mapNotNull { (catId, total) ->
                val cat = categories.find { it.id == catId }
                if (cat != null) cat to total else null
            }.sortedByDescending { it.second }
        } catch (e: Exception) {
            emptyList()
        }
    }

    val totalAmount = remember(filteredData) { filteredData.sumOf { it.second } }

    val palette = listOf(
        Color(0xFF5C6BC0), Color(0xFFEF5350), Color(0xFF66BB6A),
        Color(0xFFFFA726), Color(0xFF29B6F6), Color(0xFFAB47BC),
        Color(0xFFFF7043), Color(0xFF8D6E63), Color(0xFF26A69A),
        Color(0xFF78909C)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F7FA), RoundedCornerShape(16.dp))
            .padding(bottom = 16.dp)
    ) {
        // Header chọn tháng
        StatsHeader(
            monthLabel = monthLabel,
            onPrev = { if (month == 0) { month = 11; year -= 1 } else month -= 1 },
            onNext = { if (month == 11) { month = 0; year += 1 } else month += 1 }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Switch Thu/Chi
        SegmentedControl(currentMode = mode, onModeChanged = { mode = it })

        Spacer(modifier = Modifier.height(24.dp))

        // Biểu đồ
        if (totalAmount > 0) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                DonutChart(data = filteredData, total = totalAmount, palette = palette)

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Tổng cộng", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        text = "${nf.format(totalAmount)}đ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        } else {
            EmptyStateChart()
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Danh sách chi tiết
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            if (filteredData.isEmpty()) {
                Text(
                    "Chưa có giao dịch nào.",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                filteredData.forEachIndexed { index, (cat, amount) ->
                    val color = palette[index % palette.size]
                    // Fix lỗi chia cho 0: Nếu total = 0 thì percentage = 0
                    val percentage = if (totalAmount > 0) (amount / totalAmount).toFloat() else 0f

                    CategoryStatItem(
                        category = cat,
                        amount = amount,
                        percentage = percentage,
                        color = color,
                        formatter = nf
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun StatsHeader(monthLabel: String, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Dùng ArrowBack/Forward (Core Icon) để tránh lỗi crash thư viện
        IconButton(onClick = onPrev, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.Gray)
        }
        Text(text = monthLabel, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        IconButton(onClick = onNext, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
fun SegmentedControl(currentMode: StatMode, onModeChanged: (StatMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .height(40.dp)
            .background(Color(0xFFE0E0E0), RoundedCornerShape(20.dp))
            .padding(4.dp)
    ) {
        val expenseColor by animateColorAsState(if (currentMode == StatMode.EXPENSE) Color.White else Color.Transparent, label = "c1")
        val incomeColor by animateColorAsState(if (currentMode == StatMode.INCOME) Color.White else Color.Transparent, label = "c2")

        Box(
            modifier = Modifier.weight(1f).fillMaxHeight().background(expenseColor, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp)).clickable { onModeChanged(StatMode.EXPENSE) },
            contentAlignment = Alignment.Center
        ) {
            Text("Chi tiêu", fontSize = 13.sp, fontWeight = if(currentMode == StatMode.EXPENSE) FontWeight.Bold else FontWeight.Normal,
                color = if(currentMode == StatMode.EXPENSE) Color(0xFFE53935) else Color.Gray)
        }
        Box(
            modifier = Modifier.weight(1f).fillMaxHeight().background(incomeColor, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp)).clickable { onModeChanged(StatMode.INCOME) },
            contentAlignment = Alignment.Center
        ) {
            Text("Thu nhập", fontSize = 13.sp, fontWeight = if(currentMode == StatMode.INCOME) FontWeight.Bold else FontWeight.Normal,
                color = if(currentMode == StatMode.INCOME) Color(0xFF43A047) else Color.Gray)
        }
    }
}

@Composable
fun DonutChart(
    data: List<Pair<Category, Double>>,
    total: Double,
    palette: List<Color>,
    size: Dp = 180.dp,
    thickness: Dp = 20.dp
) {
    val animatedProgress by animateFloatAsState(targetValue = 1f, animationSpec = tween(1000), label = "progress")

    Canvas(modifier = Modifier.size(size)) {
        var startAngle = -90f
        data.forEachIndexed { index, (_, amount) ->
            // Fix lỗi NaN nếu total = 0
            val ratio = if (total > 0) (amount / total).toFloat() else 0f
            val sweepAngle = (ratio * 360f) * animatedProgress
            val color = palette[index % palette.size]

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = thickness.toPx(), cap = StrokeCap.Butt)
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
fun EmptyStateChart() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().height(180.dp)) {
        Canvas(modifier = Modifier.size(180.dp)) {
            drawArc(color = Color.LightGray.copy(alpha = 0.2f), startAngle = 0f, sweepAngle = 360f, useCenter = false, style = Stroke(width = 20.dp.toPx()))
        }
        Text("Không có dữ liệu", color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun CategoryStatItem(
    category: Category,
    amount: Double,
    percentage: Float,
    color: Color,
    formatter: NumberFormat
) {
    // Đảm bảo percentage an toàn
    val safePercentage = percentage.coerceIn(0f, 1f)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(36.dp).background(color.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
            Text(text = category.name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = color, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = category.name, fontWeight = FontWeight.Medium, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = "${formatter.format(amount)}đ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))

            // Fix lỗi Progress Bar: Dùng lambda hoặc giá trị safe
            LinearProgressIndicator(
                progress = { safePercentage },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = Color(0xFFEEEEEE),
            )
        }
    }
}

private fun monthBounds(year: Int, month0: Int): Pair<Long, Long> {
    val c1 = Calendar.getInstance().apply {
        set(year, month0, 1, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val c2 = Calendar.getInstance().apply {
        set(year, month0, getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
        set(Calendar.MILLISECOND, 999)
    }
    return c1.timeInMillis to c2.timeInMillis
}