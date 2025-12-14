package com.example.appqlchitieu.dao

import androidx.room.*
import com.example.appqlchitieu.model.User

// Nhiệm vụ: Quản lý thông tin và đăng nhập người dùng.
@Dao
interface UserDao {

    @Insert
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM user_table WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM user_table WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): User?      // ✔ thêm hàm login

    @Update
    suspend fun updateUser(user: User)                             // ✔ do repository gọi updateUser

    @Query("SELECT * FROM user_table WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): User?                        // ✔ repo đang gọi

    // login state lưu local (nếu muốn ghi nhớ đăng nhập)
    @Query("UPDATE user_table SET isLoggedIn = 1 WHERE id = :userId")
    suspend fun setLoggedIn(userId: Int)

    @Query("UPDATE user_table SET isLoggedIn = 0")
    suspend fun logoutAll()

    @Query("SELECT * FROM user_table WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getLoggedInUser(): User?

    @Query("SELECT * FROM user_table WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): User?


    @Query("UPDATE user_table SET password = :newPassword WHERE id = :id")
    suspend fun updatePassword(id: Int, newPassword: String)

}


