package com.vybesxapp.service.domain_model

import java.text.SimpleDateFormat
import java.util.*

data class WalletTransaction(
    val id: String,
    val amount: Int,
    val status: String,
    val type: String,
    val userId: String,
    val createdAt: Long,
) {
    fun showType(): String {
        if (type == "COMMISSION") return "komisi"
        return type
    }

    fun showDateCreated(): String {
        val format = "dd MMM YYYY"
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(Date(createdAt))
    }
}