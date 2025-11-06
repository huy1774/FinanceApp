package com.example.appqlchitieu.viewmodel

import androidx.lifecycle.*
import com.example.appqlchitieu.model.Wallet
import com.example.appqlchitieu.repository.WalletRepository
import kotlinx.coroutines.launch
import androidx.lifecycle.asLiveData

/**
 * Quản lý ví (Wallet).
 * Expose tổng số dư và thao tác cập nhật số dư theo delta.
 */
class WalletViewModel(private val repo: WalletRepository) : ViewModel() {

    /** Tất cả ví hiện có */
    val allWallets: LiveData<List<Wallet>> = repo.allWallets.asLiveData()

    /** Tổng số dư của tất cả ví (Flow đã tính trong repo) */
    val totalBalance: LiveData<Double> = repo.totalBalance.asLiveData()

    fun insert(wallet: Wallet) = viewModelScope.launch { repo.insert(wallet) }
    fun update(wallet: Wallet) = viewModelScope.launch { repo.update(wallet) }
    fun delete(wallet: Wallet) = viewModelScope.launch { repo.delete(wallet) }

    /** Lấy ví theo id (one-shot, gọi trong coroutine) */
    suspend fun getWalletById(id: Int) = repo.getWalletById(id)

    /** Điều chỉnh số dư theo delta (+/-) — an toàn cho song song */
    fun adjustBalance(walletId: Int, delta: Double) = viewModelScope.launch {
        repo.updateBalanceDelta(walletId, delta)
    }
}

class WalletViewModelFactory(private val repo: WalletRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WalletViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WalletViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
