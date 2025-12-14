package com.example.appqlchitieu.navigation

import android.widget.Toast
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.appqlchitieu.ui.auth.*
import com.example.appqlchitieu.utils.SessionManager
import com.example.appqlchitieu.view.auth.ForgotPasswordScreen
import com.example.appqlchitieu.view.auth.ResetPasswordScreen
import com.example.appqlchitieu.viewmodel.UserViewModel

fun NavGraphBuilder.AuthNavigation(
    nav: NavHostController,
    vm: UserViewModel,
    sessionManager: SessionManager,
    onLoginSuccess: () -> Unit
) {
    // 1. LOGIN
    composable("login") {
        LoginScreen(
            nav = nav,
            vm = vm,
            sessionManager = sessionManager,
            onRegisterClick = { nav.navigate("register") },
            onLoginSuccess = {
                onLoginSuccess()
                nav.navigate("home") { popUpTo("login") { inclusive = true } }
            }
        )
    }

    // 2. REGISTER
    composable("register") {
        RegisterScreen(
            nav = nav,
            vm = vm,
            onLoginClick = {
                nav.navigate("login") { popUpTo("register") { inclusive = true } }
            },
            onRegisterSuccess = { email ->
                // Đăng ký thành công -> Gửi OTP -> Sang trang xác thực
                vm.generateOtp(email, VerifyPurpose.REGISTER) { sent ->
                    if (sent) nav.navigate("verify/$email/REGISTER")
                    else Toast.makeText(nav.context, "Lỗi kết nối, không gửi được OTP", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // 3. VERIFY OTP (Dùng chung cho Đăng ký và Quên mật khẩu)
    composable("verify/{email}/{purpose}") { backStack ->
        val email = backStack.arguments?.getString("email") ?: ""
        val purposeStr = backStack.arguments?.getString("purpose") ?: "REGISTER"
        val purpose = try { VerifyPurpose.valueOf(purposeStr) } catch (e: Exception) { VerifyPurpose.REGISTER }

        VerifyEmailScreen(
            email = email,
            vm = vm,
            purpose = purpose,
            onVerifySuccess = {
                if (purpose == VerifyPurpose.REGISTER) {
                    // Nếu là Đăng ký -> Login luôn
                    vm.loginAfterVerify(email) { ok ->
                        if (ok) {
                            onLoginSuccess()
                            nav.navigate("home") { popUpTo("login") { inclusive = true } }
                        }
                    }
                } else {
                    // Nếu là Quên mật khẩu -> Sang trang đổi pass
                    nav.navigate("reset_password/$email")
                }
            },
            onBack = {
                // Logic nút Back
                if (purpose == VerifyPurpose.REGISTER) {
                    nav.navigate("register") { popUpTo("register") { inclusive = true } }
                } else {
                    nav.navigate("forgot_password") { popUpTo("forgot_password") { inclusive = true } }
                }
            },
            onResendCode = {
                // Logic Gửi lại mã
                vm.generateOtp(email, purpose) { sent ->
                    val msg = if (sent) "Đã gửi lại mã OTP mới tới $email" else "Gửi thất bại. Kiểm tra mạng!"
                    Toast.makeText(nav.context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // 4. FORGOT PASSWORD (Nhập Email)
    composable("forgot_password") {
        ForgotPasswordScreen(
            nav = nav,
            vm = vm,
            onBack = {
                nav.navigate("login") { popUpTo("login") { inclusive = true } }
            }
        )
    }

    // 5. RESET PASSWORD (Nhập mật khẩu mới)
    composable("reset_password/{email}") { backStack ->
        val email = backStack.arguments?.getString("email") ?: return@composable
        ResetPasswordScreen(
            email = email,
            nav = nav,
            vm = vm,
            onBack = {
                // Back về trang OTP
                nav.popBackStack()
            }
        )
    }
}