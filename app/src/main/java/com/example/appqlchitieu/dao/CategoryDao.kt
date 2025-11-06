package com.example.appqlchitieu.dao

import androidx.room.*
import com.example.appqlchitieu.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM category_table")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM category_table WHERE type = :type")
    fun getCategoriesByType(type: String): Flow<List<Category>>
    @Query("SELECT COUNT(*) FROM category_table")
    suspend fun countAll(): Int

    @Insert
    suspend fun insertMany(categories: List<Category>)

}
