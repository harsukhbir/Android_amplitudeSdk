package com.vybesxapp.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.vybesxapp.MainActivity
import com.vybesxapp.R
import com.vybesxapp.service.ApiClient
import com.vybesxapp.ui.login.LoginActivity
import com.vybesxapp.utils.SessionManager
import io.branch.referral.Branch


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash)
        supportActionBar?.hide()

        Handler(Looper.getMainLooper()).postDelayed({
            if (SessionManager(this).hasLoggedIn()) {
                // set static field accessToken
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, 1000)
    }

    private val branchReferralInitListener =
        Branch.BranchReferralInitListener { _, error ->
            // do stuff with deep link data (nav to page, display content, etc)
            if (error != null) {
                FirebaseCrashlytics.getInstance().recordException(Error(error.message))
            }
        }

    override fun onStart() {
        super.onStart()
        Branch.sessionBuilder(this).withCallback(branchReferralInitListener)
            .withData(if (intent != null) intent.data else null).init()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        Branch.sessionBuilder(this).withCallback(branchReferralInitListener).reInit()
    }
}


