package com.example.appqlchitieu.repository

import com.example.appqlchitieu.dao.AIChatDao
import com.example.appqlchitieu.model.AIChat
import kotlinx.coroutines.flow.Flow

/**
 * Repository cho lịch sử chat AI.
 * Dùng trong AIChatViewModel/AIChatActivity để lưu & lấy hội thoại của 1 user.
 */
class AIChatRepository(private val dao: AIChatDao) {

    /** Lấy lịch sử chat của user (mới nhất -> cũ) */
    fun chatsByUser(userId: Int): Flow<List<AIChat>> = dao.getChatsByUser(userId)

    /** Lưu 1 cặp question/response mới vào DB */
    suspend fun insert(chat: AIChat) = dao.insertChat(chat)
    // Thêm cho khớp DAO mới của em:
    suspend fun deleteAll(userId: Int) = dao.deleteAllChats(userId)
    suspend fun lastMessage(userId: Int) = dao.getLastMessage(userId)
}
