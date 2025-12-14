package com.example.appqlchitieu.ui.auth

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import com.example.appqlchitieu.model.User
import com.example.appqlchitieu.viewmodel.UserViewModel
import com.example.appqlchitieu.ui.auth.VerifyPurpose

@Composable
fun RegisterScreen(
    nav: NavHostController,
    vm: UserViewModel,
    onLoginClick: () -> Unit,
    onRegisterSuccess: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF4980FF), Color(0xFF8AC9FF))))
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().align(Alignment.Center)) {
            Text("Tạo tài khoản", fontSize = 28.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Đăng ký để bắt đầu sử dụng", fontSize = 16.sp, color = Color.White.copy(.9f), modifier = Modifier.padding(bottom = 30.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Họ và tên") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 15.dp),
                        singleLine = true, shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, null) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 15.dp),
                        singleLine = true, shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Mật khẩu") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 15.dp),
                        singleLine = true, shape = RoundedCornerShape(12.dp),
                        visualTransformation = PasswordVisualTransformation())
                    OutlinedTextField(value = confirm, onValueChange = { confirm = it }, label = { Text("Nhập lại mật khẩu") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 25.dp),
                        singleLine = true, shape = RoundedCornerShape(12.dp),
                        visualTransformation = PasswordVisualTransformation())

                    Button(
                        onClick = {
                            if (name.isBlank()) { Toast.makeText(context, "Tên không được để trống!", Toast.LENGTH_SHORT).show(); return@Button }
                            if (email.isBlank()) { Toast.makeText(context, "Email không được để trống!", Toast.LENGTH_SHORT).show(); return@Button }
                            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { Toast.makeText(context, "Email không đúng định dạng!", Toast.LENGTH_SHORT).show(); return@Button }
                            if (pass.isBlank()) { Toast.makeText(context, "Mật khẩu không được để trống!", Toast.LENGTH_SHORT).show(); return@Button }
                            if (confirm.isBlank()) { Toast.makeText(context, "Vui lòng nhập lại mật khẩu!", Toast.LENGTH_SHORT).show(); return@Button }
                            if (pass.length < 6) { Toast.makeText(context, "Mật khẩu phải ≥ 6 ký tự!", Toast.LENGTH_SHORT).show(); return@Button }
                            if (pass != confirm) { Toast.makeText(context, "Mật khẩu không trùng khớp!", Toast.LENGTH_SHORT).show(); return@Button }

                            val newUser = User(name = name.trim(), email = email.trim(), phone = phone.trim(), password = pass)

                            vm.register(newUser) { success ->
                                if (success) onRegisterSuccess(email.trim())
                                else Toast.makeText(context, "Email đã tồn tại!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3C72FF))
                    ) { Text("Đăng ký", fontSize = 18.sp, color = Color.White) }

                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        Text("Đã có tài khoản?")
                        TextButton(onClick = onLoginClick) { Text("Đăng nhập", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}
