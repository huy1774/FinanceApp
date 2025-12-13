package com.example.appqlchitieu.viewmodel

import androidx.lifecycle.*
import com.example.appqlchitieu.model.AIChat
import com.example.appqlchitieu.repository.*
import kotlinx.coroutines.launch
import android.util.Log
class AIChatViewModel(
    private val chatRepo: AIChatRepository,
    private val walletRepo: WalletRepository,
    private val expenseRepo: ExpenseRepository,
    private val categoryRepo: CategoryRepository,
    private val budgetRepo: BudgetRepository,
) : ViewModel() {

    fun chats(userId: Int) = chatRepo.chatsByUser(userId).asLiveData()

    /** Gửi message + DB context cho AI */
    fun sendMessage(userId: Int, message: String) = viewModelScope.launch {

        //  Lấy dữ liệu từ Database (one-shot)
        val wallets = walletRepo.getAllOnce(userId)
        val expenses = expenseRepo.getAllOnce(userId)
        val categories = categoryRepo.getAllOnce(userId)
        val budgets = budgetRepo.getAllOnce(userId)

        // Tạo AI Context
        val context = buildString {

            appendLine("=== Ví (Wallets) ===")
            wallets.forEach {
                appendLine("- ${it.name}: ${it.balance} VND")
            }

            appendLine("\n=== Danh mục (Categories) ===")
            categories.forEach {
                appendLine("- ${it.id}: ${it.name} (${it.type})")
            }

            appendLine("\n=== Giao dịch (Expenses) ===")
            expenses.forEach {
                appendLine("- ${it.amount} VND | Cat=${it.categoryId} | ${it.date}")
            }

            appendLine("\n=== Ngân sách (Budgets) ===")
            budgets.forEach {
                appendLine("- Cat=${it.categoryId} | Limit=${it.amountLimit} | ${it.startDate} → ${it.endDate}")
            }
        }

        //  Gửi prompt cho Gemini
        val prompt = """
            Bạn là AI tư vấn tài chính cá nhân.
            Đây là toàn bộ dữ liệu của người dùng:

            $context

            Câu hỏi của người dùng:
            $message
            
            Hãy trả lời như 1 chuyên gia tài chính thực sự, chính xác và dựa hoàn toàn vào dữ liệu trên, đưa ra lời khuyên.
        """.trimIndent()
        Log.d("AI_DEBUG", "PROMPT SEND TO GEMINI:\n$prompt")
        val aiResponse = GeminiRepository.askGemini(prompt)
        Log.d("AI_DEBUG", "AI RESPONSE: $aiResponse")
        //  Lưu lịch sử chat
        chatRepo.insert(
            AIChat(
                userId = userId,
                question = message,
                response = aiResponse
            )
        )
    }
}

class AIChatViewModelFactory(
    private val chatRepo: AIChatRepository,
    private val walletRepo: WalletRepository,
    private val expenseRepo: ExpenseRepository,
    private val categoryRepo: CategoryRepository,
    private val budgetRepo: BudgetRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AIChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AIChatViewModel(
                chatRepo,
                walletRepo,
                expenseRepo,
                categoryRepo,
                budgetRepo
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}