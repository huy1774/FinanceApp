package com.example.appqlchitieu.viewmodel

import androidx.lifecycle.*
import com.example.appqlchitieu.model.Category
import com.example.appqlchitieu.repository.CategoryRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CategoryViewModel(private val repo: CategoryRepository) : ViewModel() {

    /** Tất cả danh mục (realtime) */
    val allCategories: LiveData<List<Category>> = repo.allCategories.asLiveData()

    /** Lọc theo loại "expense" | "income" (lọc trên Flow cho nhẹ) */
    fun categoriesByType(type: String): LiveData<List<Category>> =
        repo.allCategories.map { it.filter { c -> c.type == type } }.asLiveData()

    /** Thêm */
    fun insert(category: Category) = viewModelScope.launch { repo.insert(category) }

    /** Sửa */
    fun update(category: Category) = viewModelScope.launch { repo.update(category) }

    /** Xóa */
    fun delete(category: Category) = viewModelScope.launch { repo.delete(category) }
}

class CategoryViewModelFactory(private val repo: CategoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
