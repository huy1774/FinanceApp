package com.example.appqlchitieu.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.*
import com.example.app.data.datastore.UserDataStore
import com.example.appqlchitieu.model.User
import com.example.appqlchitieu.repository.UserRepository
import com.example.appqlchitieu.ui.auth.VerifyPurpose
import com.example.appqlchitieu.utils.EmailSender
import com.example.appqlchitieu.utils.SessionManager
import kotlinx.coroutines.launch
import kotlin.random.Random

class UserViewModel(
    private val repo: UserRepository,
    private val dataStore: UserDataStore,
    private val sessionManager: SessionManager
) : ViewModel() {

    var currentUser: User? by mutableStateOf(null)
        private set

    // OTP State
    private var otpCode by mutableStateOf("")
    private var otpEmail by mutableStateOf("")
    private var otpPurpose by mutableStateOf<VerifyPurpose?>(null)
    private var otpExpireTime by mutableStateOf(0L)

    // ===== LOGIN (ĐÃ SỬA LOGIC THÔNG BÁO) =====
    // Callback trả về (Success, Message)
    fun login(context: Context, email: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            // 1. Kiểm tra email có tồn tại không trước
            val userByEmail = repo.getByEmail(email)

            if (userByEmail == null) {
                // Không tìm thấy email -> Báo chưa có tài khoản
                onResult(false, "Tài khoản không tồn tại. Vui lòng đăng ký!")
            } else {
                // 2. Có email -> Kiểm tra mật khẩu (login)
                val userLogin = repo.login(email, password)
                if (userLogin != null) {
                    // Đúng pass
                    currentUser = userLogin
                    dataStore.saveUser(userLogin.name, userLogin.email)
                    sessionManager.saveLogin(userLogin.id)
                    onResult(true, "Đăng nhập thành công")
                } else {
                    // Sai pass
                    onResult(false, "Mật khẩu không đúng!")
                }
            }
        }
    }

    fun loginAfterVerify(email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = repo.getByEmail(email)
            if (user != null) {
                val verifiedUser = user.copy(isVerified = true)
                repo.updateUser(verifiedUser)

                currentUser = verifiedUser
                dataStore.saveUser(verifiedUser.name, verifiedUser.email)
                sessionManager.saveLogin(verifiedUser.id)
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    // ===== REGISTER =====
    fun register(user: User, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        val existed = repo.getByEmail(user.email)

        if (existed == null) {
            repo.insertUser(user)
            onResult(true)
        } else {
            if (existed.isVerified) {
                onResult(false)
            } else {
                val userToUpdate = user.copy(id = existed.id)
                repo.updateUser(userToUpdate)
                onResult(true)
            }
        }
    }

    // ===== OTP & EMAIL =====
    fun generateOtp(email: String, purpose: VerifyPurpose, callback: (Boolean) -> Unit) {
        val code = Random.nextInt(100000, 999999).toString()
        otpCode = code
        otpEmail = email
        otpPurpose = purpose
        otpExpireTime = System.currentTimeMillis() + 5 * 60 * 1000L

        EmailSender.sendOtp(email, code) { success ->
            callback(success)
        }
    }

    fun verifyOtp(email: String, inputOtp: String, purpose: VerifyPurpose): Boolean {
        val now = System.currentTimeMillis()
        return email == otpEmail && inputOtp == otpCode && purpose == otpPurpose && now <= otpExpireTime
    }

    // ===== RESET PASSWORD =====
    fun changePassword(userId: Int, newPass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repo.getUserById(userId)
            if (user == null) {
                onResult(false, "Không tìm thấy người dùng")
                return@launch
            }
            repo.updatePassword(userId, newPass)
            onResult(true, "Đổi mật khẩu thành công")
        }
    }

    // ===== GET USER BY EMAIL =====
    fun getUserByEmail(email: String, onResult: (User?) -> Unit) {
        viewModelScope.launch {
            val user = repo.getByEmail(email)
            onResult(user)
        }
    }
}

class UserViewModelFactory(
    private val repo: UserRepository,
    private val dataStore: UserDataStore,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(repo, dataStore, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}