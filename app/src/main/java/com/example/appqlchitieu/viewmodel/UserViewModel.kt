package com.example.appqlchitieu.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.*
import com.example.app.data.datastore.UserDataStore
import com.example.appqlchitieu.model.User
import com.example.appqlchitieu.repository.UserRepository
import com.example.appqlchitieu.utils.EmailSender
import com.example.appqlchitieu.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Quản lý trạng thái người dùng hiện tại (đăng nhập / thông tin cá nhân).
 */
class UserViewModel(private val repo: UserRepository, private val dataStore: UserDataStore, private val sessionManager: SessionManager) : ViewModel() {

    val userName = dataStore.userName
    val userEmail = dataStore.userEmail

    private val _currentUser = MutableLiveData<User?>(null)
    val currentUser: LiveData<User?> = _currentUser

    fun insert(user: User) = viewModelScope.launch { repo.insert(user) }

    var otpCode by mutableStateOf("")
    var otpEmail by mutableStateOf("")
    var otpExpireTime by mutableStateOf(0L)

    /** Đăng nhập → cập nhật currentUser nếu thành công */

    fun login(context: Context, email: String, pass: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = repo.login(email, pass)

            if (user != null) {
                _currentUser.value = user
                dataStore.saveUser(user.name, user.email)
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    fun getUserByEmail(email: String, onResult: (User?) -> Unit) {
        viewModelScope.launch {
            val user = repo.getByEmail(email)
            onResult(user)
        }
    }

    fun loginAfterVerify(email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = repo.getByEmail(email)
            if (user != null) {
                _currentUser.value = user
                dataStore.saveUser(user.name, user.email)
                sessionManager.saveLogin(user.id) // KEY CHÍNH Ở ĐÂY
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    fun register(user: User, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        val existed = repo.getByEmail(user.email) // repo phải có hàm này
        if (existed == null) {
            repo.insert(user)
            onResult(true)
        } else {
            onResult(false)
        }
    }

    /** Cập nhật lại thông tin user (đổi tên, avatar, …) */
    fun update(user: User) = viewModelScope.launch { repo.update(user) }

    /** Lấy user theo id (one-shot) và đẩy vào currentUser */
    fun loadUser(id: Int) = viewModelScope.launch {
        _currentUser.value = repo.getById(id)
    }

    /** Đăng xuất “nhẹ” trong bộ nhớ (tuỳ workflow của em) */
    fun logout(onLoggedOut: () -> Unit = {}) {
        viewModelScope.launch {
            dataStore.clearUser()        // Xóa tên + email trong datastore
            sessionManager.logout()      // Xóa flag đăng nhập
            onLoggedOut()
        }
    }

    fun generateOtp(email: String, callback: (Boolean) -> Unit) {
        val otp = (100000..999999).random().toString()
        otpCode = otp
        otpEmail = email
        otpExpireTime = System.currentTimeMillis() + 2 * 60 * 1000

        EmailSender.sendOtp(email, otp) { success ->
            // callback đã ở MainThread, nhưng để chắc chắn:
            viewModelScope.launch(Dispatchers.Main) {
                callback(success)
            }
        }
    }

    fun verifyOtp(input: String): Boolean {
        val now = System.currentTimeMillis()

        return input == otpCode && now <= otpExpireTime
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
