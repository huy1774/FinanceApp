// app/src/main/java/com/example/appqlchitieu/view/WalletScreen.kt
package com.example.appqlchitieu.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appqlchitieu.database.DatabaseProvider
import com.example.appqlchitieu.model.Wallet
import com.example.appqlchitieu.repository.WalletRepository
import com.example.appqlchitieu.view.ui.theme.AppQLChiTieuTheme
import com.example.appqlchitieu.viewmodel.WalletViewModel
import com.example.appqlchitieu.viewmodel.WalletViewModelFactory
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*
import com.example.appqlchitieu.utils.SessionManager
import com.example.appqlchitieu.utils.UserSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(onBack: (() -> Unit)? = null) {
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

    val db = remember(context) { DatabaseProvider.getDatabase(context) }
    val repo = remember { WalletRepository(db.walletDao()) }
    val vm: WalletViewModel = viewModel(factory = WalletViewModelFactory(repo, userId))
    val scope = rememberCoroutineScope()

    val wallets by vm.allWallets.observeAsState(emptyList())
    val total by remember(wallets) { derivedStateOf { wallets.sumOf { it.balance } } }
    val nf = remember { NumberFormat.getInstance(Locale("vi", "VN")) }

    var showAdd by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<Wallet?>(null) }
    var deleteTarget by remember { mutableStateOf<Wallet?>(null) }

    val gradient = remember {
        Brush.verticalGradient(listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB)))
    }

    // Nền chung đồng bộ với các màn hình
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        // Tắt insets mặc định của Scaffold để không bị đẩy nội dung xuống
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0)
        ) { inner ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(inner)
                    // chỉ padding ngang, KHÔNG padding top để dính ngay dưới AppBar chính
                    .padding(16.dp,16.dp,16.dp,0.dp)
            ) {
                // Hàng tiêu đề dưới AppBar "Quản lý chi tiêu"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onBack?.invoke() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color(0xFF1976D2)
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Ví của tôi",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF212121) // đen
                    )
                }

                Text("Tổng: ${nf.format(total)}₫", color = Color.Gray)
                Spacer(Modifier.height(12.dp))

                if (wallets.isEmpty()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Chưa có ví nào", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(wallets, key = { it.id }) { w ->
                            WalletCard(
                                wallet = w,
                                nf = nf,
                                onEdit = { editTarget = w },
                                onDelete = { deleteTarget = w }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { showAdd = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF388E3C),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Thêm ví")
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }

    if (showAdd) {
        WalletEditDialog(
            title = "Thêm ví",
            initName = "",
            initBalance = "",
            onDismiss = { showAdd = false },
            onConfirm = { name, balance ->
                scope.launch {
                    db.walletDao().insertWallet(Wallet(userId = userId, name = name, balance = balance))
                }
                showAdd = false
            }
        )
    }

    editTarget?.let { w ->
        WalletEditDialog(
            title = "Sửa ví",
            initName = w.name,
            initBalance = w.balance.toString(),
            onDismiss = { editTarget = null },
            onConfirm = { name, balance ->
                scope.launch { db.walletDao().updateWallet(w.copy(name = name, balance = balance)) }
                editTarget = null
            }
        )
    }

    deleteTarget?.let { w ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Xóa ví") },
            text = { Text("Bạn có chắc muốn xóa ví \"${w.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { db.walletDao().deleteWallet(w) }
                    deleteTarget = null
                }) { Text("Xóa") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Hủy") } }
        )
    }
}

@Composable
private fun WalletCard(
    wallet: Wallet,
    nf: NumberFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(12.dp).background(Color(0xFF4CAF50), shape = CircleShape))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(wallet.name, style = MaterialTheme.typography.titleMedium)
                Text("${nf.format(wallet.balance)}₫", color = Color.Gray)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Sửa") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = Color(0xFFE53935)) }
        }
    }
}

@Composable
private fun WalletEditDialog(
    title: String,
    initName: String,
    initBalance: String,
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var name by remember { mutableStateOf(initName) }
    var balanceText by remember { mutableStateOf(initBalance) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên ví") })
                OutlinedTextField(value = balanceText, onValueChange = { balanceText = it }, label = { Text("Số dư ban đầu") })
                if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val bal = balanceText.toDoubleOrNull()
                    if (name.isBlank()) { error = "Tên ví không được trống"; return@TextButton }
                    if (bal == null) { error = "Số dư không hợp lệ"; return@TextButton }
                    onConfirm(name.trim(), bal)
                }
            ) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewWalletScreen() {
    AppQLChiTieuTheme { WalletScreen() }
}
