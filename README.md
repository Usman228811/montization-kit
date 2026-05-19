# Monetization Kit

A complete Android monetization SDK for:

* AdMob Ads
* Mediation
* RevenueCat
* In-App Purchases & Subscriptions
* App Open Ads
* Native Ads
* Rewarded Ads
* Firebase Remote Config
* In-App Updates
* In-App Review
* Analytics
* AppFlyer

Supports:

* Jetpack Compose
* XML Views

---

# Table of Contents

1. Installation
2. Repositories Setup
3. Gradle Plugins
4. SDK Initialization
5. Consent Manager
6. Native Ads
7. Banner Ads
8. Interstitial Ads
9. Rewarded Ads
10. App Open Ads
11. Firebase Remote Config
12. Premium Billing
13. RevenueCat
14. In-App Update
15. In-App Review
16. Analytics
17. Locale Helper
18. Full Splash Example

---

# Installation

## Choose Your SDK

| Use Case               | Dependency                                                                  |
| ---------------------- | --------------------------------------------------------------------------- |
| Standard SDK           | `implementation("com.github.Usman228811:montization-kit:3.4.6")`            |
| Mediation SDK          | `implementation("com.github.Usman228811:montization-kit:3.4.6-adapter")`    |
| RevenueCat SDK         | `implementation("com.github.Usman228811:montization-kit:3.4.6-rc")`         |
| RevenueCat + Mediation | `implementation("com.github.Usman228811:montization-kit:3.4.6-rc-adapter")` |
| Next Gen SDK           | `implementation("com.github.Usman228811:montization-kit:1.0.1-ng")`         |
| Next Gen + Mediation   | `implementation("com.github.Usman228811:montization-kit:1.0.1-ng-adapter")` |

> RevenueCat versions increase SDK size because they include the RevenueCat SDK.

---

# Repositories Setup

## Standard SDK

Add this in `settings.gradle`:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://www.jitpack.io") }
    }
}
```

---

## Mediation SDK

Required for:

* `-adapter`
* `-rc-adapter`

```kotlin
dependencyResolutionManagement {
    repositories {

        maven { url = uri("https://www.jitpack.io") }

        maven {
            url = uri("https://repo.premiumads.net/artifactory/mobile-ads-sdk/")
        }

        maven {
            url = uri("https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea")
        }

        maven {
            url = uri("https://artifact.bytedance.com/repository/pangle/")
        }
    }
}
```

---

# Gradle Plugins

## libs.versions.toml

```toml
[plugins]
gmsServiceVersion = "4.4.4"
firebaseCrashlyticsVersion = "3.0.6"
firebasePerfVersion = "2.0.1"

