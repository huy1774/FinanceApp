package com.example.appqlchitieu.viewmodel

import androidx.lifecycle.*
import com.example.appqlchitieu.model.User
import com.example.appqlchitieu.repository.UserRepository
import kotlinx.coroutines.launch

/**
 * Quản lý trạng thái người dùng hiện tại (đăng nhập / thông tin cá nhân).
 */
class UserViewModel(private val repo: UserRepository) : ViewModel() {

    private val _currentUser = MutableLiveData<User?>(null)
    val currentUser: LiveData<User?> = _currentUser

    fun insert(user: User) = viewModelScope.launch { repo.insert(user) }

    /** Đăng nhập → cập nhật currentUser nếu thành công */
    fun login(email: String, password: String) = viewModelScope.launch {
        _currentUser.value = repo.login(email, password)
    }

    /** Cập nhật lại thông tin user (đổi tên, avatar, …) */
    fun update(user: User) = viewModelScope.launch { repo.update(user) }

    /** Lấy user theo id (one-shot) và đẩy vào currentUser */
    fun loadUser(id: Int) = viewModelScope.launch {
        _currentUser.value = repo.getById(id)
    }

    /** Đăng xuất “nhẹ” trong bộ nhớ (tuỳ workflow của em) */
    fun logout() { _currentUser.value = null }
}

class UserViewModelFactory(private val repo: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
