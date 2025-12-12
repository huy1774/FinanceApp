package com.example.appqlchitieu.repository

import androidx.room.withTransaction
import com.example.appqlchitieu.database.AppDatabase
import com.example.appqlchitieu.model.Expense

/**
 * Giao dịch đa-bảng: thêm Expense + cập nhật số dư ví trong 1 transaction.
 * Đã nâng cấp theo đa tài khoản.
 */
class TransactionRepository(private val db: AppDatabase) {

    /**
     * Thêm 1 expense.
     * - type == "expense" -> trừ ví
     * - type == "income"  -> cộng ví
     *
     * Yêu cầu: expense MUST có userId đúng.
     */
    suspend fun addExpenseAndAffectWallet(expense: Expense) {
        db.withTransaction {
            // 1) Lưu giao dịch
            db.expenseDao().insertExpense(expense)

            // 2) Áp số dư cho ví tương ứng theo user
            val delta = if (expense.type == "expense") -expense.amount else expense.amount

            db.walletDao().updateBalanceDelta(
                userId = expense.userId,
                walletId = expense.walletId,
                delta = delta
            )
        }
    }
}
