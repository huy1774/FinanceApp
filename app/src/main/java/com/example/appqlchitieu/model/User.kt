
package com.example.appqlchitieu.model

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "user_table")
data class User(

    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val phone: String?,
    val password: String,
    val avatarUri: String? = null,
    val isLoggedIn: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
