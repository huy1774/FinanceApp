@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.appqlchitieu.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appqlchitieu.database.DatabaseProvider
import com.example.appqlchitieu.model.Category
import com.example.appqlchitieu.repository.CategoryRepository
import com.example.appqlchitieu.utils.SessionManager
import com.example.appqlchitieu.utils.UserSession
import com.example.appqlchitieu.viewmodel.CategoryViewModel
import com.example.appqlchitieu.viewmodel.CategoryViewModelFactory
import androidx.compose.runtime.livedata.observeAsState

@Composable
fun CategoryManageScreen(
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val userSession = remember { UserSession(sessionManager) }
    val userId = userSession.userIdOrNull()

    if (userId == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Bạn chưa đăng nhập")
        }
        return
    }

    val db = DatabaseProvider.getDatabase(context)
    val repo = remember { CategoryRepository(db.categoryDao()) }

    val vm: CategoryViewModel = viewModel(
        factory = CategoryViewModelFactory(repo, userId)
    )

    val categories by vm.allCategories.observeAsState(emptyList())

    var showAddDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<Category?>(null) }
    var query by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    val filtered = remember(categories, query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) categories
        else categories.filter {
            it.name.lowercase().contains(q) || it.type.lowercase().contains(q)
        }
    }

    val expenseCats = filtered.filter { it.type == "expense" }
    val incomeCats = filtered.filter { it.type == "income" }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))
                )
            )
            .padding(16.dp, 16.dp, 16.dp, 0.dp)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { inner ->

            Column(
                Modifier
                    .fillMaxSize()
                    .padding(inner)
            ) {

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onBack?.invoke() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        "Quản lý danh mục",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Tìm theo tên / loại") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    if (expenseCats.isNotEmpty()) {
                        item { SectionTitle("Chi tiêu") }
                        items(expenseCats, key = { it.id }) {
                            CategoryItem(it, { editTarget = it }) {
                                vm.delete(it)
                            }
                        }
                    }

                    if (incomeCats.isNotEmpty()) {
                        item { SectionTitle("Thu nhập") }
                        items(incomeCats, key = { it.id }) {
                            CategoryItem(it, { editTarget = it }) {
                                vm.delete(it)
                            }
                        }
                    }

                    if (expenseCats.isEmpty() && incomeCats.isEmpty()) {
                        item {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Không có danh mục")
                            }
                        }
                    }
                }

                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Thêm danh mục")
                }
            }
        }
    }

    /** ADD */
    if (showAddDialog) {
        CategoryEditDialog(
            title = "Thêm danh mục",
            initName = "",
            initType = "expense",
            categories = categories,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, type ->
                vm.insert(
                    Category(
                        userId = userId,
                        name = name.trim(),
                        type = type
                    )
                )
                showAddDialog = false
            }
        )
    }

    /** EDIT */
    editTarget?.let { target ->
        CategoryEditDialog(
            title = "Sửa danh mục",
            initName = target.name,
            initType = target.type,
            categories = categories.filter { it.id != target.id },
            onDismiss = { editTarget = null },
            onConfirm = { name, type ->
                vm.update(target.copy(name = name.trim(), type = type))
                editTarget = null
            }
        )
    }
}

/* ---------------- UI COMPONENTS ---------------- */

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        color = Color(0xFF1E88E5)
    )
}

@Composable
private fun CategoryItem(
    cat: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(12.dp)
                    .background(
                        if (cat.type == "income") Color(0xFF4CAF50)
                        else Color(0xFFE53935),
                        CircleShape
                    )
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(cat.name)
                Text(
                    if (cat.type == "income") "Thu nhập" else "Chi tiêu",
                    color = Color.Gray
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red
                )
            }
        }
    }
}

/* ---------------- DIALOG ---------------- */

@Composable
private fun CategoryEditDialog(
    title: String,
    initName: String,
    initType: String,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initName) }
    var type by remember { mutableStateOf(initType) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        error = null
                    },
                    label = { Text("Tên danh mục") },
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == "expense",
                        onClick = { type = "expense" },
                        label = { Text("Chi tiêu") }
                    )
                    FilterChip(
                        selected = type == "income",
                        onClick = { type = "income" },
                        label = { Text("Thu nhập") }
                    )
                }
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val trimmed = name.trim()
                if (trimmed.isEmpty()) {
                    error = "Tên không được trống"
                    return@TextButton
                }

                val duplicate = categories.any {
                    it.type == type && it.name.equals(trimmed, true)
                }
                if (duplicate) {
                    error = "Danh mục đã tồn tại trong loại này"
                    return@TextButton
                }

                onConfirm(trimmed, type)
            }) { Text("Lưu") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}
