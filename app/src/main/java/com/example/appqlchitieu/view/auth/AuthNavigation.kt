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

fun NavGraphBuilder.AuthNavigation(
    nav: NavHostController,
    vm: UserViewModel,
    sessionManager: SessionManager
) {

    composable("login") {
        LoginScreen(
            nav = nav,
            vm = vm,
            sessionManager = sessionManager,
            onRegisterClick = { nav.navigate("register") },
            onLoginSuccess = {
                nav.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }
        )
    }

    composable("register") {
        RegisterScreen(
            nav = nav,
            vm = vm,
            onLoginClick = { nav.navigate("login") },
            onRegisterSuccess = { email ->
                vm.generateOtp(email) { sent ->
                    if (sent) {
                        nav.navigate("verify/$email")
                    } else {
                        Toast.makeText(nav.context, "Không gửi được OTP", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    composable("verify/{email}") { backStack ->
        val email = backStack.arguments?.getString("email") ?: ""

        VerifyEmailScreen(
            email = email,
            vm = vm,
            onBackToLogin = { nav.navigate("login") },
            onResendCode = { vm.generateOtp(email) {} },
            onVerifySuccess = {
                // ✅ OTP OK thì coi như auto-login
                vm.loginAfterVerify(email) { ok ->
                    if (ok) {
                        nav.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        Toast.makeText(nav.context, "Không tìm thấy user sau xác thực", Toast.LENGTH_SHORT).show()
                        nav.navigate("login") {
                            popUpTo("verify/$email") { inclusive = true }
                        }
                    }
                }
            }
        )
    }
}
