package com.example.appqlchitieu.dao

import androidx.room.*
import com.example.appqlchitieu.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    // Flow – dùng cho UI
    @Query("SELECT * FROM category_table WHERE userId = :userId")
    fun getAllCategories(userId: Int): Flow<List<Category>>

    @Query("""
        SELECT * FROM category_table 
        WHERE userId = :userId AND type = :type
    """)
    fun getCategoriesByType(
        userId: Int,
        type: String
    ): Flow<List<Category>>

    // 🔥 THÊM HÀM NÀY – DÙNG CHO AIChat / Debug / One-shot
    @Query("SELECT * FROM category_table WHERE userId = :userId")
    suspend fun getAllOnce(userId: Int): List<Category>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)
}
