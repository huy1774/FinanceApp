package com.example.appqlchitieu.view

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.appqlchitieu.utils.SessionManager
import com.example.appqlchitieu.utils.UserSession
import com.example.appqlchitieu.viewmodel.UserViewModel

/* ================= ENUM ================= */

enum class ErrorField {
    OLD, NEW, CONFIRM
}

/* ================= SCREEN ================= */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    userViewModel: UserViewModel,
    sessionManager: SessionManager,
    navController: NavHostController
) {
    val context = LocalContext.current
    val userId = UserSession(sessionManager).requireUserId()

    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }

    var errorField by remember { mutableStateOf<ErrorField?>(null) }
    var errorMessage by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }

    fun validate(): Boolean {
        return when {
            oldPass.isBlank() -> {
                errorField = ErrorField.OLD
                errorMessage = "Vui lòng nhập mật khẩu cũ"
                false
            }

            newPass.isBlank() -> {
                errorField = ErrorField.NEW
                errorMessage = "Vui lòng nhập mật khẩu mới"
                false
            }

            newPass.length < 6 -> {
                errorField = ErrorField.NEW
                errorMessage = "Mật khẩu phải ≥ 6 ký tự"
                false
            }

            confirmPass.isBlank() -> {
                errorField = ErrorField.CONFIRM
                errorMessage = "Vui lòng xác nhận mật khẩu"
                false
            }

            confirmPass != newPass -> {
                errorField = ErrorField.CONFIRM
                errorMessage = "Mật khẩu xác nhận không khớp"
                false
            }

            else -> {
                errorField = null
                errorMessage = ""
                true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đổi mật khẩu") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
        ) {

            /* ===== OLD PASSWORD ===== */
            OutlinedTextField(
                value = oldPass,
                onValueChange = {
                    oldPass = it
                    if (errorField == ErrorField.OLD) errorField = null
                },
                label = { Text("Mật khẩu cũ") },
                visualTransformation = PasswordVisualTransformation(),
                isError = errorField == ErrorField.OLD,
                supportingText = {
                    if (errorField == ErrorField.OLD)
                        Text(errorMessage, color = MaterialTheme.colorScheme.error)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            /* ===== NEW PASSWORD ===== */
            OutlinedTextField(
                value = newPass,
                onValueChange = {
                    newPass = it
                    if (errorField == ErrorField.NEW) errorField = null
                },
                label = { Text("Mật khẩu mới") },
                visualTransformation = PasswordVisualTransformation(),
                isError = errorField == ErrorField.NEW,
                supportingText = {
                    if (errorField == ErrorField.NEW)
                        Text(errorMessage, color = MaterialTheme.colorScheme.error)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            /* ===== CONFIRM PASSWORD ===== */
            OutlinedTextField(
                value = confirmPass,
                onValueChange = {
                    confirmPass = it
                    if (errorField == ErrorField.CONFIRM) errorField = null
                },
                label = { Text("Xác nhận mật khẩu mới") },
                visualTransformation = PasswordVisualTransformation(),
                isError = errorField == ErrorField.CONFIRM,
                supportingText = {
                    if (errorField == ErrorField.CONFIRM)
                        Text(errorMessage, color = MaterialTheme.colorScheme.error)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading,
                onClick = {
                    if (!validate()) return@Button

                    loading = true
                    userViewModel.changePassword(
                        userId = userId,
                        oldPass = oldPass,
                        newPass = newPass
                    ) { success, msg ->
                        loading = false
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

                        if (success) {
                            sessionManager.logout()
                            navController.navigate("login") {
                                popUpTo(0)
                            }
                        }
                    }
                }
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Đổi mật khẩu")
                }
            }
        }
    }
}
