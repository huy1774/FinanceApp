package com.example.appqlchitieu.view

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.appqlchitieu.database.AppDatabase
import com.example.appqlchitieu.repository.*
import com.example.appqlchitieu.utils.SessionManager
import com.example.appqlchitieu.utils.UserSession
import com.example.appqlchitieu.viewmodel.AIChatViewModel
import com.example.appqlchitieu.viewmodel.AIChatViewModelFactory
import com.example.appqlchitieu.view.ui.theme.AppQLChiTieuTheme
import com.example.appqlchitieu.viewmodel.WalletViewModel
import kotlinx.coroutines.launch

class AIChatActivity : ComponentActivity() {

    private lateinit var aiChatViewModel: AIChatViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getInstance(this)

        val userSession = UserSession(SessionManager(this))
        val userId = userSession.requireUserId()

        // Tạo đầy đủ repo
        val chatRepo = AIChatRepository(db.aiChatDao())
        val walletRepo = WalletRepository(db.walletDao())
        val expenseRepo = ExpenseRepository(db.expenseDao())
        val categoryRepo = CategoryRepository(db.categoryDao())
        val budgetRepo = BudgetRepository(db.budgetDao())

        // Tạo ViewModel
        aiChatViewModel = ViewModelProvider(
            this,
            AIChatViewModelFactory(
                chatRepo,
                walletRepo,
                expenseRepo,
                categoryRepo,
                budgetRepo
            )
        )[AIChatViewModel::class.java]

        // In dữ liệu DB khi mở màn
        debugPrintDatabase(userId, walletRepo, expenseRepo, categoryRepo, budgetRepo)

        setContent {
            AppQLChiTieuTheme {
                AIChatScreen(
                    viewModel = aiChatViewModel,
                    userId = userId,
                    onClose = { finish() }
                )
            }
        }
    }

    private fun debugPrintDatabase(
        userId: Int,
        walletRepo: WalletRepository,
        expenseRepo: ExpenseRepository,
        categoryRepo: CategoryRepository,
        budgetRepo: BudgetRepository
    ) {
        lifecycleScope.launch {
            Log.d("AI_DEBUG", "=== DEBUG DATABASE ===")
            Log.d("AI_DEBUG", "UserId = " + userId)
            Log.d("AI_DEBUG", "Wallets: " + walletRepo.getAllOnce(userId))
            Log.d("AI_DEBUG", "Expenses: " + expenseRepo.getAllOnce(userId))
            Log.d("AI_DEBUG", "Categories: " + categoryRepo.getAllOnce(userId))
            Log.d("AI_DEBUG", "Budgets: " + budgetRepo.getAllOnce(userId))
        }
    }
}

