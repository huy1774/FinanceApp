package com.example.appqlchitieu.dao

import androidx.room.*
import com.example.appqlchitieu.model.AIChat
import kotlinx.coroutines.flow.Flow

//Lưu và lấy lịch sử hội thoại giữa người dùng và AI.
@Dao
interface AIChatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: AIChat)
    // Thêm một tin nhắn mới vào bảng.
    // Nếu có bản ghi trùng id thì ghi đè (REPLACE).

    @Query("SELECT * FROM aichat_table WHERE userId = :userId ORDER BY createdAt DESC")
    fun getChatsByUser(userId: Int): Flow<List<AIChat>>
    // Lấy danh sách tin nhắn của 1 người dùng, sắp xếp từ mới nhất → cũ nhất.

    @Query("DELETE FROM aichat_table WHERE userId = :userId")
    suspend fun deleteAllChats(userId: Int)
    // Xóa toàn bộ lịch sử chat của người dùng.

    @Query("SELECT * FROM aichat_table WHERE userId = :userId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLastMessage(userId: Int): AIChat?
    // Lấy tin nhắn gần nhất (dùng hiển thị ở màn hình chính).
}
