package com.example.appqlchitieu.viewmodel

import androidx.lifecycle.*
import com.example.appqlchitieu.model.Budget
import com.example.appqlchitieu.repository.BudgetRepository
import kotlinx.coroutines.launch
import androidx.lifecycle.asLiveData

/**
 * Quản lý ngân sách (Budget) cho UI.
 * Cầu nối giữa UI ↔ Repo: UI không cần biết DAO/Room.
 */
class BudgetViewModel(private val repo: BudgetRepository) : ViewModel() {

    /** Toàn bộ ngân sách (tự cập nhật khi DB đổi) */
    val allBudgets: LiveData<List<Budget>> = repo.allBudgets.asLiveData()

    fun insert(budget: Budget) = viewModelScope.launch { repo.insert(budget) }
    fun update(budget: Budget) = viewModelScope.launch { repo.update(budget) }
    fun delete(budget: Budget) = viewModelScope.launch { repo.delete(budget) }

    /** Ngân sách theo danh mục (VD: Ăn uống) */
    fun byCategory(categoryId: Int) = repo.byCategory(categoryId).asLiveData()

    /** Ngân sách đang hiệu lực ngay bây giờ */
    fun activeNow() = repo.activeAt(System.currentTimeMillis()).asLiveData()

    /** Ngân sách đang hiệu lực tại 1 thời điểm tuỳ ý (timestamp millis) */
    fun activeAt(ts: Long) = repo.activeAt(ts).asLiveData()
}

class BudgetViewModelFactory(private val repo: BudgetRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BudgetViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
