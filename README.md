
# Library
```kotlin
dependencies {
    implementation("com.github.Usman228811:montization-kit:v1.7.4")
}


//app level gradle
    alias(libs.plugins.gmsServicePlugin) apply false
    alias(libs.plugins.firebaseCrashlyticsPlugin) apply false
    alias(libs.plugins.firebasePerfPlugin) apply false

// project level gradle
   alias(libs.plugins.gmsServicePlugin)
   alias(libs.plugins.firebaseCrashlyticsPlugin)
   alias(libs.plugins.firebasePerfPlugin)

```

# SDK Initialize
in App class, oncreate
```kotlin

 AdKit.init(
            context = this,
            admobId = "ca-app-pub-3940256099942544~3347511713",
            openAdId = "ca-app-pub-3940256099942544/9257395921",
            mapOfInterIds = mapOf(
                "splash_inter" to "ca-app-pub-3940256099942544/1033173712",
                "home_inter" to "ca-app-pub-3940256099942544/1033173712", // if single then take this id else will rotate for this placement
                "inter_common" to listOf(
                    "ca-app-pub-3940256099942544/1033173712",
                    "ca-app-pub-3940256099942544/1033173712",
                    "ca-app-pub-3940256099942544/1033173712"
                )
            ),
            mapOfNativeIds = mapOf(
                "home_native" to "ca-app-pub-3940256099942544/2247696110",
            ),
            mapOfBannerIds = mapOf(
                "home_banner" to "ca-app-pub-3940256099942544/9214589741",
            ),
          onInitSdk = {
                //optional
                AdKit.analytics.showToast(false)

                // if added then will take these layouts else default layouts
                AdKit.initializer.setNativeCustomLayouts(
                    bigNativeLayout = R.layout.large_native_layout_custom,
                    bigNativeShimmer = R.layout.large_native_layout_shimmer,
                )

                //  stop showing open ads from xml activities
                AdKit.openAdManager.excludeActivitiesFromOpenAd(MainActivity::class.java,)

                //  stop showing open ads from compose routes
                AdKit.openAdManager.excludeComposeRoutesFromOpenAd(
                    AppRoute.SplashRoute::class.qualifiedName?: "",
                    AppRoute.FeedbackRoute::class.qualifiedName?: "",
                    AppRoute.PrivacyPolicy::class.qualifiedName?: "",
                )
            })


```

# Consent Manager 
```kotlin
 viewModelScope.apply {

            launch {
                AdKit.consentManager.googleConsent.collectLatest {
                    runSplash()
                }
            }
        }

 fun initConsent(activity: Activity) {
        viewModelScope.launch {
            if (state.value.isConsentManager.not()) {
                _state.update {
                    it.copy(
                        isConsentManager = true
                    )
                }
                if (!pref.isAppPurchased && adSdkInternetController.isConnected) {
                    AdKit.consentManager.gatherConsent(activity)
                    if (AdKit.consentManager.canRequestAds) {
                        runSplash()
                    }
                } else {
                    runSplash()
                }
            }
        }
    }

```



# Native Ad
## AdTypes
 - 0 for large native
 - 1 for split 
 - 2 for small native

## remote config values 
Add default values in the default values
- {$placementkey}_isAdEnable
- {$placementkey}_adType
- {$placementkey}_loadNewAd

## Compose support for native
```kotlin
AdKitNativeAdView(
            nativeControllerConfig = NativeControllerConfig(
                placementKey = "home_native", //placement key will be unique for that placement
                adIdKey = "home_native", // ad id key can be common for different placements
                adType = 2
            )
        )


// for dialog in the same screen
AdKitNativeAdViewDialog(
            nativeControllerConfig = NativeControllerConfig(
                placementKey = "home_native", //placement key will be unique for that placement
                adIdKey = "home_native", // ad id key can be common for different placements
                adType = 2
            )
        )
```
## XML support for native

