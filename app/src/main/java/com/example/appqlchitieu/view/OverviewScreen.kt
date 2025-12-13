
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.appqlchitieu.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appqlchitieu.R
import com.example.appqlchitieu.view.ui.theme.AppQLChiTieuTheme
import java.text.NumberFormat
import java.util.*

@Composable
fun OverviewScreen(
    modifier: Modifier = Modifier,
    onNavigateToWallet: () -> Unit = {},
    onNavigateToCategory: () -> Unit = {},
    totalBalance: Double = 0.0
) {
    val nf = remember { NumberFormat.getInstance(Locale("vi", "VN")) }
    val scroll = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))))
            .padding(16.dp,16.dp,16.dp,0.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(scroll)   // cho phép cuộn thay vì “nén” nội dung
        ) {
            // Tổng số dư
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(12.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Tổng số dư", style = MaterialTheme.typography.titleMedium, color = Color(0xFF757575))
                    Spacer(Modifier.height(8.dp))
                    Text("${nf.format(totalBalance)}₫", style = MaterialTheme.typography.headlineLarge, color = Color(0xFF512DA8))
                }
            }

            FeatureCard("Ví của tôi", painterResource(id = R.drawable.ic_wallet)) { onNavigateToWallet() }
            FeatureCard("Quản lý danh mục", painterResource(id = R.drawable.ic_list)) { onNavigateToCategory() }

            Spacer(Modifier.height(12.dp))

            // Bảng thống kê (đọc DB thật)
            StatsScreen(modifier = Modifier.fillMaxWidth())

            // chêm thêm khoảng trống nhỏ cuối để khi cuộn không “dính” đường đỏ
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun FeatureCard(title: String, icon: Painter, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(12.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(painter = icon, contentDescription = title, modifier = Modifier.size(40.dp), tint = Color(0xFF1976D2))
            Spacer(Modifier.width(20.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = Color(0xFF212121))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewOverviewScreen() {
    AppQLChiTieuTheme { OverviewScreen(totalBalance = 12_345_678.0) }
}
