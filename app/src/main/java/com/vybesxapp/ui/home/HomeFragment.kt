package com.vybesxapp.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vybesxapp.R
import com.vybesxapp.base.BaseFragment
import com.vybesxapp.base.BaseViewHolder
import com.vybesxapp.service.ApiClient
import com.vybesxapp.service.domain_model.Notification
import com.vybesxapp.service.domain_model.WalletTransaction
import com.vybesxapp.ui.feed.list_notification.NotificationViewHolder
import com.vybesxapp.ui.payment.withdraw.WithdrawActivity
import com.vybesxapp.ui.product_details.ProductDetailsActivity
import com.vybesxapp.utils.Analytics
import com.vybesxapp.utils.Money
import com.vybesxapp.utils.SessionManager
import kotlinx.coroutines.*
import retrofit2.HttpException

class HomeFragment : BaseFragment() {
    private lateinit var mHomeItemsRV: RecyclerView
    private lateinit var mLoadingBar:ProgressBar
    private lateinit var mHomeAdapter: HomeAdapter
    private var mHomeItems: ArrayList<Any> = ArrayList(emptyList())

    private var mPinnedNotification: Notification? = null
    private var mUserStats: UserStat? = null
    private var mWalletTransactions: List<WalletTransaction> = ArrayList(emptyList())
    private var mWalletStat: WalletStat? = null


    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        mHomeItemsRV = view.findViewById(R.id.home_items_rv)
        mLoadingBar = view.findViewById(R.id.loading_bar)

        mHomeItemsRV.layoutManager = LinearLayoutManager(context)
        mHomeAdapter = HomeAdapter(mHomeItems)
        mHomeItemsRV.adapter = mHomeAdapter

        if (mHomeItems.size == 0) {
            mLoadingBar.visibility = View.VISIBLE
            retrieveSaleStats()
            retrievePinnedNotifications()
            retrieveWalletInfo()
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Analytics().amplitudeAnalytics("view Home screen")
        Analytics().pushEvent("view Home screen")
    }

    private fun onFetchAllItems() {
        if (mPinnedNotification != null && mUserStats != null && mWalletStat !=null) {
            mLoadingBar.visibility = View.GONE
            mHomeAdapter.addItem(mUserStats!!)
            mHomeAdapter.addItem(mPinnedNotification!!)
            mHomeAdapter.addItem(mWalletStat!!)
            mWalletTransactions.forEach {
                mHomeAdapter.addItem(it)
            }
        }
    }

    private fun retrieveWalletInfo() = CoroutineScope(Job() + Dispatchers.Main).launch {
        try {
            val res = mApiClient.retrieveWalletInfo()
            val currentBalance = res.data.currentBalance
            val transactionsData = res.data.recentTransactions
            mWalletStat = WalletStat(currentBalance = Money.formatCurrency(currentBalance))
            mWalletTransactions = transactionsData.map {
                WalletTransaction(id = it.id,
                    amount = it.amount,
                    status = it.status,
                    type = it.type,
                    userId = it.userId,
                    createdAt = it.createdAt)
            }
            onFetchAllItems()
        } catch (e: HttpException) {
            Log.e("HomeFragment", "retrieveNotificationDetails: ", e)
        } catch (e: Exception) {
            Log.e("HomeFragment", "retrieveNotificationDetails: ", e)
        }
    }

    private fun retrievePinnedNotifications() =
        CoroutineScope(Job() + Dispatchers.Main).launch {
            try {
                val notifications = mApiClient.retrievePinnedNotifications()
                mPinnedNotification = notifications[0]
                onFetchAllItems()
            } catch (e: HttpException) {
                Log.e("HomeFragment", "retrieveNotificationDetails: ", e)
            } catch (e: Exception) {
                Log.e("HomeFragment", "retrieveNotificationDetails: ", e)
            }
        }

    private fun retrieveSaleStats() {
        val job = Job()
        val errorHandler = CoroutineExceptionHandler { _, exception ->
            Log.e("HomeFragment", exception.message + "")
        }
        val coroutineScope = CoroutineScope(job + Dispatchers.Main)
        coroutineScope.launch(errorHandler) {
            val totalSale = mApiClient.retrieveTotalSale().data.totalSale
            val pointBalance = mApiClient.retrieveCurrentPointBalance().data.pointBalance
            val sellerInfo = mApiClient.retrieveSellerInfo()

            mUserStats = (UserStat(sellerName = "Hi, ${sellerInfo.name ?: ""}",
                totalSale = totalSale,
                pointBalance = pointBalance))
            onFetchAllItems()
        }
    }

