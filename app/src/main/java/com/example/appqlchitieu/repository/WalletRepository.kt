package com.example.appqlchitieu.repository

import com.example.appqlchitieu.dao.WalletDao
import com.example.appqlchitieu.model.Wallet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import kotlinx.coroutines.flow.first

class WalletRepository(private val walletDao: WalletDao) {

    fun allWallets(userId: Int) = walletDao.getAllWallets(userId)

    suspend fun getAllOnce(userId: Int): List<Wallet> =
        walletDao.getAllWalletsOnce(userId)

    fun totalBalance(userId: Int) =
        allWallets(userId).map { it.sumOf { w -> w.balance } }

    suspend fun insert(wallet: Wallet) = walletDao.insertWallet(wallet)
    suspend fun update(wallet: Wallet) = walletDao.updateWallet(wallet)
    suspend fun delete(wallet: Wallet) = walletDao.deleteWallet(wallet)

    suspend fun getWalletById(userId: Int, id: Int) =
        walletDao.getWalletById(userId, id)

    suspend fun updateBalanceDelta(userId: Int, walletId: Int, delta: Double) =
        walletDao.updateBalanceDelta(userId, walletId, delta)
}