```kotlin

    <io.monetize.kit.sdk.presentation.ui.native_ad.AdKitNativeAdViewXml
        android:id="@+id/adFrameNative"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="@dimen/_50sdp"
        app:layout_constraintTop_toBottomOf="@+id/btn_settings" />

    // for dialog in the same screen
    <io.monetize.kit.sdk.presentation.ui.native_ad.AdKitNativeAdViewDialogXml
        android:id="@+id/adFrameNative"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="@dimen/_50sdp"
        app:layout_constraintTop_toBottomOf="@+id/btn_settings" />


  // in activity or fragment
  binding.adFrameNative.loadNative(
            this@MainXmlActivity,
           nativeControllerConfig = NativeControllerConfig(
                placementKey = "home_native", //placement key will be unique for that placement
                adIdKey = "home_native", // ad id key can be common for different placements
                adType = 2
            )
        )


// custom natives

// in app class, onInitSdk ->
AdKit.initializer.setNativeCustomLayouts(
            bigNativeLayout = R.layout.large_native_layout_custom,
            bigNativeShimmer = R.layout.large_native_layout_shimmer, // if added then shows this shimmer else default
                )
```

# Analytics Events

```kotlin
AdKit.analytics.postAnalytics("Main_idenify_plant_btn")


🔴 if toast events in debug mode just add in app class

onInitSdk = {
                AdKit.analytics.showToast(BuildConfig.DEBUG)
                
            })

```

# App Open Ad


```kotlin
 fun initializeAppClass() {
        try {
            registerActivityLifecycleCallbacks(this)
        } catch (_: Exception) {
        }
    }


    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        handleCurrentActivity(activity)
    }


    private fun handleCurrentActivity(activity: Activity) {
        AdKit.interHelper.setAppInPause(false)
        AdKit.openAdManager.setActivity(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        handleCurrentActivity(activity)
    }

    override fun onActivityStopped(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {
        AdKit.interHelper.setAppInPause(true)
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {

        AdKit.openAdManager.setActivity(null)
        AdKit.interHelper.setAppInPause(false)
    }

```

## In main activity

```kotlin

    (appContext as AppClass).initializeAppClass()


🔴 if you are using compose then to stop showing open ad in splash 
     adSdkOpenAdManager.setCurrentComposeRoute(
            SplashRoute::class.qualifiedName
        )


 
🔴 if using compose, in oncreate of main activity -> set current route in AdSdkOpenAdManager so that open ad knows if it is to stop show ad in this screen or not

val navController = rememberNavController()
            val currentDestination by navController.currentBackStackEntryFlow.collectAsState(
                initial = null
            )
            val currentRoute = currentDestination?.destination?.route
            adSdkOpenAdManager.setCurrentComposeRoute(currentRoute)

```

## stop showing open ad, in app class oncreate
```kotlin

        // stop showing open ad in compose screens
        AdKit.openAdManager.excludeComposeRoutesFromOpenAd(
            SplashRoute::class.qualifiedName ?: ""
        )

         // stop showing open ad in xml activities
        AdKit.openAdManager.excludeComposeRoutesFromOpenAd(
            MainActivity::class.java
        )

```


# Banner Ad


## remote config values 
Add default values in the default values
- {$placementkey}_isAdEnable
- {$placementkey}_adType
- {$placementkey}_loadNewAd
- {$placementkey}_isCollapsible
- {$placementkey}_isCollapsibleTop

## Compose support for banner

```kotlin
  AdKitBannerAdView(
                 bannerControllerConfig = BannerControllerConfig(
                placementKey = "home_banner",
                adIdKey = "home_banner",
            )
            )

````

## Xml support for banner

```kotlin

    <io.monetize.kit.sdk.presentation.ui.banner.AdKitBannerAdViewXml
        android:id="@+id/adFrame"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:layout_constraintBottom_toBottomOf="parent" />

binding.adFrame.loadBanner(
            this@MainXmlActivity,
            bannerControllerConfig = BannerControllerConfig(
                placementKey = "home_native", //placement key will be unique for that placement
                adIdKey = "home_native", // ad id key can be common for different placements
            )
        )

