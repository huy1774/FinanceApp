package com.example.appqlchitieu.repository

import com.example.appqlchitieu.dao.CategoryDao
import com.example.appqlchitieu.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CategoryRepository(private val categoryDao: CategoryDao) {

    // Luồng toàn bộ danh mục theo user
    fun allCategories(userId: Int) = categoryDao.getAllCategories(userId)

    suspend fun getAllOnce(userId: Int): List<Category> =
        categoryDao.getAllCategoriesOnce(userId)


    // Lọc theo loại "expense" | "income" theo user
    fun categoriesByType(userId: Int, type: String): Flow<List<Category>> =
        categoryDao.getCategoriesByType(userId, type)

    // Đếm danh mục theo user (phục vụ seed default)
    suspend fun countAll(userId: Int): Int =
        categoryDao.countAll(userId)

    // Thêm mới
    suspend fun insert(category: Category) =
        categoryDao.insertCategory(category)

    // Sửa
    suspend fun update(category: Category) =
        categoryDao.updateCategory(category)

    // Xóa
    suspend fun delete(category: Category) =
        categoryDao.deleteCategory(category)

    // Insert nhiều danh mục (seed)
    suspend fun insertMany(categories: List<Category>) =
        categoryDao.insertMany(categories)
}
