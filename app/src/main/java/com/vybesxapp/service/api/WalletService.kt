package com.vybesxapp.service.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface WalletService {
    @GET("/wallet")
    suspend fun retrieveWalletInfo(): WalletInfoResponse

    @GET("/wallet/withdraw_request/available_banks")
    suspend fun retrieveAvailableBanks(): AvailableBanksResponse

    @POST("/wallet/withdraw_request")
    suspend fun requestWithdrawMoney(@Body request:WithdrawMoneyRequest)
}

data class WalletInfoResponse(
    val data: WalletInfoNetworkModel,
) {
    data class WalletInfoNetworkModel(
        @SerializedName("current_balance")
        val currentBalance: Int,
        @SerializedName("recent_transactions")
        val recentTransactions: List<WalletTransactionNetworkModel>,
    )
}

data class WalletTransactionNetworkModel(
    @SerializedName("_id")
    val id: String,
    val amount: Int,
    val status: String,
    val type: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("created_at")
    val createdAt: Long,
)

data class AvailableBanksResponse(
    val data: AvailableBanks,
) {
    data class AvailableBanks(
        @SerializedName("available_banks")
        val availableBanks: ArrayList<Bank>,
    )

    data class Bank(
        val code: String,
        val name: String,
    )
}

data class WithdrawMoneyRequest(
    @SerializedName("account_name")
    val accountName: String,
    @SerializedName("account_number")
    val accountNumber: String,
    @SerializedName("amount")
    val amount: Int,
    @SerializedName("bank_code")
    val bankCode: String,
)