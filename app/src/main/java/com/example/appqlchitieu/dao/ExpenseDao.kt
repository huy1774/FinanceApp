package com.example.appqlchitieu.dao

import androidx.room.*
import com.example.appqlchitieu.model.Expense
import kotlinx.coroutines.flow.Flow
@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT * FROM expense_table ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expense_table WHERE id = :id LIMIT 1")
    suspend fun getExpenseById(id: Int): Expense?

    @Query("SELECT * FROM expense_table WHERE categoryId = :categoryId ORDER BY date DESC")
    fun getExpensesByCategory(categoryId: Int): Flow<List<Expense>>

    @Query("SELECT * FROM expense_table WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getExpensesByDateRange(start: Long, end: Long): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expense_table WHERE type = 'expense'")
    fun getTotalExpense(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM expense_table WHERE type = 'income'")
    fun getTotalIncome(): Flow<Double?>
    @Query("SELECT COUNT(*) FROM expense_table WHERE categoryId = :categoryId")
    suspend fun countByCategory(categoryId: Int): Int

}
