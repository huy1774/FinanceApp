@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.appqlchitieu.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.appqlchitieu.R
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
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(scroll)
        ) {
            // Tổng số dư
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Tổng số dư",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF757575)
                    )

                    Text(
                        text = "${nf.format(totalBalance)}₫",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color(0xFF512DA8)
                    )
                }
            }

            // Các nút chức năng
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) {
                    FeatureCard("Ví của tôi", painterResource(id = R.drawable.ic_wallet)) { onNavigateToWallet() }
                }
                Box(Modifier.weight(1f)) {
                    FeatureCard("Danh mục", painterResource(id = R.drawable.ic_list)) { onNavigateToCategory() }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Gọi StatsScreen đã tách ra file riêng để hiển thị biểu đồ
            StatsScreen(modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(80.dp)) // Khoảng trống cuối cùng để không bị che bởi BottomBar
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    icon: Painter,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = Color(0xFF1976D2)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF212121)
            )
        }
    }
}