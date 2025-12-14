package com.example.appqlchitieu.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.appqlchitieu.viewmodel.UserViewModel

@Composable
fun AccountScreen(
    userViewModel: UserViewModel,
    navController: NavHostController,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    // --- SỬA LỖI TẠI ĐÂY ---
    // Lấy dữ liệu user thực tế từ ViewModel
    val currentUser = userViewModel.currentUser

    // Nếu currentUser null (ví dụ mới mở lại app), hiển thị placeholder, ngược lại hiển thị thông tin thật
    val name = currentUser?.name ?: "Người dùng"
    val email = currentUser?.email ?: "Đang tải..."

    // Nếu User chưa được load (currentUser == null) nhưng đã đăng nhập,
    // ta có thể kích hoạt reload lại từ DB (Option này tùy chọn, nhưng tốt cho trải nghiệm)
    /*
    LaunchedEffect(Unit) {
         if (currentUser == null) {
             // userViewModel.reloadUser() // Cần viết hàm này bên ViewModel nếu muốn load lại khi restart app
         }
    }
    */

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

        // Hiển thị tên
        Text(
            text = name,
            style = MaterialTheme.typography.titleLarge
        )

        // Hiển thị email
        Text(
            text = email,
            color = Color.Gray
        )

        Spacer(Modifier.height(24.dp))

        AccountOption(
            icon = Icons.Default.Lock,
            title = "Đổi mật khẩu"
        ) {
            navController.navigate("change_password")
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
                            showDialog = false
                            onLogout()
                        }
                    ) {
                        Text("Đăng xuất", color = Color.Red)
                    }
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
            Icon(icon, contentDescription = null, tint = Color(0xFF388E3C))
            Spacer(Modifier.width(12.dp))
            Text(title)
        }
    }
}