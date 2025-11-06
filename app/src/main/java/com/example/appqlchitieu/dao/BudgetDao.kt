package com.example.appqlchitieu.dao

import androidx.room.*
import com.example.appqlchitieu.model.Budget
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)
    @Update
    suspend fun updateBudget(budget: Budget)
    @Delete
    suspend fun deleteBudget(budget: Budget)

    @Query("SELECT * FROM budget_table")
    fun getAllBudgets(): Flow<List<Budget>>

    @Query("SELECT * FROM budget_table WHERE categoryId = :categoryId")
    fun getBudgetsByCategory(categoryId: Int): Flow<List<Budget>>

    @Query("SELECT * FROM budget_table WHERE startDate <= :currentDate AND endDate >= :currentDate")
    fun getActiveBudgets(currentDate: Long): Flow<List<Budget>>
}
