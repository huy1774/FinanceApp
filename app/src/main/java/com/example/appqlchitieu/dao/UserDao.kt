package com.example.appqlchitieu.dao

import androidx.room.*
import com.example.appqlchitieu.model.User

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM user_table WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): User?

    @Query("SELECT * FROM user_table WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): User?
}
