package com.example.appqlchitieu.model
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "wallet_table")
data class Wallet(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val balance: Double,
    val icon: String? = null,
    val color: String? = "#4CAF50"
)
