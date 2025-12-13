package com.example.appqlchitieu.repository

import com.example.appqlchitieu.dao.CategoryDao
import com.example.appqlchitieu.model.Category
import kotlinx.coroutines.flow.Flow

class CategoryRepository(
    private val categoryDao: CategoryDao
) {

    fun allCategories(userId: Int) =
        categoryDao.getAllCategories(userId)

    fun categoriesByType(userId: Int, type: String) =
        categoryDao.getCategoriesByType(userId, type)

    suspend fun getAllOnce(userId: Int): List<Category> =
        categoryDao.getAllOnce(userId)

    suspend fun insert(category: Category) =
        categoryDao.insertCategory(category)

    suspend fun update(category: Category) =
        categoryDao.updateCategory(category)

    suspend fun delete(category: Category) =
        categoryDao.deleteCategory(category)
}