gmsServicePlugin = { id = "com.google.gms.google-services", version.ref = "gmsServiceVersion" }
firebaseCrashlyticsPlugin = { id = "com.google.firebase.crashlytics", version.ref = "firebaseCrashlyticsVersion" }
firebasePerfPlugin = { id = "com.google.firebase.firebase-perf", version.ref = "firebasePerfVersion" }
```

---

## Project-level build.gradle

```kotlin
plugins {
    alias(libs.plugins.gmsServicePlugin) apply false
    alias(libs.plugins.firebaseCrashlyticsPlugin) apply false
    alias(libs.plugins.firebasePerfPlugin) apply false
}
```

---

## App-level build.gradle

```kotlin
plugins {
    alias(libs.plugins.gmsServicePlugin)
    alias(libs.plugins.firebaseCrashlyticsPlugin)
    alias(libs.plugins.firebasePerfPlugin)
}
```

---

# Supported Mediation Networks

* Pangle
* Liftoff/Vungle
* Meta
* Mintegral
* Inmobi

---

# SDK Initialization

## Manifest Setup

Add inside `<application>` tag:

```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="@string/your_app_id" />
```

---

## Initialize SDK

```kotlin id="0w8xkt"
AdKit.init(

    isDebug = BuildConfig.DEBUG,
    context = this,

    // Optional AppFlyer SDK Key
    appFlyerSdkKey = "",

    // App Open Ad Id
    openAdId = "ca-app-pub-3940256099942544/9257395921",

    // Interstitial Ads
    mapOfInterIds = mapOf(

        // Single Ad Id
        "splash_inter" to "ca-app-pub-3940256099942544/1033173712",

        // Single Ad Id
        "home_inter" to "ca-app-pub-3940256099942544/1033173712",

        // Multiple Ad Ids (Auto Rotation)
        "inter_common" to listOf(
            "ca-app-pub-3940256099942544/1033173712",
            "ca-app-pub-3940256099942544/1033173712",
            "ca-app-pub-3940256099942544/1033173712"
        )
    ),

    // Native Ads
    mapOfNativeIds = mapOf(

        "home_native" to "ca-app-pub-3940256099942544/2247696110",

        // Multiple Ad Ids (Auto Rotation)
        "native_common" to listOf(
            "ca-app-pub-3940256099942544/2247696110",
            "ca-app-pub-3940256099942544/2247696110",
            "ca-app-pub-3940256099942544/2247696110"
        )
    ),

    // Banner Ads
    mapOfBannerIds = mapOf(

        "home_banner" to "ca-app-pub-3940256099942544/9214589741",

        "premium_banner" to "ca-app-pub-3940256099942544/2014213617",

        // Multiple Ad Ids (Auto Rotation)
        "banner_common" to listOf(
            "ca-app-pub-3940256099942544/9214589741",
            "ca-app-pub-3940256099942544/9214589741",
            "ca-app-pub-3940256099942544/9214589741"
        )
    ),

    // Reward Ads
    mapOfRewardIds = mapOf(
        "reward_id" to "your_reward_ad_id"
    ),

    // Default Remote Configs
    defaultRemoteConfigBuilder = {

        // App Open Ads
        bool("OPEN_AD_ENABLE", true)
        bool("IS_OPEN_AD_INSTANT", false)
        bool("OPEN_AD_LOADING_ENABLE", true)
        long("OPEN_AD_INSTANT_TIME", 8)

        // Interstitial Ads
        bool("INTER_LOADING_ENABLE", true)
        bool("SPLASH_INTER_LOADING_ENABLE", true)
        long("INTER_INSTANT_TIME", 8)

        // Splash Timeout
        long("splash_time", 16)

        // Native Ads
        native("home_native") {
            enable(true)
            adType(NativeAdType.SMALL_NATIVE_MEDIA_VIEW)
            refreshTime(7)
        }

        // Banner Ads
        banner("home_banner") {
            enable(true)
            bannerType(
                BannerAdType.LARGE_ANCHORED_ADAPTIVE_BANNER
            )
        }

        // Interstitial Ads
        fullScreen("home_inter") {
            enable(true)
            instantInter(true)
        }

        // Reward Ads
        fullScreen("reward_screen") {
            enable(true)
            instantReward(true)
        }
    },
	onDefaultConfigGenerated = { defaultConfigs ->
                Log.d("opoppp", "onDefaultConfigGenerated: $defaultConfigs")
    },
    onInitSdk = {

        // Analytics Debug Toast
        AdKit.analytics.showToast(false)

        // Enable/Disable Ads
        AdKit.initializer.disableAds(false)

        // Custom Native Layouts
        AdKit.nativeCustomLayoutHelper.setNativeCustomLayouts(

            largeNativeLayout =
                R.layout.large_native_layout_custom,

            largeNativeShimmer =
                R.layout.large_native_layout_shimmer
        )

        // Exclude Activities from App Open Ads
        AdKit.openAdManager.excludeActivitiesFromOpenAd(
            MainActivity::class.java
        )

        // Exclude Compose Routes
        AdKit.openAdManager.excludeNavigationRoutesFromOpenAd(
            AppRoute.SplashRoute::class.qualifiedName ?: "",
            AppRoute.FeedbackRoute::class.qualifiedName ?: ""
        )

        // Exclude XML Navigation Routes
        AdKit.openAdManager.excludeNavigationRoutesFromOpenAd(
            "fragment_splash",
            "SettingsFragment"
        )
    }
)
```

## Important Remote Config Keys

| Key                           | Description                        |
| ----------------------------- | ---------------------------------- |
| `OPEN_AD_ENABLE`              | Enable/Disable App Open Ads        |
| `IS_OPEN_AD_INSTANT`          | Enable instant App Open Ads        |
| `OPEN_AD_LOADING_ENABLE`      | Enable App Open loading dialog     |
| `OPEN_AD_INSTANT_TIME`        | Instant App Open timing            |
| `INTER_LOADING_ENABLE`        | Enable Interstitial loading dialog |
| `SPLASH_INTER_LOADING_ENABLE` | Enable Splash loading dialog       |
| `INTER_INSTANT_TIME`          | Instant Interstitial timing        |

---

# Consent Manager

```kotlin
AdKit.consentManager.gatherConsent(activity)

