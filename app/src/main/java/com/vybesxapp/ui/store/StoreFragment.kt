package com.vybesxapp.ui.store

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.vybesxapp.R
import com.vybesxapp.base.BaseFragment
import com.vybesxapp.base.BaseViewHolder
import com.vybesxapp.service.ApiClient
import com.vybesxapp.service.ErrorParser
import com.vybesxapp.service.domain_model.Offer
import com.vybesxapp.service.domain_model.Product
import com.vybesxapp.ui.product_details.ProductDetailsActivity
import com.vybesxapp.ui.recommend_products.RecommendProductsActivity
import com.vybesxapp.utils.Analytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.HttpException


class StoreFragment : BaseFragment() {
    private lateinit var storeItemsView: RecyclerView
    private lateinit var mLoadingBar: ProgressBar
    private lateinit var viewAdapter: StoreAdapter
    private lateinit var mContext: Context
    private var mStoreItems: ArrayList<Any> = ArrayList(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_store, container, false)
        storeItemsView = view.findViewById(R.id.store_items)
        mLoadingBar = view.findViewById(R.id.loading_bar)
        viewAdapter = StoreAdapter(this@StoreFragment, mStoreItems)
        storeItemsView.layoutManager = LinearLayoutManager(mContext)
        storeItemsView.adapter = viewAdapter

        retrieveStoreData()
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Analytics().amplitudeAnalytics("view Store screen")
        Analytics().pushEvent("view Store screen")
    }

    fun retrieveStoreData() {
        val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)
        coroutineScope.launch {
            try {
                val res = mApiClient.getStoreData()
                val offerDtos = res.data.offers
                val storeInfo = res.data.store
                val storeName = if (storeInfo.slug.isEmpty()) "Not set!" else storeInfo.shopLink
                mStoreItems = ArrayList()
                mStoreItems.add(storeName)
                for (offerDto in offerDtos) {
                    val productDto = offerDto.product
                    val offer = Offer(
                        Product(
                            productDto.id,
                            productDto.brand,
                            productDto.description,
                            productDto.name,
                            productDto.imageUrl,
                            Product.Pricing(
                                productDto.pricing.retail,
                                productDto.pricing.sale
                            )
                        )
                    )
                    mStoreItems.add(offer)
                }
                viewAdapter.updateAll(mStoreItems)
                mLoadingBar.visibility = View.GONE
            } catch (e: HttpException) {
                Log.e("StoreFragment", "retrieveStoreData: ", e)
            } catch (e: Exception) {
                Log.e("StoreFragment", "retrieveStoreData: ", e)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = StoreFragment()
    }
}

class ChangeStoreLinkDialog : DialogFragment() {
    private lateinit var mStoreNameET: EditText

    interface OnSlugUpdatedListener {
        fun onSlugUpdated()
    }

    var onSlugUpdatedListener: OnSlugUpdatedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
//        dialog!!.window?.setBackgroundDrawableResource(R.drawable.round_corner);
        val view = inflater.inflate(R.layout.dialog_update_store_name, container, false)
        mStoreNameET = view.findViewById(R.id.store_name)
        val confirmBtn: Button = view.findViewById(R.id.confirm_button)