```

# Interstitial Ad


## remote config values 
Add default values in the default values
- {$placementkey}_isAdEnable
- {$placementkey}_isInterInstant

```kotlin

AdKit.interHelper.showInterAd(
                adIdKey = "inter_common",// for ad id key
                placementKey = "inter_btn_plant", // for enable disable or instant
                activity = activity,
                listener = object : InterstitialControllerListener {
                    override fun onAdClosed() {

                        }

                },
		//optional
                prefKey = "common_pref", 
                counter = 2 //from remote conigs
            )

```

# In App Update

```kotlin
// in compose
   val launcher = AdKitInAppUpdateFlowResultLauncher (onFail = {
       //continue for consent
    })

    //in xml 
    private val updateLauncher: ActivityResultLauncher<IntentSenderRequest> by lazy {
        AdKitInAppUpdateManager.registerLauncher(this) {
           //continue for consent
        }
    }

fun checkUpdate(
        context: Context,
        launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>) {

        AdKit.inAppUpdateManager.setUpdateStateCallback { updateState ->
            when (updateState) {
                UpdateState.Available -> {

                    AdKit.inAppUpdateManager.startUpdateFlow(launcher)
                }

                UpdateState.Downloaded -> {
                    /* show restart dialog
                    
                     or

                    adSdkInAppUpdateManager.updateComplete()*/

                }

                UpdateState.Failed -> {
                    //continue for consent

                }

                UpdateState.Idle -> {

                }
            }

        }

        AdKit.inAppUpdateManager.checkUpdate(context)

    }

    //on cleanerd in viewmodel | ondestroy
    override fun onCleared() {
        super.onCleared()
        AdKit.inAppUpdateManager.unRegisterLister()
        animator?.cancel()
    }

```

# Firebase Remote Configs

```kotlin

//oncreate 
 AdKit.firebaseHelper.apply {
            viewModelScope.launch {
                configFetched.collectLatest {
                    try {
                        val SPLASH_TIME = getLong("SPLASH_TIME", 16L)
                        val HOME_NATIVE_ENABLE = getBoolean("HOME_NATIVE_ENABLE", true)
                        val IS_AI = getString("IS_AI", "YES")
                       //continue
                    
                    } catch (e: Exception) {
                        //continue
                    }
                }
            }

            🔴 Add all default values in the default remote config like this

              fetchRemoteValues(BuildConfig.DEBUG) {
               
                bool("home_native_isAdEnable", true)
                bool("home_banner_isAdEnable", true)
                bool("home_banner_isCollapsible", true)
                bool("home_banner_isCollapsibleTop", false)
                bool("subscription_native_isAdEnable", true)
                long("home_native_adType", 1L)
                long("subscription_native_adType", 1L)
                long("SPLASH_TIME", 16)
            }

        }
```

# One Time Purchase

```kotlin
 //in splash

    //replace it with your product id

        AdKit.purchaseHelper.initBilling("one_time_purchase_id")



    //in viewmodel or screen

     viewModelScope.apply {

            launch {

                AdKit.purchaseHelper.appPurchased.collectLatest { isPurchased ->
                    Log.d("ioiioo", "isPurchased: $isPurchased")
                }
            }

            launch {

                AdKit.purchaseHelper.productPriceFlow.collectLatest {

                    Log.d("ioiioo", "productPriceFlow: ${it.price.ifEmpty { "..." }}")

                }
            }
        }

        //to purchase

        AdKit.purchaseHelper.purchaseProduct(activity)
```

# Subscription

```kotlin


// viewmodel for subscription


data class SettingScreenState(
    val weeklyPrice: String = "",
    val monthlyPrice: String = "",
    val yearlyPrice: String = "",
    val subscribedId: String = "",
    val selectedButtonPos: Int = 0,
    val buttonText: String = "subscribe",
)

