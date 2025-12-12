package com.example.appqlchitieu.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appqlchitieu.utils.SessionManager
import com.example.appqlchitieu.viewmodel.UserViewModel

@Composable
fun VerifyEmailScreen(
    email: String,
    vm: UserViewModel,        // ⚡ THÊM VIEWMODEL
    onVerifySuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    onResendCode: () -> Unit
) {
    var otp by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF3A7BD5), Color(0xFF00d2ff))
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        Card(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Xác Thực Email",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Mã xác thực đã được gửi đến\n$email",
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 10.dp)
                )

                OutlinedTextField(
                    value = otp,
                    onValueChange = { if (it.length <= 6) otp = it },
                    label = { Text("Nhập mã OTP (6 số)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 🔥 NÚT XÁC MINH OTP — ĐÃ CHỈNH ĐÚNG
                Button(
                    onClick = {
                        if (otp.length == 6) {
                            if (vm.verifyOtp(otp)) {
                                onVerifySuccess()
                            } else {
                                message = "Mã OTP không đúng hoặc đã hết hạn!"
                            }

                        } else {
                            message = "OTP phải gồm 6 số!"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Xác minh", fontSize = 17.sp)
                }

                Text(
                    text = message,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 6.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                TextButton(onClick = onResendCode) {
                    Text("Gửi lại mã", color = Color(0xFF007AFF))
                }

                TextButton(onClick = onBackToLogin) {
                    Text("Quay lại đăng nhập", color = Color.Gray, fontSize = 14.sp)
                }
            }
        }
    }
}

