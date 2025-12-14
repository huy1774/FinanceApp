package com.example.appqlchitieu.repository

import com.example.appqlchitieu.dao.UserDao
import com.example.appqlchitieu.model.User

class UserRepository(private val userDao: UserDao) {

    suspend fun insertUser(user: User) = userDao.insertUser(user)

    suspend fun login(email: String, password: String): User? = userDao.login(email, password)

    suspend fun updateUser(user: User) = userDao.updateUser(user)

    suspend fun getUserById(id: Int): User? = userDao.getUserById(id)

    suspend fun getByEmail(email: String): User? = userDao.getByEmail(email)

    suspend fun updatePassword(id: Int, newPassword: String) = userDao.updatePassword(id, newPassword)
}
