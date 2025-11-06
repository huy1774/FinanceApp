package com.example.appqlchitieu.viewmodel

import androidx.lifecycle.*
import com.example.appqlchitieu.model.AIChat
import com.example.appqlchitieu.repository.AIChatRepository
import kotlinx.coroutines.launch

/**
 * ViewModel cho màn hình chat AI.
 * - Expose LiveData để Compose observeAsState() ổn định.
 * - Gói thao tác ghi DB trong viewModelScope.
 */
class AIChatViewModel(private val repo: AIChatRepository) : ViewModel() {

    /** LiveData lịch sử chat của 1 user (mới → cũ) */
    fun chats(userId: Int): LiveData<List<AIChat>> =
        repo.chatsByUser(userId).asLiveData()

    /** Thêm 1 message mới */
    fun insert(chat: AIChat) = viewModelScope.launch {
        repo.insert(chat)
    }
}

/** Factory để tạo ViewModel có sẵn repository */
class AIChatViewModelFactory(private val repo: AIChatRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AIChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AIChatViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
