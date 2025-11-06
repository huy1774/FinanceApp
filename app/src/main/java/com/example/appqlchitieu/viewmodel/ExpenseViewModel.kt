// app/src/main/java/com/example/appqlchitieu/viewmodel/ExpenseViewModel.kt
package com.example.appqlchitieu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.appqlchitieu.model.Expense
import com.example.appqlchitieu.repository.ExpenseRepository
import kotlinx.coroutines.launch

/**
 * Quản lý giao dịch (Expense).
 * Ngoài CRUD, expose các tổng hợp như tổng thu/chi hoặc lọc theo thời gian.
 */
class ExpenseViewModel(private val repo: ExpenseRepository) : ViewModel() {

    /** Danh sách giao dịch mới → cũ */
    val allExpenses = repo.allExpenses.asLiveData()

    fun insert(expense: Expense) = viewModelScope.launch { repo.insert(expense) }
    fun update(expense: Expense) = viewModelScope.launch { repo.update(expense) }
    fun delete(expense: Expense) = viewModelScope.launch { repo.delete(expense) }

    /** Lấy 1 giao dịch (one-shot, gọi trong coroutine ở UI) */
    suspend fun getById(id: Int) = repo.getById(id)

    /** Lọc theo danh mục (đã thêm trong Repo) */
    fun byCategory(categoryId: Int) = repo.byCategory(categoryId).asLiveData()

    /** Lọc theo khoảng ngày (đã thêm trong Repo) */
    fun byDateRange(start: Long, end: Long) = repo.byDateRange(start, end).asLiveData()

    /** Tổng chi và tổng thu toàn thời gian (đã thêm trong Repo) */
    val totalExpense = repo.totalExpense.asLiveData()
    val totalIncome  = repo.totalIncome.asLiveData()

    /** Tổng chi/thu trong khoảng ngày (đã có sẵn trong Repo) */
    fun totalExpenseBetween(start: Long, end: Long) =
        repo.totalExpenseBetween(start, end).asLiveData()

    fun totalIncomeBetween(start: Long, end: Long) =
        repo.totalIncomeBetween(start, end).asLiveData()
}

class ExpenseViewModelFactory(private val repo: ExpenseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