if (AdKit.consentManager.canRequestAds) {

}

 launch {
          consentManager.googleConsent.collectLatest {  }
		}
```

---

# Native Ads

## Compose

```kotlin
AdKitNativeAdView(
    nativeControllerConfig = NativeControllerConfig(
        placementKey = "home_native",
        adIdKey = "home_native"
    )
)
```

---

## XML

```xml
<io.monetize.kit.sdk.presentation.ui.native_ad.AdKitNativeAdViewXml
    android:id="@+id/adFrameNative"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

```kotlin
binding.adFrameNative.loadNative(
    this,
    owner = this,
    nativeControllerConfig = NativeControllerConfig(
        placementKey = "home_native",
        adIdKey = "home_native"
    )
)
```

---

# Banner Ads

## Compose

```kotlin
AdKitBannerAdView(
    bannerControllerConfig = BannerControllerConfig(
        placementKey = "home_banner",
        adIdKey = "home_banner"
    )
)
```

---

## XML

```xml
<io.monetize.kit.sdk.presentation.ui.banner.AdKitBannerAdViewXml
    android:id="@+id/adFrame"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

```kotlin
binding.adFrame.loadBanner(
    this,
    owner = this,
    bannerControllerConfig = BannerControllerConfig(
        placementKey = "home_banner",
        adIdKey = "home_banner"
    )
)
```

---

# Interstitial Ads

## Show Ad

```kotlin
AdKit.interHelper.showInterAd(
    adIdKey = "inter_common",
    placementKey = "home_inter",
    activity = activity,

    // Optional
    prefKey = "inter_pref_key",
    counter = 1,

    listener = object : InterstitialControllerListener {

        override fun onAdClosed(
            isInterShowed: Boolean,
            reason: String
        ) {

        }
    }
)
```

---

## Preload Ad

```kotlin
AdKit.interHelper.preLoadInter(
    activity = activity,
    adIdKey = "inter_common",
    placementKey = "home_inter"
)
```

---

# Rewarded Ads

## Show Reward Ad

```kotlin
AdKit.rewardHelper.showRewardAd(
    adIdKey = "reward_id",
    placementKey = "reward_placement",
    activity = activity,
    listener = object : RewardedControllerListener {

        override fun onRewardDismissed(
            isRewarded: Boolean,
            reason: String
        ) {

        }
    }
)
```

---

# App Open Ads

## Setup in Application Class

Register activity lifecycle callbacks in your `Application` class.

```kotlin

class AppClass : Application(), ActivityLifecycleCallbacks {

 companion object {
        var appContext: Context? = null
    }

 override fun onCreate() {
        super.onCreate()
        appContext = this
   }
}


fun initializeAppClass() {
    try {
        registerActivityLifecycleCallbacks(this)
    } catch (_: Exception) {
    }
}

override fun onActivityCreated(
    activity: Activity,
    savedInstanceState: Bundle?
) {
}

override fun onActivityStarted(activity: Activity) {
    handleCurrentActivity(activity)
}

