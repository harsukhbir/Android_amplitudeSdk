package com.vybesxapp.ui.product_details

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import coil.load
import com.vybesxapp.R
import com.vybesxapp.base.BaseActivity
import com.vybesxapp.service.ApiClient
import com.vybesxapp.utils.Analytics
import com.vybesxapp.utils.Money
import kotlinx.coroutines.*


class ProductDetailsActivity : BaseActivity() {
    object PARAMS {
        const val PRODUCT_ID = "_id"
        const val TABLE_USER_ATTRIBUTE_DATA = "data"
    }

    private lateinit var mProductImageView: ImageView
    private lateinit var mBrandTextView: TextView
    private lateinit var mProductNameTextView: TextView
    private lateinit var mDescriptionTextView: TextView
    private lateinit var mRetailPriceTextView: TextView
    private lateinit var mSalePriceTextView: TextView
    private lateinit var mAddToStoreBtn: Button
    private lateinit var mStoreLink: TextView
    private lateinit var mStoreLinkContainer: ViewGroup
    private lateinit var mLoadingBar: ProgressBar

    private lateinit var mShareButton: Button
    private lateinit var mProductId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)
        supportActionBar?.hide()


        mProductImageView = findViewById(R.id.product_image)
        mBrandTextView = findViewById(R.id.brand)
        mProductNameTextView = findViewById(R.id.product_name)
        mDescriptionTextView = findViewById(R.id.description)
        mRetailPriceTextView = findViewById(R.id.retail_price_tv)
        mSalePriceTextView = findViewById(R.id.sale_price_tv)
        mAddToStoreBtn = findViewById(R.id.add_to_store_btn)
        mStoreLinkContainer = findViewById(R.id.store_link_container)
        mStoreLink = findViewById(R.id.store_link)
        mShareButton = findViewById(R.id.share_button)
        mLoadingBar = findViewById(R.id.loading_bar)

        mStoreLinkContainer.visibility = View.GONE
        mAddToStoreBtn.visibility = View.GONE

        mProductId = intent.getStringExtra(PARAMS.PRODUCT_ID)!!

        Analytics().amplitudeAnalytics("View product detail $mProductId")
        Analytics().pushEvent("View product detail")

        mAddToStoreBtn.setOnClickListener {
            Analytics().amplitudeAnalytics("click Add to Store button")
            Analytics().pushEvent("click Add to Store button")
            addProductToStore(mProductId)
        }

        retrieveProductDetails(mProductId)
        retrieveOfferDetails(mProductId)
    }


    private fun addProductToStore(productId: String) {
        mAddToStoreBtn.text = "processing..."
        mAddToStoreBtn.isEnabled = false
        val job = Job()
        val errorHandler = CoroutineExceptionHandler { _, exception ->
            AlertDialog.Builder(this).setTitle("Error")
                .setMessage(exception.message)
                .setPositiveButton(android.R.string.ok) { _, _ -> }
                .setIcon(android.R.drawable.ic_dialog_alert).show()
        }
        val coroutineScope = CoroutineScope(job + Dispatchers.Main)
        coroutineScope.launch(errorHandler) {
            mApiClient.addProductsToStore(productId)
            retrieveOfferDetails(mProductId)
        }
    }

    private fun retrieveOfferDetails(productId: String) {
        val job = Job()
        val errorHandler = CoroutineExceptionHandler { _, _ ->
            mAddToStoreBtn.visibility = View.VISIBLE
        }
        val coroutineScope = CoroutineScope(job + Dispatchers.Main)
        coroutineScope.launch(errorHandler) {
            val response = mApiClient.retrieveOfferDetails(productId)

            mStoreLinkContainer.visibility = View.VISIBLE
            mAddToStoreBtn.visibility = View.GONE

            val fullStoreLink = "https://shop.getvybes.co/o/${response.data.code}"
            mStoreLink.text = fullStoreLink
            mShareButton.setOnClickListener {
                val sharingIntent = Intent(Intent.ACTION_SEND)
                sharingIntent.type = "text/plain"
                sharingIntent.putExtra(Intent.EXTRA_TEXT, fullStoreLink)
                startActivity(Intent.createChooser(sharingIntent, "Share using"))
            }
        }
    }

    private fun retrieveProductDetails(productId: String) {
        val job = Job()
        val errorHandler = CoroutineExceptionHandler { _, exception ->
            AlertDialog.Builder(this).setTitle("Error")
                .setMessage(exception.message)
                .setPositiveButton(android.R.string.ok) { _, _ -> }
                .setIcon(android.R.drawable.ic_dialog_alert).show()
        }
        val coroutineScope = CoroutineScope(job + Dispatchers.Main)
        coroutineScope.launch(errorHandler) {
            val product = mApiClient.retrieveProductDetails(productId)

            mProductImageView.load(product.imageUrl)
            mBrandTextView.text = product.brand
            mProductNameTextView.text = product.name
            mDescriptionTextView.text = product.description?.let {
                HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_COMPACT)
            }
            mRetailPriceTextView.apply {
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                text = Money.formatCurrency(product.pricing.retailPrice)
            }
            mSalePriceTextView.text = Money.formatCurrency(product.pricing.salePrice)

            mLoadingBar.visibility = View.GONE
        }
    }
}