class SubscriptionViewModel(
    
) : ViewModel() {

    
    private var _state = MutableStateFlow(SettingScreenState())
    val state = _state.asStateFlow()

    private val subscriptionMap = mapOf(
        0 to "weekly_subscription2",
        1 to "monthly1_subscription",
        2 to "yearly_subscription"
    )

    private fun selectedId() = subscriptionMap[state.value.selectedButtonPos]

    init {
        viewModelScope.apply {
            launch {
                AdKit.subscriptionHelper.subscriptionProducts.collectLatest {
                    _state.update {
                        it.copy(
                            weeklyPrice = getBillingPrice("weekly_subscription2", "P1W"),
                            monthlyPrice = getBillingPrice("monthly1_subscription", "P1M"),
                            yearlyPrice = getBillingPrice("yearly_subscription", "P1Y"),
                        )
                    }
                }
            }
            launch {
                AdKit.subscriptionHelper.subscribedId.collectLatest { subscribedId ->
                    _state.update {
                        it.copy(
                            subscribedId = subscribedId
                        )
                    }
                }
            }

            launch {
                AdKit.subscriptionHelper.historyFetched.collectLatest {

                    val buttonText = when {
                        state.value.subscribedId.isEmpty() -> "subscribe"
                        state.value.subscribedId == selectedId() -> "cancel subscription"
                        AdKit.subscriptionHelper.isSubscriptionUpdateSupported() -> "update subscription"
                        else -> state.value.buttonText // fallback to existing text
                    }

                    _state.update {
                        it.copy(buttonText = buttonText)
                    }
                }
            }
        }
    }

    fun loadProducts(activity: Activity, list: List<String>) {
        AdKit.subscriptionHelper.loadProducts(activity, list)
    }


    private fun getBillingPrice(productId: String, billingPeriod: String): String {
        return AdKit.subscriptionHelper.getBillingPrice(productId, billingPeriod).ifEmpty { "..." }


    }

    fun updateSelectedButtonPos(selectedButtonPos: Int) {
        _state.update {
            it.copy(
                selectedButtonPos = selectedButtonPos
            )
        }
        AdKit.subscriptionHelper.querySubscriptionProducts()
    }

    fun purchase(activity: Activity) {
        AdKit.subscriptionHelper.purchase(activity, selectedId())
    }
}
```

## how to implement subscription

```kotlin
// inscreen

  LaunchedEffect(Unit) {
            subscriptionViewModel.loadProducts(
                activity,
                listOf(
                    "weekly_subscription2",
                    "monthly1_subscription",
                    "yearly_subscription"
                )
            )
        }

// i.e weekly 
Button(modifier = Modifier.fillMaxWidth(),
            onClick = {
                subscriptionViewModel.updateSelectedButtonPos(0)

            }
        ) {
            Text(
                text = "weekly ${state.weeklyPrice}"
            )
        }



/* 

WHAT WILL HAPPEN IF YOU CLICK SUBSCRIBE BUTTON

CODE WILL CHECK ITS STATUS AND WILL PROCEED

=> if already subscribed -- will goto cancel subscription screen
=> not already subscribed -- will subscibe
=> already subsribed but now other plan is selected -- will goto update subscription


*/

  Button(modifier = Modifier.fillMaxWidth(),
            onClick = {
                subscriptionViewModel.purchase(activity)
            }
        ) {
            Text(
                text = state.buttonText
            )
        }
```

# Splash ViewModel 

```kotlin

data class SplashScreenState(
    val isConsentManager: Boolean = false,
    val initializeSplash: Boolean = false,
    val fireBaseFetch: Boolean = false,
    val showRestartDialog: Boolean = false,
    val isAppResumed: Boolean = false,
    val moveToMain: Boolean = false,
    val isPurchased: Boolean = false,
    val runSplash: Boolean = false,
    val progress: Int = 0,
)

