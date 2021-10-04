package com.vybesxapp.service

import android.content.Context
import com.vybesxapp.BuildConfig
import com.vybesxapp.service.api.*
import com.vybesxapp.service.domain_model.Notification
import com.vybesxapp.service.domain_model.Product
import com.vybesxapp.service.domain_model.User
import com.vybesxapp.service.dto.StoreResponse
import com.vybesxapp.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiClient(context: Context?) {
    private val storeService: StoreService
    private val recommendProductService: RecommendProductService
    private val catalogService: CatalogService
    private val rewardService: RewardService
    private val sellerService: SellerService
    private val authService: AuthService
    private val notificationService: NotificationService
    private val userService: UserService
    private val walletService: WalletService

    init {
        val accessToken = context?.let { SessionManager(it).getAccessToken() }
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        val httpClient = OkHttpClient.Builder().apply {
            addInterceptor(
                Interceptor { chain ->
                    val builder = chain.request().newBuilder()
                    builder.header("X-App-Version", BuildConfig.VERSION_NAME)
                    builder.header("X-Platform", "Android")
                    accessToken?.let { builder.header("authorization", "bearer $accessToken") }
                    return@Interceptor chain.proceed(builder.build())
                }
            )
            addInterceptor(logging)
        }.build()
        val retrofit = Retrofit.Builder().baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()

        storeService = retrofit.create(StoreService::class.java)
        recommendProductService = retrofit.create(RecommendProductService::class.java)
        catalogService = retrofit.create(CatalogService::class.java)
        rewardService = retrofit.create(RewardService::class.java)
        sellerService = retrofit.create(SellerService::class.java)
        authService = retrofit.create(AuthService::class.java)
        notificationService = retrofit.create(NotificationService::class.java)
        userService = retrofit.create(UserService::class.java)
        walletService = retrofit.create(WalletService::class.java)
    }

    suspend fun getStoreData(): StoreResponse {
        return storeService.retrieveStoreData()
    }

    suspend fun recommendProducts(): List<Product> {
        val res = recommendProductService.recommendProducts()
        return res.map {
            Product(
                it.id,
                it.brand,
                it.description,
                it.name,
                it.imageUrl,
                Product.Pricing(
                    it.pricing.retail,
                    it.pricing.sale
                )
            )
        }
    }

    suspend fun retrieveProductDetails(id: String): Product {
        val response = catalogService.retrieveProductDetails(id)
        return Product(
            response.id,
            response.brand,
            response.description,
            response.name,
            response.imageUrl,
            Product.Pricing(
                response.pricing.retail,
                response.pricing.sale
            )
        )
    }

    suspend fun addProductsToStore(productId: String) {
        return storeService.addProductsToStore(AddProductsRequest(productIds = arrayOf(productId)))
    }

    suspend fun retrieveOfferDetails(productId: String): OfferDetailsResponse {
        return storeService.retrieveOfferDetails(productId)
    }

    suspend fun updateStoreSlug(slug: String) {
        return storeService.updateStoreSlug(UpdateStoreSlugRequest(slug))
    }

    suspend fun retrieveSellerInfo(): SellerInfoResponse {
        return sellerService.retrieveSellerInfo()
    }

    suspend fun retrieveTotalSale(): TotalSaleResponse {
        return sellerService.retrieveTotalSale()
    }

    suspend fun retrieveCurrentPointBalance(): PointBalanceResponse {
        return rewardService.retrieveCurrentPointBalance()
    }

    suspend fun login(email: String, password: String): LoginResponse {
        return authService.login(LoginRequest(email = email, password = password))
    }

    suspend fun register(
        name: String,
        username: String,
        email: String,
        phoneNumber: String,
        password: String,
        token: String,
        captcha: String,
    ): RegisterResponse {
        return authService.register(RegisterRequest(name = name,
            username = username,
            email = email,
            phoneNumber = phoneNumber,
            password = password,
            token = token,
            captcha = captcha))
    }

    suspend fun getCaptcha(): GetCaptchaResponse {
        return authService.retrieveCaptcha()
    }

    suspend fun verifyCode(userId: String, code: String): VerificationCodeResponse {
        return authService.verifyCode(VerificationCodeRequest(userId = userId, code = code))
    }

    suspend fun registerDeviceToken(token: String) {
        return userService.registerDeviceToken(RegisterDeviceTokenRequest(token))
    }

    suspend fun updateUserProfile(name: String?, username: String?, phoneNumber: String?) {
        val request = UpdateUserRequest(name, username, phoneNumber)
        userService.updateUserProfile(request)
    }

    suspend fun retrieveUserProfile(): User {
        val res = userService.getUserProfile()
        return User(id = res.id,
            name = res.name,
            username = res.username,
            email = res.email,
            phoneNumber = res.phoneNumber,
            profileImage = res.profileImage,
            slug = res.slug.toString())
    }

    suspend fun retrieveNotificationList(): List<Notification> {
        val response = notificationService.getNotifications()
        val notifications = ArrayList<Notification>(emptyList())
        for (notificationDto in response.data) {
            notifications.add(Notification(
                id = notificationDto.id,
                title = notificationDto.title,
                body = notificationDto.body,
                data = Notification.NotificationData(
                    notificationId = notificationDto.data.notificationId,
                    type = notificationDto.data.type,
                    content = notificationDto.data.content
                ),
                createdAt = notificationDto.createdAt
            ))
        }
        return notifications
    }

    suspend fun retrievePinnedNotifications(): List<Notification> {
        val response = notificationService.getPinnedNotifications()
        val notifications = ArrayList<Notification>(emptyList())
        for (notificationDto in response.data) {
            notifications.add(Notification(
                id = notificationDto.id,
                title = notificationDto.title,
                body = notificationDto.body,
                data = Notification.NotificationData(
                    notificationId = notificationDto.data.notificationId,
                    type = notificationDto.data.type,
                    content = notificationDto.data.content
                ),
                createdAt = notificationDto.createdAt
            ))
        }
        return notifications
    }

    suspend fun retrieveNotificationDetails(notificationId: String): Notification {
        val res = notificationService.getNotificationDetails(notificationId)
        return Notification(id = res.data.id,
            title = res.data.title,
            body = res.data.body,
            data = Notification.NotificationData(notificationId = res.data.data.notificationId,
                type = res.data.data.type,
                res.data.data.content),
            createdAt = res.data.createdAt)
    }

    suspend fun retrieveWalletInfo(): WalletInfoResponse {
        return walletService.retrieveWalletInfo()
    }

    suspend fun retrieveAvailableBankToWithdraw(): AvailableBanksResponse {
        return walletService.retrieveAvailableBanks()
    }

    suspend fun withdrawMoney(
        accountName: String,
        accountNumber: String,
        amount: Int,
        bankCode: String,
    ) {
        return walletService.requestWithdrawMoney(WithdrawMoneyRequest(accountName = accountName,
            accountNumber = accountNumber,
            amount = amount,
            bankCode = bankCode))
    }
}