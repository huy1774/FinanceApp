package com.example.appqlchitieu.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_table")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val userId: Int,
    val name: String,
    val type: String // "expense" | "income"
)
