package com.example.appqlchitieu.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "aichat_table")
data class AIChat(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val question: String,
    val response: String,
    val createdAt: Long = System.currentTimeMillis()
)
