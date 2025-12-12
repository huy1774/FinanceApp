package com.example.app.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("user_prefs")

class UserDataStore(private val context: Context) {

    companion object {
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
    }

    val userName = context.dataStore.data.map { it[USER_NAME] ?: "" }
    val userEmail = context.dataStore.data.map { it[USER_EMAIL] ?: "" }

    suspend fun saveUser(name: String, email: String) {
        context.dataStore.edit {
            it[USER_NAME] = name
            it[USER_EMAIL] = email
        }
    }

    suspend fun clearUser() {
        context.dataStore.edit { prefs ->
            prefs[USER_NAME] = ""
            prefs[USER_EMAIL] = ""
        }
    }
}