        confirmBtn.setOnClickListener {
            Analytics().amplitudeAnalytics("click Change Name")
            Analytics().pushEvent("click Change Name")
            updateStoreSlug(mStoreNameET.text.toString())
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun updateStoreSlug(slug: String) = CoroutineScope(Job() + Dispatchers.Main).launch {
        try {
            ApiClient(context).updateStoreSlug(slug)
            onSlugUpdatedListener?.onSlugUpdated()
            dismiss()
        } catch (e: HttpException) {
            val error = ErrorParser.parseHttpResponse(e.response()?.errorBody())
            mStoreNameET.error = error?.msg
        } catch (e: Exception) {
            Log.e("ChangeStoreLinkDialog", "updateStoreSlug: ", e)
        }
    }
}

class StoreProductViewHolder(itemView: View) : BaseViewHolder<Offer>(itemView) {
    override fun onBind(item: Offer) {
        val brandTextView = itemView.findViewById<TextView>(R.id.brand)
        val productNameTextView = itemView.findViewById<TextView>(R.id.product_name)
        val productImageView = itemView.findViewById<ImageView>(R.id.product_image)
        val product = item.product
        brandTextView.text = product.brand
        productNameTextView.text = product.name
        productImageView.load(product.imageUrl) {
            crossfade(true)
        }

        itemView.setOnClickListener {
            val intent = Intent(itemView.context, ProductDetailsActivity::class.java)
            intent.putExtra(ProductDetailsActivity.PARAMS.PRODUCT_ID, product.id)
            itemView.context.startActivity(intent)
        }
    }
}

class StoreLinkViewHolder(itemView: View, private val parentFragment: StoreFragment) :
    BaseViewHolder<String>(itemView) {
    override fun onBind(item: String) {
        val shopLinkTextView = itemView.findViewById<TextView>(R.id.shop_link)
        val shareButton = itemView.findViewById<Button>(R.id.share_button)
        val changeStoreLinkButton = itemView.findViewById<Button>(R.id.change_store_link_button)
        val addProductBtn = itemView.findViewById<Button>(R.id.add_product)
        shopLinkTextView.text = item
        changeStoreLinkButton.setOnClickListener {
            val changeStoreLinkDialog = ChangeStoreLinkDialog()
            val onSlugUpdatedListener = object : ChangeStoreLinkDialog.OnSlugUpdatedListener {
                override fun onSlugUpdated() {
                    parentFragment.retrieveStoreData()
                }
            }
            changeStoreLinkDialog.onSlugUpdatedListener = onSlugUpdatedListener
            changeStoreLinkDialog.show(parentFragment.childFragmentManager, "ChangeStoreLinkDialog")
        }
        shareButton.setOnClickListener {
            Analytics().amplitudeAnalytics("click Share button")
            Analytics().pushEvent("click Share button")
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            sharingIntent.putExtra(Intent.EXTRA_TEXT, item)
            itemView.context.startActivity(Intent.createChooser(sharingIntent, "Share using"))
            Analytics().eventShareStoreLink(itemView.context)
        }
        addProductBtn.setOnClickListener {
            Analytics().amplitudeAnalytics("click Add Product")
            Analytics().pushEvent("click Add Product")

            val intent = Intent(itemView.context, RecommendProductsActivity::class.java)
            itemView.context.startActivity(intent)
        }
    }
}

class StoreAdapter(private val storeFragment: StoreFragment, private var dataList: List<Any>) :
    RecyclerView.Adapter<BaseViewHolder<*>>() {
    companion object {
        private const val TYPE_STORE_INFO = 0
        private const val TYPE_STORE_PRODUCTS = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType) {
            TYPE_STORE_INFO -> {
                val inflater = LayoutInflater.from(parent.context)
                val itemView = inflater.inflate(R.layout.item_store_link, parent, false)
                StoreLinkViewHolder(itemView, storeFragment)
            }
            TYPE_STORE_PRODUCTS -> {
                val inflater = LayoutInflater.from(parent.context)
                val itemView = inflater.inflate(R.layout.item_store_product, parent, false)
                StoreProductViewHolder(itemView)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val element = dataList[position]
        when (holder) {
            is StoreLinkViewHolder -> holder.onBind(element as String)
            is StoreProductViewHolder -> holder.onBind(element as Offer)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (dataList[position]) {
            is Offer -> TYPE_STORE_PRODUCTS
            is String -> TYPE_STORE_INFO
            else -> throw IllegalArgumentException("Invalid type of data $position")
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun updateAll(items: List<Any>) {
        dataList = items
        notifyDataSetChanged()
    }
}