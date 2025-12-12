package com.example.appqlchitieu.viewmodel

import androidx.lifecycle.*
import com.example.appqlchitieu.model.Wallet
import com.example.appqlchitieu.repository.WalletRepository
import kotlinx.coroutines.launch

/**
 * Quản lý ví (Wallet) theo USER hiện tại.
 */
class WalletViewModel(
    private val repo: WalletRepository,
    private val userId: Int
) : ViewModel() {

    /**  Tất cả ví của USER hiện tại */
    val allWallets: LiveData<List<Wallet>> =
        repo.allWallets(userId).asLiveData()

    /**  Tổng số dư của USER hiện tại */
    val totalBalance: LiveData<Double> =
        repo.totalBalance(userId).asLiveData()

    fun insert(wallet: Wallet) = viewModelScope.launch {
        repo.insert(wallet)
    }

    fun update(wallet: Wallet) = viewModelScope.launch {
        repo.update(wallet)
    }

    fun delete(wallet: Wallet) = viewModelScope.launch {
        repo.delete(wallet)
    }

    /**  Lấy ví theo id + user */
    suspend fun getWalletById(id: Int) =
        repo.getWalletById(userId, id)

    /**  Điều chỉnh số dư theo delta (+/-) theo user */
    fun adjustBalance(walletId: Int, delta: Double) = viewModelScope.launch {
        repo.updateBalanceDelta(userId, walletId, delta)
    }
}

class WalletViewModelFactory(
    private val repo: WalletRepository,
    private val userId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WalletViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WalletViewModel(repo, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
