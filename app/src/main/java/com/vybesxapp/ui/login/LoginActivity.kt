package com.vybesxapp.ui.login

import android.content.Context
import android.content.Intent
import android.icu.number.NumberFormatter.with
import android.icu.number.NumberRangeFormatter.with
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.amplitude.api.Amplitude
import com.google.android.material.textfield.TextInputEditText
import com.vybesxapp.MainActivity
import com.vybesxapp.MyApplication
import com.vybesxapp.R
import com.vybesxapp.base.BaseActivity
import com.vybesxapp.service.ErrorParser
import com.vybesxapp.ui.register.RegisterActivity
import com.vybesxapp.utils.Analytics
import com.vybesxapp.utils.SessionManager
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.util.*
import kotlin.collections.HashMap

class LoginActivity : BaseActivity() {
    private lateinit var mEmailTextView: TextInputEditText
    private lateinit var mPasswordTextView: TextInputEditText
    private lateinit var mSignInButton: Button
    private lateinit var mRegisterButton: Button
    private lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)


        // logging app resumed event to Segment/Amplitude

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mContext = this
        mEmailTextView = findViewById(R.id.email)
        mPasswordTextView = findViewById(R.id.password)
        mSignInButton = findViewById(R.id.sign_in_button)
        mRegisterButton = findViewById(R.id.register_button)

        mRegisterButton.setOnClickListener{
            mContext.startActivity(Intent(mContext, RegisterActivity::class.java))
        }
        mSignInButton.setOnClickListener {
            mSignInButton.isEnabled = false
            mSignInButton.text = "Signing in..."
            signIn()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    private fun signIn() {
        val job = Job()
        val errorHandler = CoroutineExceptionHandler { _, e ->
            if (e is HttpException) {
                val error = ErrorParser.parseHttpResponse(e.response()?.errorBody())
                AlertDialog.Builder(this).setTitle("Error")
                    .setMessage(error?.msg)
                    .setPositiveButton(android.R.string.ok) { _, _ -> }
                    .setIcon(android.R.drawable.ic_dialog_alert).show()
                mSignInButton.isEnabled = true
                mSignInButton.text = "Sign in"
            }
        }
        val coroutineScope = CoroutineScope(job + Dispatchers.Main)
        coroutineScope.launch(errorHandler) {
            val email = mEmailTextView.text.toString().trim().toLowerCase(Locale.getDefault())
            val password = mPasswordTextView.text.toString()
            val response = mApiClient.login(email = email, password = password)

            SessionManager(mContext).saveUserInfo(id = response.user.id,
                email = response.user.email,
                accessToken = response.accessToken)
            Analytics().identifyUser(response.user.id)
            Analytics().eventLogInSuccess(mContext)

            Analytics().amplitudeAnalytics("Login Successful")
            Analytics().setAmplitudeUserId(response.user.id)


            val map = HashMap<String, Any>()
            map["Email"] = response.user.email
            map["Identity"] = response.user.id
            map["Phone"] = response.user.phoneNumber
            Analytics().pushProfileInCleverTap(map)

            mContext.startActivity(Intent(mContext, MainActivity::class.java))
        }
    }
}
