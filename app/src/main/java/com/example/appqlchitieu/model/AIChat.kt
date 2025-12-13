package com.example.appqlchitieu.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "aichat_table")
data class AIChat(

    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    // id là khóa chính của bảng.

    val userId: Int,
    // userId là ID của người dùng gửi câu hỏi.

    val question: String,
    // question là câu hỏi người dùng nhập.

    val response: String,
    // response là câu trả lời của AI.

    val createdAt: Long = System.currentTimeMillis()
    // createdAt là thời gian lưu đoạn chat (tính theo mili-giây).
)
