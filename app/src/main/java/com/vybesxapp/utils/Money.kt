package com.vybesxapp.utils

import java.text.NumberFormat
import java.util.*

class Money {
    companion object {
        fun formatCurrency(amount: Int): String {
            val format: NumberFormat = NumberFormat.getCurrencyInstance()
            format.maximumFractionDigits = 0
            format.currency = Currency.getInstance("IDR")

            return format.format(amount)
        }
    }
}