    data class UserStat(
        val sellerName: String,
        val totalSale: Int,
        val pointBalance: Int,
    )

    data class WalletStat(
        val currentBalance: String,
    )

    class UserStatsViewHolder(itemView: View) : BaseViewHolder<UserStat>(itemView) {
        override fun onBind(item: UserStat) {
            val totalSaleTextView: TextView = itemView.findViewById(R.id.total_sale)
            val pointBalanceTextView: TextView = itemView.findViewById(R.id.point_balance)
            val sellerName: TextView = itemView.findViewById(R.id.seller_name)
            totalSaleTextView.text = Money.formatCurrency(item.totalSale)
            pointBalanceTextView.text = item.pointBalance.toString()
            sellerName.text = item.sellerName
        }
    }

    class WalletStatViewHolder(itemView: View) : BaseViewHolder<WalletStat>(itemView) {
        override fun onBind(item: WalletStat) {
            val currentBalanceTV: TextView = itemView.findViewById(R.id.current_balance)
            val withdrawMoneyBtn:Button = itemView.findViewById(R.id.withdraw_money_btn)
            currentBalanceTV.text = item.currentBalance
            withdrawMoneyBtn.setOnClickListener{
                val intent = Intent(itemView.context, WithdrawActivity::class.java)
                itemView.context.startActivity(intent)
            }
        }
    }

    class WalletTransactionViewHolder(itemView: View) :
        BaseViewHolder<WalletTransaction>(itemView) {
        override fun onBind(item: WalletTransaction) {
            val typeTV: TextView = itemView.findViewById(R.id.type)
            val transactionAmountTV: TextView = itemView.findViewById(R.id.amount)
            val dateTV: TextView = itemView.findViewById(R.id.date)
            val statusTV: TextView = itemView.findViewById(R.id.status)

            typeTV.text = item.showType()
            transactionAmountTV.text = Money.formatCurrency(item.amount)
            dateTV.text = item.showDateCreated()
            statusTV.text = item.status
        }
    }


    class HomeAdapter(private var dataList: ArrayList<Any>) :
        RecyclerView.Adapter<BaseViewHolder<*>>() {
        companion object {
            private const val TYPE_STATS = 0
            private const val TYPE_FEED = 1
            private const val TYPE_WALLET_STAT = 2
            private const val TYPE_WALLET_TRANSACTION = 3
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
            val inflater = LayoutInflater.from(parent.context)

            return when (viewType) {
                TYPE_STATS -> {
                    val itemView = inflater.inflate(R.layout.item_home_stats, parent, false)
                    UserStatsViewHolder(itemView)
                }
                TYPE_FEED -> {
                    val itemView = inflater.inflate(R.layout.item_notification, parent, false)
                    NotificationViewHolder(itemView)
                }
                TYPE_WALLET_TRANSACTION -> {
                    val itemView = inflater.inflate(R.layout.item_wallet_transaction, parent, false)
                    WalletTransactionViewHolder(itemView)
                }
                TYPE_WALLET_STAT -> {
                    val itemView = inflater.inflate(R.layout.item_home_balance, parent, false)
                    WalletStatViewHolder(itemView)
                }
                else -> throw IllegalArgumentException("Invalid view type")
            }
        }

        override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
            val element = dataList[position]
            when (holder) {
                is UserStatsViewHolder -> holder.onBind(element as UserStat)
                is NotificationViewHolder -> holder.onBind(element as Notification)
                is WalletTransactionViewHolder -> holder.onBind(element as WalletTransaction)
                is WalletStatViewHolder -> holder.onBind(element as WalletStat)
            }
        }

        override fun getItemViewType(position: Int): Int {
            return when (dataList[position]) {
                is Notification -> TYPE_FEED
                is UserStat -> TYPE_STATS
                is WalletTransaction -> TYPE_WALLET_TRANSACTION
                is WalletStat -> TYPE_WALLET_STAT
                else -> throw IllegalArgumentException("Invalid type of data $position")
            }
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        fun addItem(item: Any) {
            dataList.add(item)
            notifyItemInserted(dataList.size - 1)
        }

        fun updateAll(items: ArrayList<Any>) {
            dataList = items
            notifyDataSetChanged()
        }
    }
}