class SplashViewModel(
    private val prefHelper: PrefHelper,
) : ViewModel() {

    private var _state = MutableStateFlow(SplashScreenState())
    val state = _state.asStateFlow()
    private var isInterAdShowed = false
    private var isInterAdCalled = false
    private var animator: ValueAnimator? = null

    init {
        AdKit.analytics.postAnalytics("Splash_launch")
        AdKit.splashAdController.resetSplash()
        collections()
        startProgressAnimation()
        purchaseHelper.initBilling("one_time_purchase_id")
    }

    private fun onResume() {
        if (state.value.runSplash) {
            animator?.resume()
        }
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isAppResumed = true
                )
            }
        }
    }

    private fun onPause() {
        if (state.value.runSplash) {
            animator?.pause()
        }
        if (!isInterAdShowed && isInterAdCalled) {
            AdKit.splashAdController.pauseAd()
        }
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isAppResumed = false
                )
            }
        }
    }

    fun observeLifecycle(lifecycleOwner: LifecycleOwner) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> onResume()
                Lifecycle.Event.ON_PAUSE -> onPause()
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        // Ensure observer is removed when lifecycle is destroyed
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            }
        })
    }

    fun checkForUpdate(
        activity: Activity,
        launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
    ) {
        inAppUpdateManager.setUpdateStateCallback { updateState ->
            when (updateState) {
                UpdateState.Available -> {
                    inAppUpdateManager.startUpdateFlow(launcher)
                }

                UpdateState.Downloaded -> {
                    inAppUpdateManager.updateComplete()

                }

                UpdateState.Failed -> {
                    initConsent(activity)

                }

                UpdateState.Idle -> {

                }
            }
        }
        inAppUpdateManager.checkUpdate(activity)
    }


    private fun collections() {
        viewModelScope.apply {
            launch {
                consentManager.googleConsent.collectLatest {
                    initializeSplash()
                }
            }

            launch {
                firebaseHelper.apply {

                    firebaseHelper.configFetched.collectLatest {
                        try {
                            assignRemoteValues(this)
                            runSplash()
                        } catch (e: Exception) {
                            runSplash()
                        }
                    }
                }
            }

            launch {
                purchaseHelper.appPurchased.collectLatest { result ->
                    _state.update {
                        it.copy(isPurchased = result)
                    }
                }
            }
        }
    }

    fun initConsent(activity: Activity) {
        viewModelScope.launch {
            if (state.value.isConsentManager.not()) {
                _state.update {
                    it.copy(
                        isConsentManager = true
                    )
                }
                if (!adKitPref.isAppPurchased && internetController.isConnected) {
                    consentManager.gatherConsent(activity)
                    if (consentManager.canRequestAds) {
                        initializeSplash()
                    }
                } else {
                    initializeSplash()
                }
            }
        }
    }

    private fun initializeSplash() {
        viewModelScope.launch {
            if (state.value.initializeSplash.not()) {
                _state.update {
                    it.copy(
                        initializeSplash = true
                    )
                }
                fetchFirebase()
            }
        }
    }

    private fun fetchFirebase() {
        if (state.value.fireBaseFetch.not()) {
            _state.update {
                it.copy(
                    fireBaseFetch = true
                )
            }
            firebaseHelper.fetchRemoteValues(
                isDebug = isDebug,
            ) {
                bool("native_language_splash_isAdEnable", true)
                bool("inter_home_isAdEnable", true)
                bool("splash_inter_isAdEnable", true)
                bool("exit_dialog_isAdEnable", true)
                bool("native_home_isAdEnable", true)
                bool("history_native_isAdEnable", true)
                bool("native_language_settings_isAdEnable", true)
                bool("native_my_plant_isAdEnable", true)
                bool("native_water_isAdEnable", true)
                bool("banner_result_isAdEnable", true)
                bool("banner_camera_isAdEnable", true)
                bool("boarding_native_isAdEnable", true)
                bool("OPEN_AD_ENABLE", true)
                bool("INTER_LOADING_ENABLE", true)
                bool("OPEN_AD_LOADING_ENABLE", true)


                long("native_language_settings_adType", 0)
                long("native_language_splash_adType", 0)
                long("native_my_plant_adType", 2)
                long("exit_dialog_adType", 2)
                long("native_home_adType", 2)
                long("native_water_adType", 2)
                long("history_native_adType", 2)
                long("boarding_native_adType", 0)
            }
        }
    }

    private fun runSplash() {
        viewModelScope.launch {
            if (state.value.runSplash.not()) {
                _state.update {
                    it.copy(
                        runSplash = true
                    )
                }
            }
        }
    }

    private fun startProgressAnimation() {
        animator?.cancel()
        animator = ValueAnimator.ofInt(0, 100).apply {
            duration = 25_000L
            addUpdateListener { animation ->
                val value = animation.animatedValue as? Int
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            progress = value ?: 50
                        )
                    }
                }
            }
            start()
        }
    }

    fun showSplashAd(mContext: Activity) {
        if (!isInterAdCalled) {
            isInterAdCalled = true

            when (LANG_APPEAR.toInt()) {
                0 -> {
                }

                1 -> {
                    if (!prefHelper.langAppeared) {
                        preLoadNative(mContext)
                    }

                }

                2 -> {
                    preLoadNative(mContext)
                }
            }
            AdKit.splashAdController.initSplashAdmob(
                placementKey = "splash_inter",
                activity = mContext,
                interAdsConfigs = InterAdsConfigs(
                    openAdEnable = firebaseHelper.getBoolean("OPEN_AD_ENABLE", true),
                    interLoadingEnable = firebaseHelper.getBoolean(
                        "INTER_LOADING_ENABLE",
                        true
                    ),
                    openAdLoadingEnable = firebaseHelper.getBoolean(
                        "OPEN_AD_LOADING_ENABLE",
                        true
                    ),

                    
                  //  openAdInstant = false,
                  //  instantOpenAdTime = 8, 
                  //  instantInterTime = 8
                ),
                listener = object : InterstitialControllerListener {
                    override fun onAdShow() {
                        super.onAdShow()
                        isInterAdShowed = true
                        animator?.cancel()
                        viewModelScope.launch {
                            _state.update {
                                it.copy(
                                    progress = 100,
                                )
                            }
                        }
                    }

                    override fun onAdClosed() {
                        animator?.cancel()
                        _state.update {
                            it.copy(
                                progress = 100,
                                moveToMain = true,
                            )
                        }
                    }
                })
        }

    }

    fun resumeSplashAd(activity: Activity) {
        if (!isInterAdShowed && isInterAdCalled) {
            AdKit.splashAdController.resumeAd(activity, true)
        }
    }

    private fun preLoadNative(mContext: Activity) {
        AdKit.preLoadNative.preLoadNativeAd(
            mContext = mContext,
            nativeControllerConfig = NativeControllerConfig(
                placementKey = "native_language_splash",
                adIdKey = "native_language_splash",
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        inAppUpdateManager.unRegisterLister()
        animator?.cancel()
    }

}
````
# Splash Screen

```kotlin
val activity = LocalActivity.current as Activity
    val state by splashViewModel.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    val launcher = AdKitInAppUpdateFlowResultLauncher(onFail = {
        splashViewModel.initConsent(activity)
    })
    LaunchedEffect(Unit) {
        splashViewModel.checkForUpdate(activity, launcher)
    }
    LaunchedEffect(Unit) {
        splashViewModel.observeLifecycle(lifecycleOwner)
    }

    LaunchedEffect(key1 = state.runSplash) {
        if (state.runSplash) {
            splashViewModel.showSplashAd(activity)
        }
    }
    LaunchedEffect(key1 = state.moveToMain) {
        if (state.moveToMain) {
            gotoMain()

        }
    }

    LaunchedEffect(state.isAppResumed) {
        if (state.isAppResumed) {
            splashViewModel.resumeSplashAd(activity)
        }
    }
```










