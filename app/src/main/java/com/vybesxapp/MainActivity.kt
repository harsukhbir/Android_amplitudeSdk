package com.vybesxapp

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.messaging.FirebaseMessaging
import com.vybesxapp.base.BaseActivity
import com.vybesxapp.service.ApiClient
import com.vybesxapp.service.ErrorParser
import com.vybesxapp.ui.feed.list_notification.FeedFragment
import com.vybesxapp.ui.home.HomeFragment
import com.vybesxapp.ui.store.StoreFragment
import com.vybesxapp.ui.user_profile.UserProfileFragment
import com.vybesxapp.utils.Analytics
import io.branch.referral.validators.IntegrationValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        if (!isNetworkConnected(this)) {
            AlertDialog.Builder(this).setTitle("No Internet Connection")
                .setMessage("Please check your internet connection and try again!")
                .setPositiveButton("OK") { _, _ -> }
            return
        }

        val homeFragment = HomeFragment.newInstance()
        val storeFragment = StoreFragment.newInstance()
        val feedFragment = FeedFragment.newInstance()
        val userProfileFragment = UserProfileFragment.newInstance()

        loadFragment(supportFragmentManager, homeFragment)

        val navigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        navigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.page_home -> {
                    loadFragment(supportFragmentManager, homeFragment)
                }
                R.id.page_store -> {
                    loadFragment(supportFragmentManager, storeFragment)
                }
                R.id.page_profile -> {
                    loadFragment(supportFragmentManager, userProfileFragment)
                }
                else -> {
                    loadFragment(supportFragmentManager, feedFragment)
                }
            }
            true
        }

        // register device FCM token
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            registerDeviceToken(token.toString())
        })

        checkForUpdates()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_UPDATE) {
            if (requestCode == RESULT_OK) {
                Toast.makeText(baseContext, "App updated!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(baseContext, "App update failed!", Toast.LENGTH_SHORT).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        super.onResume()
        val updateManager = AppUpdateManagerFactory.create(this)
        updateManager.appUpdateInfo.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                updateManager.startUpdateFlowForResult(
                    it,
                    AppUpdateType.IMMEDIATE,
                    this,
                    REQUEST_UPDATE)
            }
        }
    }

    private fun loadFragment(fragmentManager: FragmentManager, fragment: Fragment) {
        // load fragment
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun registerDeviceToken(token: String?) {
        if (token != null) {
            val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)
            coroutineScope.launch() {
                try {
                    mApiClient.registerDeviceToken(token)
                    Analytics().pushFCMRegisteredId(token)
                } catch (e: HttpException) {
                    val error = ErrorParser.parseHttpResponse(e.response()?.errorBody())
                    Log.w("MainActivity", error?.msg.toString())
                } catch (e: Exception) {

                }
            }
        }
    }

    private fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            (networkCapabilities != null
                    && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
        } else {
            val activeNetwork = connectivityManager.activeNetworkInfo
            activeNetwork?.type == ConnectivityManager.TYPE_WIFI || activeNetwork?.type == ConnectivityManager.TYPE_MOBILE
        }
    }

    private fun checkForUpdates() {
        val updateManager: AppUpdateManager = AppUpdateManagerFactory.create(baseContext)
        updateManager.appUpdateInfo.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                it.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                updateManager.startUpdateFlowForResult(it,
                    AppUpdateType.IMMEDIATE,
                    this,
                    REQUEST_UPDATE)
                Toast.makeText(this, "Latest app version installed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_UPDATE = 100
        private const val APP_UPDATE_TYPE_SUPPORTED = AppUpdateType.IMMEDIATE
        private const val TAG = "MainActivity"
    }
}




