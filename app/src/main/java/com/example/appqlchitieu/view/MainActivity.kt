package com.example.appqlchitieu.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.app.data.datastore.UserDataStore
import com.example.appqlchitieu.R
import com.example.appqlchitieu.database.DatabaseProvider
import com.example.appqlchitieu.navigation.AuthNavigation
import com.example.appqlchitieu.repository.*
import com.example.appqlchitieu.utils.SessionManager
import com.example.appqlchitieu.utils.UserSession
import com.example.appqlchitieu.view.ui.theme.AppQLChiTieuTheme
import com.example.appqlchitieu.viewmodel.*

/* ================= ENUM ================= */

private enum class TransactionSubScreen {
    LIST, ADD, UPDATE
}

private enum class OverviewSubScreen {
    HOME, WALLET, CATEGORY
}

private val BOTTOM_BAR_HEIGHT = 63.dp

/* ================= ACTIVITY ================= */

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {

            /* ================= CONTEXT ================= */
            val context = this
            val navController = rememberNavController()

            /* ================= SESSION ================= */
            val sessionManager = remember { SessionManager(context) }
            val userSession = remember { UserSession(sessionManager) }

            val isLoggedIn by remember {
                mutableStateOf(sessionManager.isLoggedIn())
            }

            val userId = remember {
                userSession.userIdOrNull()
            }

            /* ================= DB ================= */
            val db = remember { DatabaseProvider.getDatabase(context) }

            /* ================= USER VM ================= */
            val userVM: UserViewModel = viewModel(
                factory = UserViewModelFactory(
                    UserRepository(db.userDao()),
                    UserDataStore(applicationContext),
                    sessionManager
                )
            )

            /* ================= AI CHAT VM ================= */
            val aiChatVM: AIChatViewModel = viewModel(
                factory = AIChatViewModelFactory(
                    AIChatRepository(db.aiChatDao()),
                    WalletRepository(db.walletDao()),
                    ExpenseRepository(db.expenseDao()),
                    CategoryRepository(db.categoryDao()),
                    BudgetRepository(db.budgetDao())
                )
            )

            /* ================= CHAT STATE ================= */
            var showAIChat by rememberSaveable { mutableStateOf(false) }

            /* ================= THEME ================= */
            AppQLChiTieuTheme {

                /* ============ NAVIGATION ============ */
                NavHost(
                    navController = navController,
                    startDestination = if (isLoggedIn) "home" else "login"
                ) {

                    AuthNavigation(
                        nav = navController,
                        vm = userVM,
                        sessionManager = sessionManager,
                        onLoginSuccess = {
                            showAIChat = false
                        }
                    )

                    composable("home") {
                        MainMenuScreen(
                            userViewModel = userVM,
                            navController = navController,
                            onLogoutSuccess = {
                                sessionManager.logout()
                                showAIChat = false
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }
                }

                /* ============ CHAT BUBBLE (GLOBAL) ============ */
                if (isLoggedIn && userId != null) {

                    ChatBubble(
                        onClick = { showAIChat = true }
                    )

                    if (showAIChat) {
                        AIChatScreen(
                            viewModel = aiChatVM,
                            userId = userId,
                            onClose = { showAIChat = false }
                        )
                    }
                }
            }
        }
    }
}



/* ================= MAIN MENU ================= */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    userViewModel: UserViewModel,
    navController: NavHostController,
    onLogoutSuccess: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val userId = UserSession(sessionManager).userIdOrNull() ?: return

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
            NavigationBar(modifier = Modifier.height(BOTTOM_BAR_HEIGHT)) {

                BottomItem(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        overviewSub = OverviewSubScreen.HOME
                    },
                    iconRes = R.drawable.ic_home,
                    label = "Tổng quan",
                    selectedColor = Color(0xFF512DA8)
                )

                BottomItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        tranSub = TransactionSubScreen.LIST
                    },
                    iconRes = R.drawable.ic_list,
                    label = "Giao dịch",
                    selectedColor = Color(0xFF1976D2)
                )

                NavigationBarItem(
                    selected = false,
                    onClick = {
                        selectedTab = 1
                        tranSub = TransactionSubScreen.ADD
                    },
                    icon = {
                        Box(
                            Modifier.size(44.dp).background(Color(0xFF388E3C), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_add),
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                )

                BottomItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    iconRes = R.drawable.ic_pie_chart,
                    label = "Ngân sách",
                    selectedColor = Color(0xFF388E3C)
                )

                BottomItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    iconRes = R.drawable.ic_account,
                    label = "Tài khoản",
                    selectedColor = Color(0xFFFBC02D)
                )
            }
        }
    ) { padding ->

        Box(Modifier.fillMaxSize().padding(padding)) {

            when (selectedTab) {

                /* ===== OVERVIEW ===== */
                0 -> when (overviewSub) {

                    OverviewSubScreen.HOME ->
                        OverviewScreen(
                            totalBalance = totalBalance,
                            onNavigateToWallet = {
                                overviewSub = OverviewSubScreen.WALLET
                            },
                            onNavigateToCategory = {
                                overviewSub = OverviewSubScreen.CATEGORY
                            }
                        )

                    OverviewSubScreen.WALLET ->
                        WalletScreen(onBack = {
                            overviewSub = OverviewSubScreen.HOME
                        })

                    OverviewSubScreen.CATEGORY ->
                        CategoryManageScreen(onBack = {
                            overviewSub = OverviewSubScreen.HOME
                        })
                }

                /* ===== TRANSACTION ===== */
                1 -> when (tranSub) {

                    TransactionSubScreen.LIST ->
                        TransactionScreen(
                            onAddClick = { tranSub = TransactionSubScreen.ADD },
                            onEdit = {
                                editId = it
                                tranSub = TransactionSubScreen.UPDATE
                            }
                        )

                    TransactionSubScreen.ADD ->
                        TransactionAddScreen(
                            onSaved = { tranSub = TransactionSubScreen.LIST },
                            onBack = { tranSub = TransactionSubScreen.LIST }
                        )

                    TransactionSubScreen.UPDATE ->
                        TransactionUpdateScreen(
                            expenseId = editId,
                            onBack = { tranSub = TransactionSubScreen.LIST },
                            onSaved = { tranSub = TransactionSubScreen.LIST }
                        )
                }

                3 -> BudgetScreen()

                4 -> AccountScreen(
                    userViewModel = userViewModel,
                    onLogout = {
                        sessionManager.logout()
                        onLogoutSuccess()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

/* ================= BOTTOM ITEM ================= */

@Composable
private fun RowScope.BottomItem(
    selected: Boolean,
    onClick: () -> Unit,
    iconRes: Int,
    label: String,
    selectedColor: Color
) {
    val tint = if (selected) selectedColor else Color.Gray

    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                painterResource(iconRes),
                contentDescription = label,
                tint = tint
            )
        },
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        },
        alwaysShowLabel = true
    )
}


