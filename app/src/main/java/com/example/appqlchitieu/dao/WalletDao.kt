package com.example.appqlchitieu.dao

import androidx.room.*
import com.example.appqlchitieu.model.Wallet
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: Wallet)

    @Update
    suspend fun updateWallet(wallet: Wallet)

    @Delete
    suspend fun deleteWallet(wallet: Wallet)

    @Query("SELECT * FROM wallet_table")
    fun getAllWallets(): Flow<List<Wallet>>

    @Query("SELECT * FROM wallet_table WHERE id = :id LIMIT 1")
    suspend fun getWalletById(id: Int): Wallet?

    @Query("UPDATE wallet_table SET balance = balance + :delta WHERE id = :walletId")
    suspend fun updateBalanceDelta(walletId: Int, delta: Double)
}
