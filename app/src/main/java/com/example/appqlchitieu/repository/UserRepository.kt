package com.example.appqlchitieu.repository

import com.example.appqlchitieu.dao.UserDao
import com.example.appqlchitieu.model.User

class UserRepository(private val userDao: UserDao) {

    suspend fun insert(user: User) = userDao.insertUser(user)

    suspend fun login(email: String, password: String): User? = userDao.login(email,password)

    suspend fun update(user: User) = userDao.updateUser(user)

    suspend fun getById(id: Int) = userDao.getUserById(id)

    suspend fun getByEmail(email: String) = userDao.getByEmail(email)

}


