package com.example.appqlchitieu.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.appqlchitieu.utils.SessionManager
import com.example.appqlchitieu.viewmodel.UserViewModel

@Composable
fun LoginScreen(
    nav: NavHostController, // giữ để không vỡ chỗ gọi cũ (dù không dùng trực tiếp)
    vm: UserViewModel,
    sessionManager: SessionManager,
    onRegisterClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF4980FF), Color(0xFF8AC9FF))
                )
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        ) {

            Text(
                text = "Chào mừng trở lại!",
                fontSize = 28.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Đăng nhập để tiếp tục",
                fontSize = 16.sp,
                color = Color.White.copy(.9f),
                modifier = Modifier.padding(bottom = 30.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 15.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        enabled = !loading
                    )

                    OutlinedTextField(
                        value = pass,
                        onValueChange = { pass = it },
                        label = { Text("Mật khẩu") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 25.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        enabled = !loading
                    )

                    Button(
                        onClick = {
                            val e = email.trim()
                            val p = pass

                            if (e.isEmpty() || p.isEmpty()) {
                                Toast.makeText(context, "Nhập đủ email và mật khẩu đã nào 😅", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            loading = true
                            vm.login(context, e, p) { success ->
                                loading = false

                                if (success) {
                                    val user = vm.currentUser.value
                                    if (user != null) {
                                        // Lưu userId cho các màn sau đọc
                                        sessionManager.saveLogin(user.id)

                                        // Điều hướng do AuthNavigation chịu trách nhiệm
                                        onLoginSuccess()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Login ok nhưng chưa lấy được user. Thử lại nhé.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Sai thông tin hoặc chưa xác thực email",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3C72FF)),
                        enabled = !loading
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Đang đăng nhập...", fontSize = 16.sp, color = Color.White)
                        } else {
                            Text("Đăng nhập", fontSize = 18.sp, color = Color.White)
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Chưa có tài khoản?")
                        TextButton(
                            onClick = onRegisterClick,
                            enabled = !loading
                        ) {
                            Text("Đăng ký ngay", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
