package com.vybesxapp.service

import android.util.Log
import okhttp3.ResponseBody
import org.json.JSONObject

object ErrorParser {
    data class ApiError(
        var err: String,
        var msg: String,
    )

    fun parseHttpResponse(responseBody: ResponseBody?): ApiError? {
        val errorObj = ApiError("", "")
        try {
            val jsonObject = JSONObject(responseBody!!.string())
            if (jsonObject.has("err")) {
                errorObj.err = jsonObject.getString("err")
            }
            if (jsonObject.has("msg")) {
                errorObj.msg = jsonObject.getString("msg")
            }
            return errorObj
        } catch (e: Exception) {
            Log.e("ErrorParser", "parseHttpResponse: ",  )
        }
        return null
    }
}