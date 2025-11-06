package com.example.appqlchitieu.model

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "expense_table")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val categoryId: Int,
    val walletId: Int,
    val date: Long,
    val type: String
)