override fun onActivityResumed(activity: Activity) {
    handleCurrentActivity(activity)
}

private fun handleCurrentActivity(activity: Activity) {
    AdKit.interHelper.setAppInPause(false)
    AdKit.openAdManager.setActivity(activity)
}

override fun onActivityPaused(activity: Activity) {
    AdKit.interHelper.setAppInPause(true)
}

override fun onActivityStopped(activity: Activity) {
}

override fun onActivitySaveInstanceState(
    activity: Activity,
    bundle: Bundle
) {
}

override fun onActivityDestroyed(activity: Activity) {
    AdKit.openAdManager.setActivity(null)
    AdKit.interHelper.setAppInPause(false)
}
```

Call this once from your `MainActivity` or during app startup:

```kotlin
(appContext as AppClass).initializeAppClass()
```

---

## App Open Ad Listener

You can attach App Open Ad listeners inside the `onInitSdk` block.

```kotlin
AdKit.openAdManager.setOpenAdListeners(
    object : OpenAdListener {

        override fun onAdShow() {
            Log.d("AdKit", "App Open Ad Showed")
        }

        override fun onAdLoaded() {
            Log.d("AdKit", "App Open Ad Loaded")
        }

        override fun onAdDismissed() {
            Log.d("AdKit", "App Open Ad Dismissed")
        }

        override fun onAdFailed(error: String) {
            Log.d("AdKit", "App Open Ad Failed: $error")
        }
    }
)
```

---

## Exclude Screens from App Open Ads

You can exclude specific activities, Compose routes, or XML navigation destinations.

```kotlin
// Exclude Activity
AdKit.openAdManager.excludeActivitiesFromOpenAd(
    MainActivity::class.java
)

// Exclude Compose Routes
AdKit.openAdManager.excludeNavigationRoutesFromOpenAd(
    AppRoute.SplashRoute::class.qualifiedName ?: "",
    AppRoute.FeedbackRoute::class.qualifiedName ?: ""
)

// Exclude XML Navigation Routes
AdKit.openAdManager.excludeNavigationRoutesFromOpenAd(
    "fragment_splash",
    "SettingsFragment"
)
```

---

## Enable or Disable App Open Ads Temporarily 

```kotlin
AdKit.openAdManager.canShowOpenAd(true)
```

Use `false` when you want to temporarily disable App Open Ads:

```kotlin
AdKit.openAdManager.canShowOpenAd(false)
```

---

# Firebase Remote Config

## Fetch Values

```kotlin
AdKit.firebaseHelper.fetchRemoteValues(
    BuildConfig.DEBUG
)

launch {
                firebaseHelper.apply {
                    configFetched.collectLatest {

                    }
                }
            }
```

## Read Values

```kotlin
val isEnabled = firebaseBoolean(
    "HOME_NATIVE_ENABLE",
    false
)
```

# Remote Config Keys

## Open Ads

```text
OPEN_AD_ENABLE
IS_OPEN_AD_INSTANT
OPEN_AD_INSTANT_TIME
```

## Interstitial Ads

```text
INTER_LOADING_ENABLE
INTER_INSTANT_TIME
SPLASH_INTER_LOADING_ENABLE
```

## Native Ads

```text
{placementKey}_isAdEnable
{placementKey}_nativeAdType
{placementKey}_ctaColor
{placementKey}_bgColor
{placementKey}_refreshTime
```

## Banner Ads

```text
{placementKey}_bannerAdType
```
---

# Premium Billing

## Initialize Billing

```kotlin
AdKit.premiumHelper.initBilling(
    activity,
    items = listOf(

        BillingItem.Lifetime(
            "android.test.purchased",
            BillingItem.Type.REMOVE_ADS
        ),

        BillingItem.Subscription(
            "sub_remove_ads",
            BillingItem.Type.REMOVE_ADS
        )
    )
)
```

---

## Get Product Price 
```kotlin
val feature1Price = AdKit.premiumHelper.getBillingPrice(REMOVE_ADS_ID)
Log.d(
    TAG,
     "mainOfferText=${feature1Price.mainOfferText} - period=${feature1Price.period} - freeTrialText=${feature1Price.freeTrialText} - paidTrialText=${feature1Price.paidTrialText}"
 )
