package com.example.appqlchitieu.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.app.data.datastore.UserDataStore
import com.example.appqlchitieu.R
import com.example.appqlchitieu.database.DatabaseProvider
import com.example.appqlchitieu.navigation.AuthNavigation
import com.example.appqlchitieu.repository.*
import com.example.appqlchitieu.utils.SessionManager
import com.example.appqlchitieu.utils.UserSession
import com.example.appqlchitieu.view.ui.theme.AppQLChiTieuTheme
import com.example.appqlchitieu.viewmodel.*

// Các Enum cho màn hình con
private enum class TransactionSubScreen { LIST, ADD, UPDATE }
private enum class OverviewSubScreen { HOME, WALLET, CATEGORY }

private val BOTTOM_BAR_HEIGHT = 63.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val context = this
            val navController = rememberNavController()
            val sessionManager = remember { SessionManager(context) }
            val db = remember { DatabaseProvider.getDatabase(context) }

            // State quản lý trạng thái Login/Logout realtime
            var isUserLoggedIn by remember { mutableStateOf(sessionManager.isLoggedIn()) }
            var currentUserId by remember { mutableStateOf(sessionManager.getUserId()) }

            val userVM: UserViewModel = viewModel(
                factory = UserViewModelFactory(
                    UserRepository(db.userDao()),
                    UserDataStore(applicationContext),
                    sessionManager
                )
            )

            // --- SỬA LỖI TẠI ĐÂY: Load user ngay khi mở app nếu đã đăng nhập ---
            LaunchedEffect(isUserLoggedIn) {
                if (isUserLoggedIn && currentUserId != -1) {
                    userVM.loadUser(currentUserId)
                }
            }
            // ------------------------------------------------------------------

            val aiChatVM: AIChatViewModel = viewModel(
                factory = AIChatViewModelFactory(
                    AIChatRepository(db.aiChatDao()),
                    WalletRepository(db.walletDao()),
                    ExpenseRepository(db.expenseDao()),
                    CategoryRepository(db.categoryDao()),
                    BudgetRepository(db.budgetDao())
                )
            )

            var showAIChat by rememberSaveable { mutableStateOf(false) }

            AppQLChiTieuTheme {
                NavHost(
                    navController = navController,
                    startDestination = if (sessionManager.isLoggedIn()) "home" else "login"
                ) {
                    // Navigation cho Auth
                    AuthNavigation(
                        nav = navController,
                        vm = userVM,
                        sessionManager = sessionManager,
                        onLoginSuccess = {
                            isUserLoggedIn = true
                            currentUserId = sessionManager.getUserId()
                            showAIChat = false
                        }
                    )

                    composable("home") {
                        MainMenuScreen(
                            userViewModel = userVM,
                            navController = navController,
                            onLogoutSuccess = {
                                sessionManager.logout()
                                isUserLoggedIn = false
                                currentUserId = -1
                                showAIChat = false
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("change_password") {
                        ChangePasswordScreen(
                            userViewModel = userVM,
                            sessionManager = sessionManager,
                            navController = navController,
                            onLogoutSuccess = {
                                isUserLoggedIn = false
                                currentUserId = -1
                                showAIChat = false
                            }
                        )
                    }
                }

                // --- CHAT BUBBLE ---
                if (isUserLoggedIn && currentUserId != -1) {
                    ChatBubble(onClick = { showAIChat = true })

                    if (showAIChat) {
                        AIChatScreen(
                            viewModel = aiChatVM,
                            userId = currentUserId,
                            onClose = { showAIChat = false }
                        )
                    }
                }
            }
        }
    }
}

