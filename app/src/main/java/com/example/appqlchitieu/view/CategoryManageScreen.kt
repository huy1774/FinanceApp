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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appqlchitieu.database.DatabaseProvider
import com.example.appqlchitieu.model.Category
import com.example.appqlchitieu.utils.SessionManager
import com.example.appqlchitieu.utils.UserSession
import com.example.appqlchitieu.view.ui.theme.AppQLChiTieuTheme
import kotlinx.coroutines.launch

@Composable
fun CategoryManageScreen(
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val db = remember { DatabaseProvider.getDatabase(context) }
    val scope = rememberCoroutineScope()

    val sessionManager = remember { SessionManager(context) }
    val userSession = remember { UserSession(sessionManager) }
    val userId = userSession.userIdOrNull()

    if (userId == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Bạn chưa đăng nhập")
        }
        return
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<Category?>(null) }
    var query by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    val categories by db.categoryDao()
        .getAllCategories(userId)
        .collectAsState(initial = emptyList())

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
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB)))
            )
            .padding(16.dp, 16.dp, 16.dp, 0.dp)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0),
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { inner ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(inner)
                    .padding(horizontal = 16.dp)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onBack?.invoke() }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color(0xFF1976D2)
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Quản lý danh mục",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF212121)
                    )
                }

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Tìm theo tên/loại…") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF79C4F9),
                        unfocusedBorderColor = Color(0xFFCFE0EB),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 4.dp)
                ) {
                    if (expenseCats.isNotEmpty()) {
                        item {
                            Text(
                                "Chi tiêu",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF1E88E5)
                            )
                        }
                        items(expenseCats, key = { it.id }) { cat ->
                            CategoryItem(
                                cat = cat,
                                onEdit = { editTarget = cat },
                                onDelete = {
                                    scope.launch {
                                        val used = db.expenseDao().countByCategory(userId, cat.id) > 0
                                        if (used) {
                                            snackbarHostState.showSnackbar(
                                                "Danh mục đang được sử dụng, không thể xoá."
                                            )
                                        } else {
                                            db.categoryDao().deleteCategory(cat)
                                        }
                                    }
                                }
                            )
                        }
                    }

                    if (incomeCats.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Thu nhập",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF1E88E5)
                            )
                        }
                        items(incomeCats, key = { it.id }) { cat ->
                            CategoryItem(
                                cat = cat,
                                onEdit = { editTarget = cat },
                                onDelete = {
                                    scope.launch {
                                        val used = db.expenseDao().countByCategory(userId, cat.id) > 0
                                        if (used) {
                                            snackbarHostState.showSnackbar(
                                                "Danh mục đang được sử dụng, không thể xoá."
                                            )
                                        } else {
                                            db.categoryDao().deleteCategory(cat)
                                        }
                                    }
                                }
                            )
                        }
                    }

                    if (expenseCats.isEmpty() && incomeCats.isEmpty()) {
                        item {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Không có danh mục phù hợp", color = Color.Gray)
                            }
                        }
                    }
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF388E3C),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) { Text("Thêm danh mục") }

                Spacer(Modifier.height(12.dp))
            }
        }
    }

    if (showAddDialog) {
        CategoryEditDialog(
            title = "Thêm danh mục",
            initName = "",
            initType = "expense",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, type ->
                if (name.isBlank()) return@CategoryEditDialog
                scope.launch {
                    db.categoryDao().insertCategory(
                        Category(userId = userId, name = name.trim(), type = type)
                    )
                }
                showAddDialog = false
            }
        )
    }

    editTarget?.let { target ->
        CategoryEditDialog(
            title = "Sửa danh mục",
            initName = target.name,
            initType = target.type,
            onDismiss = { editTarget = null },
            onConfirm = { name, type ->
                if (name.isBlank()) return@CategoryEditDialog
                scope.launch {
                    db.categoryDao().updateCategory(
                        target.copy(name = name.trim(), type = type)
                    )
                }
                editTarget = null
            }
        )
    }
}

@Composable
private fun CategoryItem(
    cat: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val dotColor = if (cat.type == "income") Color(0xFF4CAF50) else Color(0xFFE53935)
            Box(Modifier.size(12.dp).background(dotColor, CircleShape))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(cat.name, style = MaterialTheme.typography.bodyLarge)
                Text(if (cat.type == "income") "Thu nhập" else "Chi tiêu", color = Color.Gray)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Sửa")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Xoá", tint = Color(0xFFE53935))
            }
        }
    }
}

@Composable
private fun CategoryEditDialog(
    title: String,
    initName: String,
    initType: String,
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
                    onValueChange = { name = it; error = null },
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
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isBlank()) {
                    error = "Tên không được trống"
                    return@TextButton
                }
                onConfirm(name, type)
            }) { Text("Lưu") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}
