package com.vybesxapp.utils

import android.content.Context
import com.vybesxapp.R

class SessionManager(context: Context) {
    private val sharedPref = context.getSharedPreferences(
        context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)

    fun saveUserInfo(id: String, email: String, accessToken: String) {
        sharedPref.edit()
            .putString(USER_ID, id)
            .putString(USER_EMAIL, email)
            .putString(ACCESS_TOKEN, accessToken)
            .apply()
    }

    fun getUserId(): String? {
        return sharedPref.getString(USER_ID, null)
    }

    fun getUserEmail(): String? {
        return sharedPref.getString(USER_EMAIL, null)
    }

    fun getAccessToken(): String? {
        return sharedPref.getString(ACCESS_TOKEN, null)
    }

    fun hasLoggedIn(): Boolean {
        return getAccessToken() !== null
    }

    fun logout() {
        sharedPref.edit().clear().apply()
    }

    companion object {
        private const val USER_EMAIL = "email"
        private const val USER_ID = "id"
        private const val ACCESS_TOKEN = "access_token"
    }
}