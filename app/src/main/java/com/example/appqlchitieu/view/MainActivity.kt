
package com.example.appqlchitieu.view

import android.content.Intent
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
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
import com.example.appqlchitieu.repository.AIChatRepository
import com.example.appqlchitieu.repository.BudgetRepository
import com.example.appqlchitieu.repository.CategoryRepository
import com.example.appqlchitieu.repository.ExpenseRepository
import com.example.appqlchitieu.repository.UserRepository
import com.example.appqlchitieu.repository.WalletRepository
import com.example.appqlchitieu.utils.SessionManager
import com.example.appqlchitieu.view.ui.theme.AppQLChiTieuTheme
import com.example.appqlchitieu.viewmodel.*
import com.example.appqlchitieu.utils.UserSession

private enum class TransactionSubScreen { LIST, ADD, UPDATE, CATEGORY, WALLET }
private enum class OverlayScreen { WALLET, CATEGORY }

// KHÔNG còn AI button — bỏ hẳn 2 dòng này
// private val AI_BUTTON_SIZE = 64.dp
// private val AI_ZONE_HEIGHT = 0.dp

private val BOTTOM_BAR_HEIGHT = 63.dp

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

            /* ================= DATABASE ================= */
            val db = DatabaseProvider.getDatabase(context)

            /* ================= USER ================= */
            val dataStore = UserDataStore(applicationContext)
            val userRepo = UserRepository(db.userDao())
            val userVM: UserViewModel = viewModel(
                factory = UserViewModelFactory(userRepo, dataStore, sessionManager)
            )

            /* ================= AI CHAT ================= */
            val aiChatVM: AIChatViewModel = viewModel(
                factory = AIChatViewModelFactory(
                    chatRepo = AIChatRepository(db.aiChatDao()),
                    walletRepo = WalletRepository(db.walletDao()),
                    expenseRepo = ExpenseRepository(db.expenseDao()),
                    categoryRepo = CategoryRepository(db.categoryDao()),
                    budgetRepo = BudgetRepository(db.budgetDao())
                )
            )

            /* ================= LOGIN STATE ================= */
            var isLoggedIn by remember {
                mutableStateOf(sessionManager.isLoggedIn())
            }

            LaunchedEffect(Unit) {
                isLoggedIn = sessionManager.isLoggedIn()
            }

            /* ================= AI CHAT STATE ================= */
            var showAIChat by remember { mutableStateOf(false) }

            /* ================= USER ID ================= */
            val userId = if (isLoggedIn) userSession.userIdOrNull() else null

            AppQLChiTieuTheme {

                Box(modifier = Modifier.fillMaxSize()) {

                    /* ================= NAVIGATION ================= */
                    NavHost(
                        navController = navController,
                        startDestination = if (isLoggedIn) "home" else "login"
                    ) {

                        AuthNavigation(
                            nav = navController,
                            vm = userVM,
                            sessionManager = sessionManager,
                            onLoginSuccess = {
                                isLoggedIn = true
                            }
                        )

                        composable("home") {
                            MainMenuScreen(
                                userViewModel = userVM,
                                navController = navController,
                                onLogoutSuccess = {
                                    sessionManager.logout()
                                    isLoggedIn = false
                                    showAIChat = false
                                }
                            )
                        }
                    }

                    /* ================= CHAT BUBBLE ================= */
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
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    userViewModel: UserViewModel,
    navController: NavHostController,
    onLogoutSuccess: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val context = LocalContext.current

    /* ================= SESSION ================= */
    val sessionManager = remember { SessionManager(context) }
    val userSession = remember { UserSession(sessionManager) }
    val userId = userSession.userIdOrNull()

    /* ================= CHƯA LOGIN ================= */
    if (userId == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Bạn chưa đăng nhập")
        }
        return
    }

    /* ================= WALLET ================= */
    val db = remember(context) { DatabaseProvider.getDatabase(context) }
    val walletRepo = remember { WalletRepository(db.walletDao()) }
    val walletVM: WalletViewModel = viewModel(
        factory = WalletViewModelFactory(walletRepo, userId)
    )
    val totalBalance by walletVM.totalBalance.observeAsState(0.0)

    /* ================= UI STATE ================= */
    var tranSub by rememberSaveable { mutableStateOf(TransactionSubScreen.LIST) }
    var editId by rememberSaveable { mutableStateOf(-1) }
    var overlay by rememberSaveable { mutableStateOf<OverlayScreen?>(null) }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF6A5ACD), Color(0xFF1976D2))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Quản lý chi tiêu",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },

        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 12.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color.White
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    modifier = Modifier.height(BOTTOM_BAR_HEIGHT)
                ) {
                    BottomItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        iconRes = R.drawable.ic_home,
                        label = "Tổng quan",
                        selectedColor = Color(0xFF512DA8)
                    )
                    BottomItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1; tranSub = TransactionSubScreen.LIST },
                        iconRes = R.drawable.ic_list,
                        label = "Giao dịch",
                        selectedColor = Color(0xFF1976D2)
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1 && tranSub == TransactionSubScreen.ADD,
                        onClick = {
                            selectedTab = 1
                            tranSub = TransactionSubScreen.ADD
                        },
                        icon = {
                            Box(
                                Modifier
                                    .size(44.dp)
                                    .background(Color(0xFF388E3C), CircleShape),
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
        },

        containerColor = Color.Transparent
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))
                    )
                )
                .padding(
                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                    top = innerPadding.calculateTopPadding(),
                    bottom = BOTTOM_BAR_HEIGHT
                )
        ) {

            when (overlay) {

                OverlayScreen.WALLET ->
                    WalletScreen(onBack = { overlay = null })

                OverlayScreen.CATEGORY ->
                    CategoryManageScreen(onBack = { overlay = null })

                null -> when (selectedTab) {

                    0 -> OverviewScreen(
                        modifier = Modifier.fillMaxSize(),
                        onNavigateToWallet = { overlay = OverlayScreen.WALLET },
                        onNavigateToCategory = { overlay = OverlayScreen.CATEGORY },
                        totalBalance = totalBalance
                    )

                    1 -> when (tranSub) {

                        TransactionSubScreen.LIST ->
                            TransactionScreen(
                                onAddClick = { tranSub = TransactionSubScreen.ADD },
                                onEdit = { id ->
                                    editId = id
                                    tranSub = TransactionSubScreen.UPDATE
                                }
                            )

                        TransactionSubScreen.ADD ->
                            TransactionAddScreen(
                                onSaved = { tranSub = TransactionSubScreen.LIST },
                                onBack = { tranSub = TransactionSubScreen.LIST },
                                onManageCategory = { tranSub = TransactionSubScreen.CATEGORY },
                                onManageWallet = { tranSub = TransactionSubScreen.WALLET }
                            )

                        TransactionSubScreen.UPDATE ->
                            TransactionUpdateScreen(
                                expenseId = editId,
                                onBack = { tranSub = TransactionSubScreen.LIST },
                                onSaved = { tranSub = TransactionSubScreen.LIST }
                            )

                        TransactionSubScreen.CATEGORY ->
                            CategoryManageScreen(
                                onBack = { tranSub = TransactionSubScreen.ADD }
                            )

                        TransactionSubScreen.WALLET ->
                            WalletScreen(
                                onBack = { tranSub = TransactionSubScreen.ADD }
                            )
                    }

                    3 -> BudgetScreen()

                    4 -> AccountScreen(
                        userViewModel = userViewModel,
                        onLogout = {
                            sessionManager.logout()
                            onLogoutSuccess()     // 🔥 BÁO VỀ MAINACTIVITY

                            navController.navigate("login") {
                                popUpTo("home") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    }
}



@Composable
private fun RowScope.BottomItem(
    selected: Boolean,
    onClick: () -> Unit,
    iconRes: Int,
    label: String,
    selectedColor: Color
) {
    val scale by animateFloatAsState(if (selected) 1.1f else 1f, label = "")

    val tint = if (selected) selectedColor else Color(0xFF9E9E9E)

    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painterResource(iconRes),
                    contentDescription = label,
                    tint = tint,
                    modifier = Modifier.scale(scale)
                )
                Spacer(Modifier.height(2.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = tint)
            }
        }
    )
}
