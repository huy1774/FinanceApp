package com.example.appqlchitieu.navigation

import android.widget.Toast
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.appqlchitieu.ui.auth.LoginScreen
import com.example.appqlchitieu.ui.auth.RegisterScreen
import com.example.appqlchitieu.ui.auth.VerifyEmailScreen
import com.example.appqlchitieu.utils.SessionManager
import com.example.appqlchitieu.viewmodel.UserViewModel

/**
 * Auth flow:
 * - Login
 * - Register
 * - Verify Email
 *
 * Sau khi login / verify thành công:
 *  - SessionManager đã lưu user + isLoggedIn
 *  - GỌI onLoginSuccess() để MainActivity cập nhật STATE
 */
fun NavGraphBuilder.AuthNavigation(
    nav: NavHostController,
    vm: UserViewModel,
    sessionManager: SessionManager,
    onLoginSuccess: () -> Unit
) {

    /* ================= LOGIN ================= */
    composable("login") {
        LoginScreen(
            nav = nav,
            vm = vm,
            sessionManager = sessionManager,
            onRegisterClick = {
                nav.navigate("register")
            },
            onLoginSuccess = {
                // 🔥 Session đã lưu xong trong LoginScreen
                onLoginSuccess()   // 👉 báo MainActivity recompose

                nav.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }
        )
    }

    /* ================= REGISTER ================= */
    composable("register") {
        RegisterScreen(
            nav = nav,
            vm = vm,
            onLoginClick = {
                nav.navigate("login") {
                    popUpTo("register") { inclusive = true }
                }
            },
            onRegisterSuccess = { email ->
                vm.generateOtp(email) { sent ->
                    if (sent) {
                        nav.navigate("verify/$email")
                    } else {
                        Toast.makeText(
                            nav.context,
                            "Không gửi được OTP",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )
    }

    /* ================= VERIFY EMAIL ================= */
    composable("verify/{email}") { backStack ->
        val email = backStack.arguments?.getString("email") ?: ""

        VerifyEmailScreen(
            email = email,
            vm = vm,
            onBackToLogin = {
                nav.navigate("login") {
                    popUpTo("verify/$email") { inclusive = true }
                }
            },
            onResendCode = {
                vm.generateOtp(email) {}
            },
            onVerifySuccess = {
                vm.loginAfterVerify(email) { ok ->
                    if (ok) {
                        // 🔥 Login xong + đã lưu session
                        onLoginSuccess()   // 👉 cập nhật isLoggedIn + userId

                        nav.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        Toast.makeText(
                            nav.context,
                            "Không tìm thấy user sau xác thực",
                            Toast.LENGTH_SHORT
                        ).show()

                        nav.navigate("login") {
                            popUpTo("verify/$email") { inclusive = true }
                        }
                    }
                }
            }
        )
    }
}
