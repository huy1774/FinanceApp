package com.example.appqlchitieu.viewmodel

import androidx.lifecycle.*
import com.example.appqlchitieu.model.Category
import com.example.appqlchitieu.repository.CategoryRepository
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val repo: CategoryRepository,
    private val userId: Int
) : ViewModel() {

    /**  Tất cả danh mục của USER hiện tại */
    val allCategories: LiveData<List<Category>> =
        repo.allCategories(userId).asLiveData()

    /**  Lọc theo loại "expense" | "income" của USER hiện tại */
    fun categoriesByType(type: String): LiveData<List<Category>> =
        repo.categoriesByType(userId, type).asLiveData()

    /** Thêm */
    fun insert(category: Category) = viewModelScope.launch {
        repo.insert(category)
    }

    /** Sửa */
    fun update(category: Category) = viewModelScope.launch {
        repo.update(category)
    }

    /** Xóa */
    fun delete(category: Category) = viewModelScope.launch {
        repo.delete(category)
    }
}

class CategoryViewModelFactory(
    private val repo: CategoryRepository,
    private val userId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryViewModel(repo, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
