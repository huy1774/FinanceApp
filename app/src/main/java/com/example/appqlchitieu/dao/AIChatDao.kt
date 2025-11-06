package com.example.appqlchitieu.dao
import androidx.room.*
import com.example.appqlchitieu.model.AIChat
import kotlinx.coroutines.flow.Flow

@Dao
interface AIChatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: AIChat)


    @Query("SELECT * FROM aichat_table WHERE userId = :userId ORDER BY createdAt DESC")
    fun getChatsByUser(userId: Int): Flow<List<AIChat>>

    @Query("DELETE FROM aichat_table WHERE userId = :userId")
    suspend fun deleteAllChats(userId: Int)

    @Query("SELECT * FROM aichat_table WHERE userId = :userId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLastMessage(userId: Int): AIChat?
}
