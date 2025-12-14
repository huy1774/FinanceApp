package com.example.appqlchitieu.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appqlchitieu.viewmodel.UserViewModel

@Composable
fun VerifyEmailScreen(
    email: String,
    vm: UserViewModel,
    purpose: VerifyPurpose,
    onVerifySuccess: () -> Unit,
    onBack: () -> Unit,
    onResendCode: () -> Unit
) {
    var otp by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

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
                "Xác Thực Email",
                fontSize = 26.sp,
                color = Color.White,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Mã OTP đã được gửi đến:",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Text(
                        email,
                        color = Color.Black,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 5.dp)
                    )
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = otp,
                        onValueChange = { if (it.length <= 6) otp = it },
                        label = { Text("Nhập mã OTP (6 số)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (message.isNotBlank()) {
                        Text(
                            message,
                            color = Color.Red,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (otp.length != 6) {
                                message = "Mã OTP phải có 6 chữ số!"
                                return@Button
                            }
                            if (vm.verifyOtp(email, otp, purpose)) {
                                onVerifySuccess()
                            } else {
                                message = "Mã OTP không đúng hoặc đã hết hạn!"
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3C72FF))
                    ) {
                        Text("Xác nhận", fontSize = 17.sp)
                    }

                    Spacer(Modifier.height(12.dp))

                    TextButton(onClick = {
                        onResendCode()
                        message = "Đang gửi lại mã..."
                    }) {
                        Text("Chưa nhận được mã? Gửi lại", color = Color(0xFF3C72FF))
                    }
                }
            }
        }
    }
}