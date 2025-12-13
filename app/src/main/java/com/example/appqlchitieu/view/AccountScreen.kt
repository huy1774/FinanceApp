
package com.example.appqlchitieu.view

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appqlchitieu.R
import com.example.appqlchitieu.viewmodel.UserViewModel

@Composable
fun AccountScreen(
    userViewModel: UserViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    // Lấy dữ liệu user từ DataStore qua ViewModel
    val name by userViewModel.userName.collectAsState(initial = "")
    val email by userViewModel.userEmail.collectAsState(initial = "")
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))
                )
            )
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            tint = Color(0xFF388E3C),
            modifier = Modifier.size(100.dp)
        )

        Spacer(Modifier.height(8.dp))

        // Hiển thị user đã login (nếu null thì dùng default)
        Text(
            text = if (name.isEmpty()) "Tên người dùng" else name,
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = if (email.isEmpty()) "email@example.com" else email,
            color = Color.Gray
        )

        Spacer(Modifier.height(24.dp))

        AccountOption(
            icon = painterResource(id = R.drawable.ic_ai),
            title = "AI Supporter"
        ) {
            context.startActivity(Intent(context, AIChatActivity::class.java))
        }

        AccountOption(Icons.Default.Settings, "Ngôn ngữ") {}
        AccountOption(Icons.Default.Notifications, "Thông báo") {}

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { showDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("Đăng xuất", color = Color.White)
        }
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Xác nhận đăng xuất") },
                text = { Text("Bạn có chắc chắn muốn đăng xuất không?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            userViewModel.logout()
                            showDialog = false
                            onLogout()   // ← điều hướng về login
                        }
                    ) { Text("Đăng xuất", color = Color.Red) }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}


@Composable
private fun AccountOption(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
)

{
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onClick
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF388E3C))
            Spacer(Modifier.width(12.dp))
            Text(title)
        }
    }
}

@Composable
private fun AccountOption(
    icon: Painter,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onClick
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = Color(0xFF388E3C)   // giữ nguyên màu xanh như nút cũ
            )
            Spacer(Modifier.width(12.dp))
            Text(title)
        }
    }
}



