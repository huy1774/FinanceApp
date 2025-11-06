package com.example.appqlchitieu.database

import com.example.appqlchitieu.model.*
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.appqlchitieu.dao.*

@Database(
    entities = [
        Expense::class,
        Category::class,
        Wallet::class,
        Budget::class,
        User::class,
        AIChat::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun walletDao(): WalletDao
    abstract fun budgetDao(): BudgetDao
    abstract fun userDao(): UserDao
    abstract fun aiChatDao(): AIChatDao
}