```

```kotlin
AdKit.premiumHelper.purchase(
    activity,
    "sub_remove_ads"
)
```

---

## Purchase

```kotlin
AdKit.premiumHelper.purchase(
    activity,
    "sub_remove_ads"
)
```

---

## Observe Premium State

```kotlin
AdKit.premiumHelper.premiumState.collectLatest {

    val isPremium = it.isPremium
}
```

---

# RevenueCat

Use these versions for RevenueCat support:

```kotlin
implementation("com.github.Usman228811:montization-kit:3.4.6-rc")
```

or

```kotlin
implementation("com.github.Usman228811:montization-kit:3.4.6-rc-adapter")
```

> Use RevenueCat versions only if you need RevenueCat integration.

---

# In-App Update

## Compose

```kotlin
val launcher = AdKitInAppUpdateFlowResultLauncher(
    onFail = {

    }
)
```

---

## XML

```kotlin
private val updateLauncher =
    AdKitInAppUpdateManager.registerLauncher(
        this,
        onFail = {

        }
    )
```

---

## Check Update

```kotlin
AdKit.inAppUpdateManager.setUpdateStateCallback { updateState ->
            when (updateState) {
                UpdateState.Available -> inAppUpdateManager.startUpdateFlow(launcher)
                UpdateState.Downloaded -> inAppUpdateManager.updateComplete()
                UpdateState.Failed -> {}
                UpdateState.Idle -> {}
            }
        }
AdKit.inAppUpdateManager.checkUpdate(activity)
```

---

# In-App Review

```kotlin
AdKit.inAppReviewManager.startReview(
    activity,
    object : ReviewListener {

        override fun onFail() {

        }

        override fun onComplete() {

        }
    }
)
```

---

# Analytics

## Post Event

```kotlin
AdKit.analytics.postAnalytics(
    "home_btn_click"
)
```

---

## Post Screen Name

```kotlin
AdKit.analytics.postScreenName(
    "home",
    "home"
)
```

---

# Locale Helper

## Attach Language

```kotlin
override fun attachBaseContext(newBase: Context?) {
    super.attachBaseContext(
        AdKit.localeHelper.setAppLanguage(
            newBase,
            AdKit.adKitPref.appLanguageCode
        )
    )
}
```

---

## Change Language

```kotlin
AdKit.adKitPref.appLanguageCode = "en"
```

---

# Splash Ads

## Initialize Splash Ad

## Important Parameter `loadAndShow` 
- If `true`, the ad will load and show automatically. On ad failure or ad dismissed, `onAdClosed` is called.
- If `false`, only the ad loads. On success or failure, `onAdLoaded` is called — you can then show the navigation button.
- To show `AppOpenAd` on the Splash screen, add `${placementKey}_isAdOpenAd` to `true` in the `Default Remote Configs` within AppClass. By default, the Splash OpenAd is false.


```kotlin
AdKit.splashAdController.initSplashInterstitial(
    placementKey = "splash_inter",
    adIdKey = "splash_inter",
    loadAndShow = false,
    activity = activity,
    splashTime = 16,
    listener = object : InterstitialControllerListener {

        override fun onAdLoaded(reason: String) {

        }

        override fun onAdClosed(
            isInterShowed: Boolean,
            reason: String
        ) {

        }
    }
)
```

---

## Show Splash Ad

```kotlin
AdKit.splashAdController.showInterstitial(
    activity,
    object : InterstitialControllerListener {

        override fun onAdClosed(
            isInterShowed: Boolean,
            reason: String
        ) {

        }
    }
)
```

---

# Full Examples

For complete production-ready examples:

* Splash Screen ViewModel
* Premium Subscription ViewModel
* Compose Integration
* XML Integration
* Custom Native Layouts

Check the examples folder in the repository.
