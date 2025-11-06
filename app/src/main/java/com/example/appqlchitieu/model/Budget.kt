package com.example.appqlchitieu.model

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "budget_table")
data class Budget(

    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val categoryId: Int,
    val amountLimit: Double,
    val startDate: Long,
    val endDate: Long
)
