package com.vybesxapp.base

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.vybesxapp.service.ApiClient

open class BaseFragment: Fragment() {
    lateinit var mApiClient: ApiClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mApiClient = ApiClient(context)
    }
}