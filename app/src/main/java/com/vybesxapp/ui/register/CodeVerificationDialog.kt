package com.vybesxapp.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.vybesxapp.R
import com.vybesxapp.service.ApiClient
import com.vybesxapp.service.ErrorParser
import com.vybesxapp.service.api.VerificationCodeResponse
import kotlinx.coroutines.*
import retrofit2.HttpException

class CodeVerificationDialog(private val userId: String) : DialogFragment() {
    private lateinit var mCodeEditText: EditText
    private lateinit var mConfirmButton: Button
    private lateinit var mResendButton: Button

    interface OnCodeVerified {
        fun onCodeVerified(verifyCodeResponse: VerificationCodeResponse)
    }

    var onCodeVerifiedListener: OnCodeVerified? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
//        dialog!!.window?.setBackgroundDrawableResource(R.drawable.round_corner);
        val view = inflater.inflate(R.layout.code_verification, container, false)
        mCodeEditText = view.findViewById(R.id.code)
        mConfirmButton = view.findViewById(R.id.confirm_button)
        mResendButton = view.findViewById(R.id.resend_code_button)
        mResendButton.visibility = View.GONE // TODO: implement resend code function
        mConfirmButton.setOnClickListener {
            startProcessing()
            verifyCode()
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun startProcessing() {
        mConfirmButton.isEnabled = false
        mConfirmButton.text = "Verifying..."
    }

    private fun doneProcessing() {
        mConfirmButton.isEnabled = true
        mConfirmButton.text = "OK"
    }

    private fun verifyCode() {
        val job = Job()
        val errorHandler = CoroutineExceptionHandler { _, exception ->
            if (exception is HttpException) {
                val error = ErrorParser.parseHttpResponse(exception.response()?.errorBody())
                mCodeEditText.error = error!!.msg
                doneProcessing()
            }
        }

        val coroutineScope = CoroutineScope(job + Dispatchers.Main)
        coroutineScope.launch(errorHandler) {
            val res = ApiClient(context).verifyCode(userId = userId,
                code = mCodeEditText.text.toString())
            onCodeVerifiedListener?.onCodeVerified(res)
            dismiss()
        }
    }
}
