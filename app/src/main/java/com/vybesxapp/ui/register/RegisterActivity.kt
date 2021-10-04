package com.vybesxapp.ui.register

import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.amplitude.api.Amplitude
import com.google.android.material.textfield.TextInputEditText
import com.vybesxapp.BuildConfig
import com.vybesxapp.MainActivity
import com.vybesxapp.R
import com.vybesxapp.base.BaseActivity
import com.vybesxapp.service.ApiClient
import com.vybesxapp.service.ErrorParser
import com.vybesxapp.service.api.VerificationCodeResponse
import com.vybesxapp.ui.login.LoginActivity
import com.vybesxapp.utils.Analytics
import com.vybesxapp.utils.SessionManager
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.sql.Timestamp
import java.util.*


class RegisterActivity : BaseActivity() {

    private lateinit var mNameEditText: TextInputEditText
    private lateinit var mUsernameEditText: TextInputEditText
    private lateinit var mPhoneNumberEditText: TextInputEditText
    private lateinit var mEmailEditText: TextInputEditText
    private lateinit var mPasswordEditText: TextInputEditText
    private lateinit var mRegisterButton: Button
    private lateinit var mCaptchaImage: ImageView
    private lateinit var mCaptchaEditText: EditText
    private lateinit var mRootLayout: View

    private var mToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        //setSupportActionBar(findViewById(R.id.toolbar))

        mRootLayout = findViewById(R.id.parent_layout)
        mNameEditText = findViewById(R.id.name)
        mUsernameEditText = findViewById(R.id.username)
        mEmailEditText = findViewById(R.id.email)
        mPhoneNumberEditText = findViewById(R.id.phone_number)
        mPasswordEditText = findViewById(R.id.password)
        mRegisterButton = findViewById(R.id.register_button)
        mCaptchaImage = findViewById(R.id.captcha_image)
        mCaptchaEditText = findViewById(R.id.captcha)

        loadCaptcha()
        mRegisterButton.setOnClickListener {
            register()
        }

        if (BuildConfig.DEBUG) {
            autoFill()
        }

        Analytics().amplitudeAnalytics("Register Activity")
    }

    private fun autoFill() {
        val currentTime:String = System.currentTimeMillis().toString()
        mNameEditText.setText("Viet Nguyen")
        mUsernameEditText.setText("nguyentuanviet${currentTime}")
        mEmailEditText.setText("test_${currentTime}@getvybes.co")
        mPhoneNumberEditText.setText("1234")
        mPasswordEditText.setText("1234")
    }

    private fun register() {
        mRegisterButton.isEnabled = false
        mRegisterButton.text = "Processing..."
        val errorHandler = CoroutineExceptionHandler { _, exception ->
            if (exception is HttpException) {
                val error = ErrorParser.parseHttpResponse(exception.response()?.errorBody())
                AlertDialog.Builder(this).setTitle("Error")
                    .setMessage(error!!.msg)
                    .setPositiveButton(android.R.string.ok) { _, _ -> }
                    .setIcon(android.R.drawable.ic_dialog_alert).show()
                loadCaptcha() //reload captcha
                mRegisterButton.isEnabled = true
                mRegisterButton.text = "daftar"
            }
        }
        val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)
        coroutineScope.launch(errorHandler) {
            val name: String = mNameEditText.text.toString().trim()
            val username: String =
                mUsernameEditText.text.toString().trim().toLowerCase(Locale.getDefault())
            val email: String =
                mEmailEditText.text.toString().trim().toLowerCase(Locale.getDefault())
            val phoneNumber: String = mPhoneNumberEditText.text.toString().trim()
            val password: String = mPasswordEditText.text.toString().trim()
            val captcha: String = mCaptchaEditText.text.toString().trim()

            val response = mApiClient.register(name = name,
                username = username,
                email = email,
                phoneNumber = "+62$phoneNumber", // TODO: hardcode +62 for now
                password = password,
                token = mToken!!,
                captcha = captcha
            )

            val codeVerificationDialog = CodeVerificationDialog(response.data.userId)
            codeVerificationDialog.onCodeVerifiedListener =
                object : CodeVerificationDialog.OnCodeVerified {
                    override fun onCodeVerified(verifyCodeResponse: VerificationCodeResponse) {

                        SessionManager(this@RegisterActivity).saveUserInfo(
                            id = verifyCodeResponse.user.id,
                            email = verifyCodeResponse.user.email,
                            accessToken = verifyCodeResponse.accessToken)
                        Analytics().identifyUser(verifyCodeResponse.user.id)
                        Analytics().eventSignUpSuccess(this@RegisterActivity)

                        Analytics().amplitudeAnalytics("Signup Successful")
                        Analytics().setAmplitudeUserId(verifyCodeResponse.user.id)

                        val map = HashMap<String, Any>()
                        map["Email"] = verifyCodeResponse.user.email
                        map["Identity"] = verifyCodeResponse.user.id
                        map["Phone"] = verifyCodeResponse.user.phoneNumber
                        Analytics().pushProfileInCleverTap(map)

                        startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                    }
                }
            codeVerificationDialog.show(supportFragmentManager,
                "CodeVerificationDialog")
        }
    }

    private fun loadCaptcha() {
        val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)
        coroutineScope.launch() {
            try {
                val response = mApiClient.getCaptcha()
                val decodedString: ByteArray = Base64.decode(response.data.captcha, Base64.DEFAULT)
                val decodedByte =
                    BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                mCaptchaImage.setImageBitmap(decodedByte)
                mToken = response.data.token
            } catch (e: HttpException) {
                val error = ErrorParser.parseHttpResponse(e.response()?.errorBody())
                AlertDialog.Builder(this@RegisterActivity).setTitle("Error")
                    .setMessage(error!!.msg)
                    .setPositiveButton(android.R.string.ok) { _, _ -> }
                    .setIcon(android.R.drawable.ic_dialog_alert).show()
            }
        }
    }
}