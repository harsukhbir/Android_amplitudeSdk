package com.vybesxapp.ui.user_profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.vybesxapp.R
import com.vybesxapp.base.BaseFragment
import com.vybesxapp.service.ApiClient
import com.vybesxapp.service.ErrorParser
import com.vybesxapp.ui.login.LoginActivity
import com.vybesxapp.utils.Analytics
import com.vybesxapp.utils.SessionManager
import kotlinx.coroutines.*
import retrofit2.HttpException

class UserProfileFragment : BaseFragment() {
    private lateinit var mNameET: EditText
    private lateinit var mEmailET: EditText
    private lateinit var mUsernameET: EditText
    private lateinit var mPhoneNumberET: EditText
    private lateinit var mLogoutButton: Button
    private lateinit var mLoadingBar: ProgressBar
    private lateinit var mContentView: ViewGroup
    private lateinit var mSaveBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_profile, container, false)
        mNameET = view.findViewById(R.id.name_et)
        mEmailET = view.findViewById(R.id.email_et)
        mUsernameET = view.findViewById(R.id.username_et)
        mPhoneNumberET = view.findViewById(R.id.phone_number_et)
        mLogoutButton = view.findViewById(R.id.log_out_button)
        mLoadingBar = view.findViewById(R.id.loading_bar)
        mSaveBtn = view.findViewById(R.id.save_button)
        mContentView = view.findViewById(R.id.content_view)

        mLogoutButton.setOnClickListener {
            Analytics().amplitudeAnalytics("Clicked Logout")
            Analytics().pushEvent("Clicked Logout")
            SessionManager(context as Context).logout()
            Analytics().endSession()

            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        mSaveBtn.setOnClickListener {
            Analytics().amplitudeAnalytics("Clicked Save")
            Analytics().pushEvent("Clicked Save")
            updateUserProfile()
        }

        retrieveUserProfile()
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Analytics().amplitudeAnalytics("Profile screen")
        Analytics().pushEvent("Profile screen")
    }

    private fun retrieveUserProfile() {
        mLoadingBar.visibility = View.VISIBLE
        mContentView.visibility = View.GONE

        val errorHandler = CoroutineExceptionHandler { _, exception ->
            Log.e("UserProfileFragment", exception.message + "")
        }
        val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)
        coroutineScope.launch(errorHandler) {
            val user = mApiClient.retrieveUserProfile()
            mNameET.setText(user.name ?: "")
            mEmailET.setText(user.email)
            mUsernameET.setText(user.username ?: "")
            mPhoneNumberET.setText(user.phoneNumber ?: "")

            mLoadingBar.visibility = View.GONE
            mContentView.visibility = View.VISIBLE
            mSaveBtn.isEnabled = true
            mSaveBtn.text = "Save"
        }
    }

    private fun updateUserProfile() {
        mSaveBtn.isEnabled = false
        mSaveBtn.text = "Saving..."

        val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)
        coroutineScope.launch() {
            try {
                val name = mNameET.text.toString()
                val username = mUsernameET.text.toString()
                val phoneNumber = mPhoneNumberET.text.toString()
                mApiClient.updateUserProfile(name = name, username = username, phoneNumber)
                Toast.makeText(context, "Profile updated!", Toast.LENGTH_LONG).show()
                retrieveUserProfile()
            } catch (e: HttpException) {
                val error = ErrorParser.parseHttpResponse(e.response()?.errorBody())
                Toast.makeText(context, error?.msg, Toast.LENGTH_LONG).show()
            } finally {
                mSaveBtn.isEnabled = true
                mSaveBtn.text = "Save"
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = UserProfileFragment()
    }
}