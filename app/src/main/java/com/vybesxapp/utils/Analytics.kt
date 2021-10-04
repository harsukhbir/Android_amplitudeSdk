package com.vybesxapp.utils

import android.content.Context
import com.amplitude.api.Amplitude
import com.clevertap.android.sdk.CleverTapAPI
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.vybesxapp.MyApplication
import com.vybesxapp.service.api.LoginResponse
import io.branch.referral.Branch
import io.branch.referral.util.BranchEvent

class Analytics {
    fun identifyUser(userId: String) {
        FirebaseCrashlytics.getInstance().setUserId(userId)
        Branch.getInstance().setIdentity(userId)
    }

    fun eventSignUpSuccess(context: Context) {
        BranchEvent("SIGN_UP_SUCCESS").logEvent(context)
    }

    fun eventLogInSuccess(context: Context) {
        BranchEvent("LOGIN_SUCCESS").logEvent(context)
    }

    fun eventShareStoreLink(context: Context) {
        BranchEvent("SHARE_STORE_LINK_BUTTON_CLICKED").logEvent(context)
    }

    fun endSession() {
        Branch.getInstance().logout()
    }

    fun amplitudeAnalytics(name:String){
        Amplitude.getInstance().logEvent(name)
    }

    fun setAmplitudeUserId(id:String){
        Amplitude.getInstance().userId = id
    }

    fun pushProfileInCleverTap(map:HashMap<String, Any>){
        MyApplication.getCleverTapInstance()?.pushProfile(map)
    }

    fun pushEvent(event:String){
        MyApplication.getCleverTapInstance()?.pushEvent(event)
    }

    fun pushFCMRegisteredId(token:String){
        MyApplication.getCleverTapInstance()?.pushFcmRegistrationId(token, true)
    }
}