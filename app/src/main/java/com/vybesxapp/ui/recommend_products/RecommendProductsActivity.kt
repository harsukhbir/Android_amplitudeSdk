package com.vybesxapp.ui.recommend_products

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.vybesxapp.R
import com.vybesxapp.base.BaseActivity
import com.vybesxapp.service.ApiClient
import com.vybesxapp.service.ErrorParser
import com.vybesxapp.service.domain_model.Product
import com.vybesxapp.ui.product_details.ProductDetailsActivity
import com.vybesxapp.utils.Money
import kotlinx.coroutines.*
import retrofit2.HttpException

class RecommendProductsActivity : BaseActivity() {
    private lateinit var mProductRV: RecyclerView
    private lateinit var mLoadingBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recommend_products)

        mProductRV = findViewById(R.id.product_list)
        mLoadingBar = findViewById(R.id.loading_bar)
        mProductRV.layoutManager = GridLayoutManager(this, 2)

        recommendProducts()
    }

    private fun recommendProducts() {
        mLoadingBar.visibility = View.VISIBLE
        val errorHandler = CoroutineExceptionHandler { _, e ->
            if (e is HttpException) {
                val error = ErrorParser.parseHttpResponse(e.response()?.errorBody())
                Toast.makeText(this@RecommendProductsActivity, error?.msg, Toast.LENGTH_LONG).show()
            }
        }
        val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)
        coroutineScope.launch(errorHandler) {
            val productList = mApiClient.recommendProducts()
            val adapter = ProductAdapter(productList)
            mProductRV.adapter = adapter
            mLoadingBar.visibility = View.GONE
        }
    }
}

class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun onBind(product: Product) {
        val brandTextView = itemView.findViewById<TextView>(R.id.brand)
        val productNameTextView = itemView.findViewById<TextView>(R.id.product_name)
        val productImageView = itemView.findViewById<ImageView>(R.id.product_image)
        val retailPriceTV = itemView.findViewById<TextView>(R.id.retail_price_tv)
        brandTextView.text = product.brand
        productNameTextView.text = product.name
        productImageView.load(product.imageUrl) {
            crossfade(true)
        }
        retailPriceTV.text = Money.formatCurrency(product.pricing.retailPrice)

        itemView.setOnClickListener {
            val intent = Intent(itemView.context, ProductDetailsActivity::class.java)
            intent.putExtra(ProductDetailsActivity.PARAMS.PRODUCT_ID, product.id)
            itemView.context.startActivity(intent)
        }
    }
}

class ProductAdapter(private val productList: List<Product>) :
    RecyclerView.Adapter<ProductViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.onBind(productList[position])
    }

    override fun getItemCount(): Int {
        return productList.size
    }
}