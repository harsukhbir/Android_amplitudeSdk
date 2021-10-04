package com.vybesxapp.ui.feed.list_notification

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vybesxapp.R
import com.vybesxapp.base.BaseFragment
import com.vybesxapp.service.ApiClient
import com.vybesxapp.service.ErrorParser
import com.vybesxapp.service.domain_model.Notification
import com.vybesxapp.utils.Analytics
import kotlinx.coroutines.*
import retrofit2.HttpException

private const val TAG: String = "Feed"
class FeedFragment : BaseFragment() {
    private lateinit var mNotificationRecyclerView: RecyclerView
    private lateinit var mLoadingBar:ProgressBar
    private lateinit var mNotificationAdapter: NotificationAdapter
    private lateinit var mNotificationList: ArrayList<Notification>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_feed_frgment, container, false)
        mNotificationRecyclerView = view.findViewById(R.id.notification_list)
        mLoadingBar = view.findViewById(R.id.loading_bar)

        mNotificationList = ArrayList(emptyList())
        mNotificationRecyclerView.layoutManager = LinearLayoutManager(context)

        retrieveNotifications()

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Analytics().amplitudeAnalytics("Feed Screen")
        Analytics().pushEvent("Feed Screen")
    }

    private fun retrieveNotifications() {
        val job = Job()
        val errorHandler = CoroutineExceptionHandler { _, exception ->
            if (exception is HttpException) {
                val error = ErrorParser.parseHttpResponse(exception.response()?.errorBody())
                AlertDialog.Builder(context).setTitle("Error")
                    .setMessage(error!!.msg)
                    .setPositiveButton(android.R.string.ok) { _, _ -> }
                    .setIcon(android.R.drawable.ic_dialog_alert).show()
            }
        }
        val coroutineScope = CoroutineScope(job + Dispatchers.Main)
        coroutineScope.launch(errorHandler) {
            val notificationList = mApiClient.retrieveNotificationList()
            mNotificationAdapter = NotificationAdapter(notificationList)
            mNotificationRecyclerView.adapter = mNotificationAdapter
            mLoadingBar.visibility = View.GONE
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = FeedFragment()
    }
}