package com.vybesxapp.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.vybesxapp.service.ApiClient

open class BaseActivity : AppCompatActivity() {
    lateinit var mApiClient: ApiClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mApiClient = ApiClient(this@BaseActivity)
    }
}