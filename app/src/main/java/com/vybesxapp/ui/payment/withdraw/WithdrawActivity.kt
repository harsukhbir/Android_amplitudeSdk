package com.vybesxapp.ui.payment.withdraw

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.vybesxapp.R
import com.vybesxapp.base.BaseActivity
import com.vybesxapp.service.ApiClient
import com.vybesxapp.service.ErrorParser
import com.vybesxapp.utils.Analytics
import com.vybesxapp.utils.Money
import kotlinx.coroutines.*
import retrofit2.HttpException


class WithdrawActivity : BaseActivity() {
    private lateinit var mWithdrawAmountET: EditText
    private lateinit var mBankAccountNumberET: EditText
    private lateinit var mBankAccountNameET: EditText
    private lateinit var mAvailableBanksSpinner: Spinner
    private lateinit var mWithdrawButton: Button
    private lateinit var mLoadingBar: ProgressBar
    private lateinit var mContent: ViewGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_withdraw)



        mContent = findViewById(R.id.content_view)
        mLoadingBar = findViewById(R.id.loading_bar)
        mWithdrawAmountET = findViewById(R.id.withdraw_amount_et)
        mBankAccountNameET = findViewById(R.id.account_name_et)
        mBankAccountNumberET = findViewById(R.id.account_number_et)
        mAvailableBanksSpinner = findViewById(R.id.available_banks_spinner)
        mWithdrawButton = findViewById(R.id.withdraw_button)

        mWithdrawButton.setOnClickListener {
            Analytics().amplitudeAnalytics("Click Withdraw")
            Analytics().pushEvent("Click Withdraw")
            withdrawMoney()
        }

        mContent.visibility = View.GONE

        retrieveAvailableBanks()
    }

    private fun retrieveAvailableBanks() {
        mLoadingBar.visibility = View.VISIBLE
        val errorHandler = CoroutineExceptionHandler { _, _ ->
        }
        val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)
        coroutineScope.launch(errorHandler) {
            val response = mApiClient.retrieveAvailableBankToWithdraw()
            val availableBanks = ArrayList<String>()
            availableBanks.add("Select Bank Code")
            availableBanks.addAll(response.data.availableBanks.map { it.code })
            val banksAdapter: ArrayAdapter<String> =
                object : ArrayAdapter<String>(this@WithdrawActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    availableBanks) {
                    override fun isEnabled(position: Int): Boolean {
                        return position != 0
                    }
                }
            mAvailableBanksSpinner.adapter = banksAdapter

            mLoadingBar.visibility = View.GONE
            mContent.visibility = View.VISIBLE
        }
    }

    private fun withdrawMoney() {
        startProcessing()

        val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)
        coroutineScope.launch() {
            try {
                val accountNumber = mBankAccountNumberET.text.toString()
                val accountName = mBankAccountNameET.text.toString()
                val amount = Integer.parseInt(mWithdrawAmountET.text.toString())
                val bankCode = mAvailableBanksSpinner.selectedItem.toString()

                mApiClient.withdrawMoney(accountName = accountName,
                    accountNumber = accountNumber,
                    amount = amount,
                    bankCode = bankCode)

                AlertDialog.Builder(this@WithdrawActivity).setTitle("Withdrawal Successful!")
                    .setMessage("${Money.formatCurrency(amount)} will be credited into your account in 2-5 business days. " +
                            "Hubungi Whatsapp +62 812-9082-7811 apabila anda butuh bantuan lebih lanjut.")
                    .setPositiveButton(android.R.string.ok) { _, _ -> finish() }
                    .setIcon(android.R.drawable.ic_dialog_info).show()
            } catch (e: HttpException) {
                val error = ErrorParser.parseHttpResponse(e.response()?.errorBody())
                mWithdrawAmountET.error = error?.msg
                doneProcessing()
            } catch (e: NumberFormatException) {
                mWithdrawAmountET.error = "Invalid withdrawal amount!"
                doneProcessing()
            }
        }
    }

    private fun startProcessing() {
        mWithdrawButton.text = "Processing..."
        mWithdrawButton.isEnabled = false
    }

    private fun doneProcessing() {
        mWithdrawButton.text = "Withdraw"
        mWithdrawButton.isEnabled = true
    }

    companion object {
        const val TAG = "WithdrawActivity"
    }
}