// ... (Giữ nguyên phần MainMenuScreen và MainBottomBar ở dưới) ...
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    userViewModel: UserViewModel,
    navController: NavHostController,
    onLogoutSuccess: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val userId = UserSession(sessionManager).userIdOrNull()

    if (userId == null) {
        LaunchedEffect(Unit) {
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        }
        return
    }

    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var tranSub by rememberSaveable { mutableStateOf(TransactionSubScreen.LIST) }
    var overviewSub by rememberSaveable { mutableStateOf(OverviewSubScreen.HOME) }
    var editId by rememberSaveable { mutableStateOf(-1) }

    val db = remember { DatabaseProvider.getDatabase(context) }
    val walletVM: WalletViewModel = viewModel(
        factory = WalletViewModelFactory(WalletRepository(db.walletDao()), userId)
    )
    val totalBalance by walletVM.totalBalance.observeAsState(0.0)

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF6A5ACD), Color(0xFF1976D2))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("Quản lý chi tiêu", color = Color.White)
            }
        },
        bottomBar = {
            MainBottomBar(
                selectedTab = selectedTab,
                onSelect = { index ->
                    selectedTab = index
                    if (index == 0) overviewSub = OverviewSubScreen.HOME
                    if (index == 1) tranSub = TransactionSubScreen.LIST
                },
                onQuickAdd = {
                    selectedTab = 1
                    tranSub = TransactionSubScreen.ADD
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 -> when (overviewSub) {
                    OverviewSubScreen.HOME -> OverviewScreen(
                        totalBalance = totalBalance,
                        onNavigateToWallet = { overviewSub = OverviewSubScreen.WALLET },
                        onNavigateToCategory = { overviewSub = OverviewSubScreen.CATEGORY }
                    )
                    OverviewSubScreen.WALLET -> WalletScreen(onBack = { overviewSub = OverviewSubScreen.HOME })
                    OverviewSubScreen.CATEGORY -> CategoryManageScreen(onBack = { overviewSub = OverviewSubScreen.HOME })
                }
                1 -> when (tranSub) {
                    TransactionSubScreen.LIST -> TransactionScreen(
                        onAddClick = { tranSub = TransactionSubScreen.ADD },
                        onEdit = {
                            editId = it
                            tranSub = TransactionSubScreen.UPDATE
                        }
                    )
                    TransactionSubScreen.ADD -> TransactionAddScreen(
                        onSaved = { tranSub = TransactionSubScreen.LIST },
                        onBack = { tranSub = TransactionSubScreen.LIST }
                    )
                    TransactionSubScreen.UPDATE -> TransactionUpdateScreen(
                        expenseId = editId,
                        onBack = { tranSub = TransactionSubScreen.LIST },
                        onSaved = { tranSub = TransactionSubScreen.LIST }
                    )
                }
                3 -> BudgetScreen()
                4 -> AccountScreen(
                    userViewModel = userViewModel,
                    navController = navController,
                    onLogout = { onLogoutSuccess() }
                )
            }
        }
    }
}

@Composable
private fun MainBottomBar(
    selectedTab: Int,
    onSelect: (Int) -> Unit,
    onQuickAdd: () -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .height(BOTTOM_BAR_HEIGHT)
            .padding(horizontal = 8.dp),
        tonalElevation = 4.dp
    ) {
        BottomNavItem(
            index = 0,
            selected = selectedTab == 0,
            onClick = { onSelect(0) },
            iconRes = R.drawable.ic_home,
            label = "Tổng quan",
            selectedColor = Color(0xFF512DA8)
        )

        BottomNavItem(
            index = 1,
            selected = selectedTab == 1,
            onClick = { onSelect(1) },
            iconRes = R.drawable.ic_list,
            label = "Giao dịch",
            selectedColor = Color(0xFF1976D2)
        )

        NavigationBarItem(
            selected = false,
            onClick = onQuickAdd,
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color(0xFF388E3C), shape = CircleShape),
                    contentAlignment = Alignment.Center
                )  {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = "Add",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        )

        BottomNavItem(
            index = 3,
            selected = selectedTab == 3,
            onClick = { onSelect(3) },
            iconRes = R.drawable.ic_pie_chart,
            label = "Ngân sách",
            selectedColor = Color(0xFF388E3C)
        )

        BottomNavItem(
            index = 4,
            selected = selectedTab == 4,
            onClick = { onSelect(4) },
            iconRes = R.drawable.ic_account,
            label = "Tài khoản",
            selectedColor = Color(0xFFFBC02D)
        )
    }
}

@Composable
private fun RowScope.BottomNavItem(
    index: Int,
    selected: Boolean,
    onClick: () -> Unit,
    iconRes: Int,
    label: String,
    selectedColor: Color
) {
    val bgColor = if (selected) selectedColor.copy(alpha = 0f) else Color.Transparent
    val tint = if (selected) selectedColor else Color.Gray

    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(bgColor, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = label,
                        tint = tint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = tint,
                    maxLines = 1
                )
            }
        },
        label = { },
        alwaysShowLabel = false
    )
}