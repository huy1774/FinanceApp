package com.example.appqlchitieu.repository

import com.example.appqlchitieu.database.AppDatabase
import com.example.appqlchitieu.model.Expense

class TransactionRepository(private val db: AppDatabase) {

    private val expenseDao = db.expenseDao()
    private val walletDao = db.walletDao()

    // 1. THÊM MỚI (Logic cũ của bạn, giữ nguyên hoặc chuẩn hóa)
    suspend fun addExpenseAndAffectWallet(expense: Expense) {
        // Cập nhật ví
        val delta = if (expense.type == "expense") -expense.amount else expense.amount
        walletDao.updateBalanceDelta(expense.userId, expense.walletId, delta)

        // Lưu giao dịch
        expenseDao.insertExpense(expense)
    }

    // 2. XÓA GIAO DỊCH (Hoàn tác số dư -> Xóa)
    suspend fun deleteExpense(expense: Expense) {
        // Nếu là Expense (Chi): Lúc trước đã trừ, giờ xóa thì phải CỘNG lại
        // Nếu là Income (Thu): Lúc trước đã cộng, giờ xóa thì phải TRỪ đi
        val revertDelta = if (expense.type == "expense") expense.amount else -expense.amount

        walletDao.updateBalanceDelta(expense.userId, expense.walletId, revertDelta)
        expenseDao.deleteExpense(expense)
    }

    // 3. CẬP NHẬT GIAO DỊCH (Hoàn tác cái cũ -> Áp dụng cái mới -> Update)
    suspend fun updateExpense(oldExpense: Expense, newExpense: Expense) {
        // Bước 1: Hoàn tác giao dịch cũ (trả lại tiền về ví cũ)
        val revertOldDelta = if (oldExpense.type == "expense") oldExpense.amount else -oldExpense.amount
        walletDao.updateBalanceDelta(oldExpense.userId, oldExpense.walletId, revertOldDelta)

        // Bước 2: Áp dụng giao dịch mới (trừ/cộng tiền vào ví mới)
        val applyNewDelta = if (newExpense.type == "expense") -newExpense.amount else newExpense.amount
        walletDao.updateBalanceDelta(newExpense.userId, newExpense.walletId, applyNewDelta)

        // Bước 3: Cập nhật thông tin giao dịch
        expenseDao.updateExpense(newExpense)
    }
}