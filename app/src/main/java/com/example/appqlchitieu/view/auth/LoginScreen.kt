package com.example.appqlchitieu.ui.auth

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.appqlchitieu.utils.SessionManager
import com.example.appqlchitieu.viewmodel.UserViewModel

@Composable
fun LoginScreen(
    nav: NavHostController,
    vm: UserViewModel,
    sessionManager: SessionManager,
    onRegisterClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    // State hiển thị pass
    var showPass by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF4980FF), Color(0xFF8AC9FF))))
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().align(Alignment.Center)
        ) {
            Text("Chào mừng trở lại!", fontSize = 28.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Đăng nhập để tiếp tục", fontSize = 16.sp, color = Color.White.copy(0.9f), modifier = Modifier.padding(bottom = 30.dp))

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
                        modifier = Modifier.fillMaxWidth().padding(bottom = 15.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        enabled = !loading
                    )
                    OutlinedTextField(
                        value = pass,
                        onValueChange = { pass = it },
                        label = { Text("Mật khẩu") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        // Logic hiện/ẩn
                        visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (showPass) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { showPass = !showPass }) {
                                Icon(imageVector = image, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 25.dp),
                        singleLine = true,
                        enabled = !loading
                    )

                    TextButton(
                        onClick = { nav.navigate("forgot_password") },
                        modifier = Modifier.align(Alignment.End),
                        enabled = !loading
                    ) { Text("Quên mật khẩu?", color = Color(0xFF3C72FF)) }

                    Button(
                        onClick = {
                            val e = email.trim()
                            val p = pass
                            if (e.isBlank()) { Toast.makeText(context, "Email không được để trống!", Toast.LENGTH_SHORT).show(); return@Button }
                            if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) { Toast.makeText(context, "Email không đúng định dạng!", Toast.LENGTH_SHORT).show(); return@Button }
                            if (p.isBlank()) { Toast.makeText(context, "Mật khẩu không được để trống!", Toast.LENGTH_SHORT).show(); return@Button }
                            if (p.length < 6) { Toast.makeText(context, "Mật khẩu phải ≥ 6 ký tự!", Toast.LENGTH_SHORT).show(); return@Button }

                            loading = true
                            vm.login(context, e, p) { success, message ->
                                loading = false
                                if (success) {
                                    vm.currentUser?.let { sessionManager.saveLogin(it.id) }
                                    onLoginSuccess()
                                } else {
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3C72FF)),
                        enabled = !loading
                    ) {
                        if (loading) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = Color.White)
                            Spacer(Modifier.width(10.dp))
                            Text("Đang đăng nhập...", fontSize = 16.sp, color = Color.White)
                        } else {
                            Text("Đăng nhập", fontSize = 18.sp, color = Color.White)
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        Text("Chưa có tài khoản?")
                        TextButton(onClick = onRegisterClick, enabled = !loading) { Text("Đăng ký ngay", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}