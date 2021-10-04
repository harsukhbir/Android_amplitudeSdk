package com.vybesxapp

import com.amplitude.api.Amplitude
import com.clevertap.android.sdk.ActivityLifecycleCallback
import com.clevertap.android.sdk.CleverTapAPI
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.branch.referral.Branch


class MyApplication : com.clevertap.android.sdk.Application() {

    private val API_KEY = "300c6665d0685e10fda2ec9cdaa9d5e7"

    override fun onCreate() {
        ActivityLifecycleCallback.register(this)
        super.onCreate()
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(BuildConfig.DEBUG.not())

        Branch.enableLogging();
        if (BuildConfig.DEBUG) {
            Branch.enableTestMode()
        }
        Branch.getAutoInstance(this)

        Amplitude.getInstance().initialize(this.applicationContext, API_KEY)
        // Enable COPPA (Turning off sensitive data tracking)
       // Amplitude.getInstance().enableCoppaControl()

        cleverTapAPI = CleverTapAPI.getDefaultInstance(this.applicationContext)
    }

    companion object{
        private var cleverTapAPI:CleverTapAPI? = null
        fun getCleverTapInstance() = cleverTapAPI
    }

}