package com.example.appqlchitieu.view.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.appqlchitieu.viewmodel.UserViewModel

@Composable
fun ResetPasswordScreen(
    email: String,
    nav: NavHostController,
    vm: UserViewModel,
    onBack: () -> Unit
) {
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    // Màu background theo yêu cầu
    val bgBrush = Brush.verticalGradient(listOf(Color(0xFF4980FF), Color(0xFF8AC9FF)))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
            .padding(20.dp)
    ) {
        // Nút Back
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Đổi Mật Khẩu",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("Mật khẩu mới") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        visualTransformation = if (showNew) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showNew = !showNew }) {
                                Icon(if (showNew) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = confirmPass,
                        onValueChange = { confirmPass = it },
                        label = { Text("Xác nhận mật khẩu") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showConfirm = !showConfirm }) {
                                Icon(if (showConfirm) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (error.isNotBlank()) {
                        Text(error, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
                    }

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            when {
                                newPass.isBlank() || confirmPass.isBlank() -> error = "Vui lòng nhập đầy đủ"
                                newPass.length < 6 -> error = "Mật khẩu phải từ 6 ký tự trở lên"
                                newPass != confirmPass -> error = "Mật khẩu xác nhận không khớp"
                                else -> {
                                    vm.getUserByEmail(email) { user ->
                                        if (user != null) {
                                            vm.changePassword(user.id, newPass) { success, msg ->
                                                if (success) {
                                                    Toast.makeText(nav.context, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show()
                                                    nav.navigate("login") { popUpTo(0) }
                                                } else {
                                                    error = msg
                                                }
                                            }
                                        } else {
                                            error = "Lỗi hệ thống: Không tìm thấy user"
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3C72FF))
                    ) {
                        Text("Lưu mật khẩu mới", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}