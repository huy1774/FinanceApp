package com.example.appqlchitieu.view.auth

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.appqlchitieu.ui.auth.VerifyPurpose
import com.example.appqlchitieu.viewmodel.UserViewModel

@Composable
fun ForgotPasswordScreen(
    nav: NavHostController,
    vm: UserViewModel,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Màu background theo yêu cầu của bạn
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
                "Quên Mật Khẩu",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Nhập email đã đăng ký để nhận mã OTP",
                color = Color.White.copy(0.9f),
                fontSize = 14.sp
            )
            Spacer(Modifier.height(30.dp))

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
                        leadingIcon = { Icon(Icons.Default.Email, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val e = email.trim()
                            if (e.isBlank()) {
                                Toast.makeText(context, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
                                Toast.makeText(context, "Email không đúng định dạng", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            loading = true

                            // CHECK LOGIC: Kiểm tra email có trong Database không?
                            vm.getUserByEmail(e) { user ->
                                if (user == null) {
                                    loading = false
                                    Toast.makeText(context, "Email này chưa được đăng ký tài khoản!", Toast.LENGTH_SHORT).show()
                                } else {
                                    // Có tồn tại -> Gửi OTP
                                    vm.generateOtp(e, VerifyPurpose.RESET_PASSWORD) { sent ->
                                        loading = false
                                        if (sent) {
                                            nav.navigate("verify/$e/RESET_PASSWORD")
                                        } else {
                                            Toast.makeText(context, "Lỗi gửi mail. Vui lòng thử lại.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3C72FF)),
                        enabled = !loading
                    ) {
                        if (loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Gửi mã xác thực", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}