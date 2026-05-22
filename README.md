# Monetization Kit Documentation

A comprehensive Kotlin library for Android (Jetpack Compose + XML), designed to streamline monetization with support for ads, in-app updates, in-app review, in-app purchases, subscriptions, App-Flyer, and analytics.

---

# Installation

## Add Dependency

To integrate the Monetization Kit into your project, include the following in your app's `build.gradle`:

```kotlin id="o5d2bl"
dependencies {

    // Standard SDK 
    implementation("com.github.Usman228811:montization-kit:3.4.8")

    // Standard SDK + Mediation Adapters
    implementation("com.github.Usman228811:montization-kit:3.4.8-adapter")

    // RevenueCat Support
    implementation("com.github.Usman228811:montization-kit:3.4.8-rc")

    // RevenueCat + Mediation Adapters
    implementation("com.github.Usman228811:montization-kit:3.4.8-rc-adapter")


    // Next Gen SDK
    implementation("com.github.Usman228811:montization-kit:1.0.1-ng")

    // Next Gen SDK + Mediation
    implementation("com.github.Usman228811:montization-kit:1.0.1-ng-adapter")

}
```


# Configure Repositories

## Standard SDK

If you are using the standard Monetization Kit SDK (without mediation), add only the JitPack repository in your `settings.gradle`:

```kotlin id="c5gbuw"
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://www.jitpack.io") }
    }
}
```

---

## Mediation SDK

If you are using any mediation version:

* `-adapter`
* `-rc-adapter`
* `-ng-adapter`

then add the following repositories in your `settings.gradle`:

```kotlin id="g2f1y2"
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

> These additional repositories are required only for mediation network adapters.

---

# Gradle Plugins

Define the required plugins in your `.toml` file:

```toml id="k1brlw"
[plugins]
gmsServiceVersion = "4.4.4"
firebaseCrashlyticsVersion = "3.0.6"
firebasePerfVersion = "2.0.1"

gmsServicePlugin = { id = "com.google.gms.google-services", version.ref = "gmsServiceVersion" }
firebaseCrashlyticsPlugin = { id = "com.google.firebase.crashlytics", version.ref = "firebaseCrashlyticsVersion" }
firebasePerfPlugin = { id = "com.google.firebase.firebase-perf", version.ref = "firebasePerfVersion" }
```

Apply plugins in your project-level `build.gradle`:

```kotlin id="4u7h0q"
plugins {
    alias(libs.plugins.gmsServicePlugin) apply false
    alias(libs.plugins.firebaseCrashlyticsPlugin) apply false
    alias(libs.plugins.firebasePerfPlugin) apply false
}
```

And in your app-level `build.gradle`:

```kotlin id="q3i0ha"
plugins {
    alias(libs.plugins.gmsServicePlugin)
    alias(libs.plugins.firebaseCrashlyticsPlugin)
    alias(libs.plugins.firebasePerfPlugin)
}
```

---

# Mediation

## Supported Mediation Networks

* Pangle
* Liftoff/Vungle
* Meta
* Mintegral
* Inmobi


# SDK Initialization

### Update your Manifest file, add this in your application tag

```kotlin
<meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="@string/your_app_id" />
```

Initialize the SDK in your `Application` class's `onCreate` method:

### Important Default Configs
These configs should be added to your `defaultRemoteConfigBuilder` in your App Class to control ad behavior in the plugin:
- `INTER_LOADING_ENABLE`- Enable/disable Interstitial & Reward ads loading dialog.
- `SPLASH_INTER_LOADING_ENABLE`- Enable/disable Splash Interstitial ads loading dialog. 
- `INTER_INSTANT_TIME`- Time window to show instant Interstitial ad
- `OPEN_AD_LOADING_ENABLE`- Enable/disable App-Open-Ad loading.
- `IS_OPEN_AD_INSTANT`- Enable/disable instant App-Open-Ad.
- `OPEN_AD_INSTANT_TIME`- Time window to show instant App-Open-Ad.
- `OPEN_AD_ENABLE`- Enable/disable App-Open-Ad completely.

```kotlin
AdKit.init(
    isDebug = BuildConfig.DEBUG,  
    context = this,
	appFlyerSdkKey = "", // If App-Flyer-Dev-key is provided, AppFlyer will post the events; otherwise, it won’t.
    openAdId = "ca-app-pub-3940256099942544/9257395921",
    mapOfInterIds = mapOf(
        "splash_inter" to "ca-app-pub-3940256099942544/1033173712",
        "home_inter" to "ca-app-pub-3940256099942544/1033173712", // If single, uses this ID; otherwise, rotates for this placement.
        "inter_common" to listOf(
            "ca-app-pub-3940256099942544/1033173712",
            "ca-app-pub-3940256099942544/1033173712",
            "ca-app-pub-3940256099942544/1033173712"
        ) // If single, uses this ID; otherwise, rotates for this placement.
    ),
    mapOfNativeIds = mapOf(
        "home_native" to "ca-app-pub-3940256099942544/2247696110"
    ),
    mapOfBannerIds = mapOf(
        "home_banner" to "ca-app-pub-3940256099942544/9214589741",
		"premium_banner" to "ca-app-pub-3940256099942544/2014213617",
    ),
	mapOfRewardIds = mapOf(
       "reward_id" to getString(com.plantcare.ai.plant.framework.R.string.chat_reward_id),
    ),
    defaultRemoteConfigBuilder = {

                bool("OPEN_AD_ENABLE", true)
                bool("IS_OPEN_AD_INSTANT", false)
                bool("INTER_LOADING_ENABLE", true)
				bool("SPLASH_INTER_LOADING_ENABLE", true)
                bool("OPEN_AD_LOADING_ENABLE", true)
                long("OPEN_AD_INSTANT_TIME", 8)
                long("INTER_INSTANT_TIME", 8)
				long("splash_time", 16)


                native("exit_native"){
                    enable(true)
                    ctaColor("")
                    bgColor("")
                    adType(NativeAdType.SMALL_NATIVE)
                }
                native("home_native"){
                    enable(true)
                    ctaColor("#FFFFFF")
                    adType(NativeAdType.SMALL_NATIVE_MEDIA_VIEW)
					refreshTime(7) // If provided, the native ad will refresh after 7 seconds
                }
                native("subscription_native"){
                    enable(true)
                    ctaColor("")
                    bgColor("")
                    adType(NativeAdType.SMALL_NATIVE)
                }

                fullScreen("splash_inter"){
                    enable(true)
                }
                fullScreen("home_inter"){
                    enable(true)
                    instantInter(true)
                }
                fullScreen("inter_btn_plant"){
                    enable(true)
                }
                fullScreen("inter_btn_plant"){
                    enable(true)
                    instantReward(true)
                }
                banner("home_banner"){
                      enable(true)
					  bannerType(BannerAdType.LARGE_ANCHORED_ADAPTIVE_BANNER)
                }
                banner("premium_banner"){
                    enable(true)
                   bannerType(BannerAdType.BOTTOM_COLLAPSIBLE_BANNER)
                }
                overAllNativeColor(ctaColor = "#964B00", bgColor = "#FF03DAC5")
            },
    onDefaultConfigGenerated = {  defaultConfigs ->
                Log.d("opoppp", "onDefaultConfigGenerated: $defaultConfigs")
            },
    onInitSdk = {
        // Optional: Disable toast notifications for analytics
        AdKit.analytics.showToast(false)
        // Optional: Enable or disable ads
        AdKit.initializer.disableAds(false)

        // Set custom native ad layouts (optional)
        AdKit.nativeCustomLayoutHelper.setNativeCustomLayouts(
            largeNativeLayout = R.layout.large_native_layout_custom,
            largeNativeShimmer = R.layout.large_native_layout_shimmer,
            // As per your requirement
			smallNativeLayout
			smallNativeShimmer
			smallNativeMiniLayout
			smallNativeMiniShimmer
			smallNativeMediaViewLayout
			smallNativeMediaViewShimmer
			fullScreenNativeLayout
			fullScreenNativeShimmer

        )

        // Exclude activities from showing open ads
        AdKit.openAdManager.excludeActivitiesFromOpenAd(MainActivity::class.java)

        // Exclude Compose routes from showing open ads
        AdKit.openAdManager.excludeNavigationRoutesFromOpenAd(
            AppRoute.SplashRoute::class.qualifiedName ?: "",
            AppRoute.FeedbackRoute::class.qualifiedName ?: "",
            AppRoute.PrivacyPolicy::class.qualifiedName ?: ""
        )

       // Exclude Nav Graph Xml fragments from showing open ads
		/*
			add label name available in nav_graph 
		*/
        AdKit.openAdManager.excludeNavigationRoutesFromOpenAd(
                     "fragment_splash",
                     "SettingsFragment",
		)
    }
)
```

---

# Consent Manager

Handle user consent for ads in your ViewModel:

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
                it.copy(isConsentManager = true)
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

---

# Locale Helper

Handle user app language with AdKit LocaleHelper, add this code in your activity, default language code is "en".

Keep Folder Name for specific languages
- **Chinese**
  - Simplified  
    - Folder name: `values-zh-rCN`  
    - Language code: `zh_rCN`
  - Traditional  
    - Folder name: `values-zh-rTW`  
    - Language code: `zh_rTW`

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
Change the application language when the user clicks the button

```kotlin
 Button(onClick = {
                AdKit.adKitPref.appLanguageCode = list[position].languageCode
                activity.startActivity(
                    Intent(activity, MainActivity::class.java)
                        .putExtra("languageChange", true)
                )
                activity.finish()
            }) {
                Text("change language")
            }
```

---


# Splash Ad

## Important Parameter `loadAndShow` 
- If `true`, the ad will load and show automatically. On ad failure or ad dismissed, `onAdClosed` is called.
- If `false`, only the ad loads. On success or failure, `onAdLoaded` is called — you can then show the navigation button.
- To show `AppOpenAd` on the Splash screen, add `${placementKey}_isAdOpenAd` to `true` in the `Default Remote Configs` within AppClass. By default, the Splash OpenAd is false.

```kotlin
private fun showSplashAd(mContext: Activity) {

        AdKit.splashAdController.initSplashInterstitial(
                placementKey = "splash_inter",
                adIdKey = "splash_inter",
                loadAndShow = false,
                activity = mContext,
				splashTime = firebaseLong("splash_time", 16),
                listener = object : InterstitialControllerListener {
                    override fun onAdShow() {
                        super.onAdShow()
                        isInterAdShowed = true
                        animator?.cancel()
                        viewModelScope.launch {
                            _state.update { it.copy(progress = 100) }
                        }
                    }
                    override fun onAdClosed(isInterShowed: Boolean, reason: String) {

						// it will be called if `loadAndShow` is true

                        animator?.cancel()
                        _state.update {
                            it.copy(progress = 100, moveToMain = true)
                        }
                    }
                    override fun onAdLoaded(reason: String) {
                        super.onAdLoaded()

						// it will be called if `loadAndShow` is false

                        _state.update {
                            it.copy(
                                onAdLoaded = true,
                            )
                        }
                    }
                }
            )
        
    }

/*
If loadAndShow is false, onAdLoaded will be called. Then use the provided function to show the splash ad.
Whether the ad is available or not, it will always trigger onAdClosed
*/


fun showSplashInterOnClick(activity: Activity){
        splashAdController.showInterstitial(
            activity = activity,
            object :InterstitialControllerListener{
                override fun onAdClosed(isInterShowed: Boolean, reason: String) {
                    sendOneTimeEvent(SplashOneTimeEventEvents.MoveToMain)
                }

            }
        )
    }

```

# Native Ads

### Ad Types
- `NativeAdType.LARGE_NATIVE`: Large native ad
- `NativeAdType.SMALL_NATIVE_MEDIA_VIEW`: Small native with media view ad
- `NativeAdType.SMALL_NATIVE`: Small native ad
- `NativeAdType.SMALL_NATIVE_MINI`: Small native mini ad
- `NativeAdType.FULL_NATIVE`: Full Screen native ad

### Remote Config Values
Add these to defaultRemoteConfigBuilder or Firebase Remoteconfigs:
- `{$placementkey}_isAdEnable`
- `{$placementkey}_nativeAdType`// add LARGE_NATIVE or SMALL_NATIVE_MEDIA_VIEW etc to change native type in remote config console
- `{$adIdKey}_loadNewAd`
- `{$placementkey}_ctaColor`
- `{$placementkey}_bgColor`
- `{$placementkey}_refreshTime`

For global native ad styling:
- `overAllNativeBgColor`
- `overAllNativeCtaColor`

### How to add in defaultRemoteConfigBuilder
in `App Class` -> defaultRemoteConfigBuilder, add

```kotlin
native("subscription_native"){
            enable(true)
            ctaColor("")
            bgColor("")
            adType(NativeAdType.SMALL_NATIVE)
			refreshTime(7)

     }

//optional 
overAllNativeColor(ctaColor = "#964B00", bgColor = "#FF03DAC5")
```

### Jetpack Compose Support

```kotlin
AdKitNativeAdView(
    nativeControllerConfig = NativeControllerConfig(
        placementKey = "home_native", // Unique placement key
        adIdKey = "home_native", // Can be common across placements,
		consumeAnyAd = true|false //If you want to use another placement’s ad if it’s available, pass true; otherwise, pass false.
		loadNextAd = false // if loadnew ad is true from remote config and  you do not want to load next ad then pass false
    ),
	//optional
    adCallBack =object: AdCallBack{
                override fun onAdFailed(reason: String) {
                    Log.d("dddddd", reason)
                }

                override fun onAdShow() {
                    
                }

                override fun onAdClick() {
                    Toast.makeText(activity, "home screen native ad click", Toast.LENGTH_SHORT).show()
                }
            },
   callCustomDestroy = { callCustomDestroy ->
				//handle Custom Destroy
                destroy = callCustomDestroy
    }
	)

// destroy?.invoke()

```

### XML Support

```xml
<io.monetize.kit.sdk.presentation.ui.native_ad.AdKitNativeAdViewXml
    android:id="@+id/adFrameNative"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="@dimen/_50sdp"
    app:layout_constraintTop_toBottomOf="@+id/btn_settings" />
```

Load the ad in your Activity or Fragment:

```kotlin
binding.adFrameNative.loadNative(
    this@MainXmlActivity,
	owner = this,
    nativeControllerConfig = NativeControllerConfig(
        placementKey = "home_native", // Unique placement key
        adIdKey = "home_native", // Can be common across placements,
		consumeAnyAd = true|false //If you want to use another placement’s ad if it’s available, pass true; otherwise, pass false.
		loadNextAd = false // if loadnew ad is true from remote config and  you do not want to load next ad then pass false
    ),
	//optional
    adCallBack =object: AdCallBack{
                override fun onAdFailed(reason: String) {
                    Log.d("dddddd", reason)
                }

                override fun onAdClick() {
                    Toast.makeText(activity, "home screen native ad click", Toast.LENGTH_SHORT).show()
                }
            }
)

//custom destroy native ad
binding.adFrameNative.destroyNativeAd()

```

### Custom Native Layouts

Set custom layouts in the `onInitSdk` callback:

```kotlin
AdKit.nativeCustomLayoutHelper.setNativeCustomLayouts(
    bigNativeLayout = R.layout.large_native_layout_custom,
    bigNativeShimmer = R.layout.large_native_layout_shimmer // Shows custom shimmer if provided
)

// Create a custom native ad layout using this code. Keep the view IDs the same."

<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/rl"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="@drawable/native_bg"
    android:layoutDirection="ltr">

    <io.monetize.kit.sdk.ads.native_ad.custom.SdkNativeAdView
        android:id="@+id/ad_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="@dimen/_5sdp">

        <RelativeLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content">

            <LinearLayout
                android:id="@+id/lll1"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:paddingTop="@dimen/_3sdp">

                <com.google.android.material.imageview.ShapeableImageView
                    android:id="@+id/ad_app_icon"
                    android:layout_width="@dimen/_40sdp"
                    android:layout_height="@dimen/_40sdp"
                    android:adjustViewBounds="true"
                    android:contentDescription="Image here"
                    android:paddingStart="0dp"
                    android:paddingEnd="5dp"
                    android:paddingBottom="5dp" />


                <LinearLayout
                    android:id="@+id/ll"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginStart="@dimen/_5sdp"
                    android:orientation="vertical">

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="horizontal">

                        <com.google.android.material.textview.MaterialTextView
                            android:id="@+id/tv_ad"
                            android:layout_width="@dimen/_20sdp"
                            android:layout_height="@dimen/_15sdp"
                            android:background="@drawable/border_ad"
                            android:gravity="center"
                            android:text="AD"
                            android:textColor="@color/black"
                            android:textSize="@dimen/_8ssp"
                            android:textStyle="bold" />

                        <com.google.android.material.textview.MaterialTextView
                            android:id="@+id/ad_headline"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:layout_marginStart="@dimen/_3sdp"
                            android:layout_marginEnd="@dimen/_5sdp"
                            android:maxLines="2"
                            android:textColor="@color/black"
                            android:textSize="@dimen/_12ssp"
                            android:textStyle="bold" />

                    </LinearLayout>

                    <com.google.android.material.textview.MaterialTextView
                        android:id="@+id/ad_body"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="@dimen/_2sdp"
                        android:layout_marginEnd="@dimen/_5sdp"
                        android:maxLines="2"
                        android:textColor="@color/black_light"
                        android:textSize="@dimen/_10ssp"
                        android:visibility="visible" />
                </LinearLayout>

            </LinearLayout>

            <io.monetize.kit.sdk.ads.native_ad.custom.SdkMediaView
                android:id="@+id/ad_media"
                android:layout_width="match_parent"
                android:layout_height="@dimen/_130sdp"
                android:layout_below="@+id/lll1"
                android:layout_centerHorizontal="true"
                android:layout_gravity="center_horizontal"
                android:layout_marginTop="3dp"
                android:layout_marginBottom="3dp"
                android:adjustViewBounds="true" />

            <androidx.appcompat.widget.AppCompatButton
                android:id="@+id/ad_call_to_action"
                android:layout_width="match_parent"
                android:layout_height="@dimen/_43sdp"
                android:layout_below="@+id/ad_media"
                android:layout_marginTop="@dimen/_2sdp"
                android:layout_marginBottom="@dimen/_3sdp"
                android:background="@color/black"
                android:gravity="center"
                android:padding="@dimen/_2sdp"
                android:textColor="@color/white"
                android:textSize="@dimen/_14ssp"
                app:backgroundTint="@null" />
        </RelativeLayout>
    </io.monetize.kit.sdk.ads.native_ad.custom.SdkNativeAdView>
</FrameLayout>



```

---

# Analytics Events

Post analytics events:

```kotlin
//post events
AdKit.analytics.postAnalytics("Main_idenify_plant_btn")

//post screen name
AdKit.analytics.postScreenName("splash", "splash")
```

Enable toast for events in debug mode in App Class:

```kotlin
onInitSdk = {
    AdKit.analytics.showToast(BuildConfig.DEBUG)
}
```

---

# App Open Ads

### Setup in Application Class

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
    } catch (_: Exception) {}
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

(Optional) To attach listeners of App Open Ad,  add it in App-Class in onInitSdk function:

```kotlin
     AdKit.openAdManager.setOpenAdListeners(object : OpenAdListener{
                    override fun onAdShow() {
                        Log.d("opoppp", "onAdShow: ")
                    }

                    override fun onAdLoaded() {
                        Log.d("opoppp", "onAdLoaded: ")
                    }

                    override fun onAdDismissed() {
                        Log.d("opoppp", "onAdDismissed: ")

                    }

                    override fun onAdFailed(error: String) {
                        Log.d("opoppp", "onAdFailed: $error")
                    }

                })

```

### Jetpack Compose Support

In onCreate in Main Activity

```kotlin
(appContext as AppClass).initializeAppClass()
```

For Jetpack Compose, manage open ads in onCreate in Main Activity:

```kotlin
// Set current Compose route
AdKit.openAdManager.setCurrentComposeRoute(SplashRoute::class.qualifiedName)

// Track navigation
val navController = rememberNavController()
val currentDestination by navController.currentBackStackEntryFlow.collectAsState(initial = null)
val currentRoute = currentDestination?.destination?.route
AdKit.openAdManager.setCurrentComposeRoute(currentRoute)
```

### NavGraph Xml Support
In onCreate in Main Activity

```kotlin
(appContext as AppClass).initializeAppClass()
```

For NavGraph Xml Support, manage open ads in onCreate in Main Activity:

```kotlin
// Set current Compose route
// this is the label you have added in nav_graph.xml for each fragment
AdKit.openAdManager.setCurrentNavigationRoute("SplashFragment")

// Track navigation
val navController = findNavController(R.id.my_nav_host_fragment)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            AdKit.openAdManager.setCurrentNavigationRoute(destination.label?.toString() ?: "")
        }

```

Exclude screens from open ads, add it in App-Class in onInitSdk function:

```kotlin
AdKit.openAdManager.excludeNavigationRoutesFromOpenAd(SplashRoute::class.qualifiedName ?: "") // compose
AdKit.openAdManager.excludeNavigationRoutesFromOpenAd("splash_fragment", "settings_fragment") // nav_graph xml
AdKit.openAdManager.excludeActivitiesFromOpenAd(MainActivity::class.java)

// Conditionally disable open ads
AdKit.openAdManager.canShowOpenAd(false|true)

// Handle App-Open-Ad behaviour in defaultRemoteConfigBuilder

 bool("OPEN_AD_ENABLE", true)
 bool("IS_OPEN_AD_INSTANT", false)
 long("INTER_INSTANT_TIME", 8)

```

---

# Banner Ads

### Ad Types
- `BannerAdType.ADAPTIVE_BANNER`: Adaptive Banner
- `BannerAdType.LARGE_BANNER`: Large Banner
- `BannerAdType.MEDIUM_RECTANGLE_BANNER`: Medium Rectangle Banner
- `BannerAdType.BOTTOM_COLLAPSIBLE_BANNER`: Bottom Collapsible Banner
- `BannerAdType.TOP_COLLAPSIBLE_BANNER`: Top Collapsible Banner
- `BannerAdType.LARGE_ANCHORED_ADAPTIVE_BANNER`: Large Anchored Adaptive Banner

### Remote Config Values
- `{$placementkey}_isAdEnable`
- `{$adIdKey}_loadNewAd`
- `{$placementkey}_bannerAdType`  // add ADAPTIVE_BANNER or LARGE_BANNER etc to change banner type in remote config console

### How to add in defaultRemoteConfigBuilder
in `App Class` -> defaultRemoteConfigBuilder, add

```kotlin
 banner("home_banner"){
       enable(true)
       bannerType(BannerAdType.BOTTOM_COLLAPSIBLE_BANNER)
}
```
### Jetpack Compose Support

```kotlin
AdKitBannerAdView(
    bannerControllerConfig = BannerControllerConfig(
        placementKey = "home_banner",
        adIdKey = "home_banner"
    ),
   //optional
    adCallBack =object: AdCallBack{
                override fun onAdFailed(reason: String) {
                    Log.d("dddddd", reason)
                }

                override fun onAdShow() {
                    
                }

                override fun onAdClick() {
                    Toast.makeText(activity, "home banner ad click", Toast.LENGTH_SHORT).show()
                }
            }
)
```

### XML Support

```xml
<io.monetize.kit.sdk.presentation.ui.banner.AdKitBannerAdViewXml
    android:id="@+id/adFrame"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:layout_constraintBottom_toBottomOf="parent" />
```

Load the banner ad:

```kotlin
binding.adFrame.loadBanner(
    this@MainXmlActivity,
	owner = this,
    bannerControllerConfig = BannerControllerConfig(
        placementKey = "home_native",
        adIdKey = "home_native"
    )
//optional
    adCallBack =object: AdCallBack{
                override fun onAdFailed(reason: String) {
                    Log.d("dddddd", reason)
                }

                override fun onAdShow() {
                    
                }

                override fun onAdClick() {
                    Toast.makeText(activity, "home banner ad click", Toast.LENGTH_SHORT).show()
                }
            }
)
```

---

# Interstitial Ads

### Remote Config Values
- `{$placementkey}_isAdEnable`
- `{$placementkey}_isInterInstant`

### How to add in defaultRemoteConfigBuilder
in `App Class` -> defaultRemoteConfigBuilder, add

```kotlin
 fullScreen("home_inter"){
         enable(true)
         instantInter(true)
   }
```

Show an interstitial ad:

```kotlin
AdKit.interHelper.showInterAd(
    adIdKey = "inter_common",
    placementKey = "inter_btn_plant",
    activity = activity,
    listener = object : InterstitialControllerListener {
        override fun onAdClosed(isInterShowed: Boolean, reason: String) {}
    },
    // Optional
    prefKey = "common_pref",
    counter = 2 // From remote configs
)
```

Preload an interstitial ad:

```kotlin
AdKit.interHelper.preLoadInter(
    activity = activity,
    adIdKey = "inter_common",
    placementKey = "inter_btn_plant",
    activity = activity,
    // Optional
    prefKey = "common_pref",
    counter = 2 // From remote configs
)
```

---


# Rewarded Ads

### Remote Config Values
- `{$placementkey}_isAdEnable`
- `{$placementkey}_isRewardInstant`

### How to add in defaultRemoteConfigBuilder
in `App Class` -> defaultRemoteConfigBuilder, add

```kotlin
 fullScreen("inter_btn_plant"){
       enable(true)
       instantReward(true)
   }
```

Show an interstitial ad:

```kotlin
AdKit.rewardHelper.showRewardAd(
    adIdKey = "inter_common",
    placementKey = "inter_btn_plant",
    activity = activity,
     listener = object : RewardedControllerListener {
                    override fun onRewardDismissed(isRewarded: Boolean, reason: String) {
                        if (isRewarded.not()) {
                            
                        }else{
                            
                        }
                    }

                },
    // Optional
    prefKey = "common_pref",
    counter = 2 // From remote configs
)
```

Preload an Rewarded ad:

```kotlin
AdKit.interHelper.preLoadRewardAd(
    activity = activity,
    adIdKey = "inter_common",
    placementKey = "inter_btn_plant",
    activity = activity,
    // Optional
    prefKey = "common_pref",
    counter = 2 // From remote configs
)
```

---

# In-App Update

### Jetpack Compose

```kotlin
val launcher = AdKitInAppUpdateFlowResultLauncher(onFail = {
    // Proceed with consent
})
```

### XML

```kotlin
private val updateLauncher = AdKitInAppUpdateManager.registerLauncher(this, onFail = {
    // Proceed with consent
})
```

Check for updates:

```kotlin
fun checkUpdate(context: Context, launcher: ActivityResultLauncher<IntentSenderRequest>) {
    AdKit.inAppUpdateManager.setUpdateStateCallback { updateState ->
        when (updateState) {
            UpdateState.Available -> {
                AdKit.inAppUpdateManager.startUpdateFlow(launcher)
            }
            UpdateState.Downloaded -> {
                // Show restart dialog or call
                AdKit.inAppUpdateManager.updateComplete()
            }
            UpdateState.Failed -> {
                // Proceed with consent
            }
            UpdateState.Idle -> {}
        }
    }
    AdKit.inAppUpdateManager.checkUpdate(context)
}

// Clean up in ViewModel or onDestroy
override fun onCleared() {
    super.onCleared()
    AdKit.inAppUpdateManager.unRegisterLister()
}
```

---

# In-App Review

```kotlin
 AdKit.inAppReviewManager.startReview(activity, object : ReviewListener {
                override fun onFail() {
					//contiue to app
                }

                override fun onComplete() {
					//contiue to app
                }

            })
```

---

# Firebase Remote Configs

Fetch remote config values:

```kotlin
AdKit.firebaseHelper.apply {
    viewModelScope.launch {
        configFetched.collectLatest {
            try {
				//examples
				val HOME_NATIVE_BANNER_AB = firebaseBoolean("HOME_NATIVE_BANNER_AB", false)
                val MAIN_NATIVE_TIME = firebaseLong("MAIN_NATIVE_TIME", 16L)
                val IS_AI = firebaseString("IS_AI", "YES")
                // Continue
            } catch (e: Exception) {
                // Continue
            }
        }
    }
    fetchRemoteValues(BuildConfig.DEBUG)
}
```

---

# Premium Billing

Use `AdKit.premiumHelper` as the single public billing entry point for lifetime purchases and subscriptions.

```kotlin
AdKit.premiumHelper.initBilling(activity,
            items = listOf(
                BillingItem.Lifetime("android.test.purchased", BillingItem.Type.REMOVE_ADS),
                BillingItem.Subscription(REMOVE_ADS_ID, BillingItem.Type.REMOVE_ADS),
                BillingItem.Subscription(FEATURE_1, BillingItem.Type.FEATURE),
                BillingItem.Subscription(FEATURE_2, BillingItem.Type.FEATURE),
                BillingItem.Subscription(FEATURE_3, BillingItem.Type.FEATURE),
            )
        )

AdKit.premiumHelper.premiumState.collectLatest { premiumState ->
    val isPremium = premiumState.isPremium
    val allPurchases = premiumState.allPurchases
}

val lifetimeOffer = AdKit.premiumHelper.getBillingPrice("android.test.purchased")
val subscriptionOffer = AdKit.premiumHelper.getBillingPrice(REMOVE_ADS_ID)

AdKit.premiumHelper.purchase(activity, "android.test.purchased")
AdKit.premiumHelper.purchase(activity, REMOVE_ADS_ID)
AdKit.premiumHelper.purchase(
    activity = activity,
    productId = REMOVE_ADS_ID,
    isForUpdatePlan = true
)

val isLifetimePurchased = AdKit.adKitPref.isLifeTimePurchased
val isSubscribed = AdKit.adKitPref.isAppSubscribed
val isPremium = AdKit.adKitPref.isAppPurchased
```

### ViewModel for Premium Billing

```kotlin
data class SettingScreenState(
    val removeAdsPrice: String = "",
    val feature1Price: String = "",
    val feature2Price: String = "",
    val feature3Price: String = "",
    val selectedButtonPos: Int = 0,
    val buttonText: String = "subscribe",
    val purchasesList: List<String> = emptyList()
)

class SubscriptionViewModel : ViewModel() {
    private var _state = MutableStateFlow(SettingScreenState())
    val state = _state.asStateFlow()

   private val subscriptionMap = mapOf(
        0 to REMOVE_ADS_ID,
        1 to FEATURE_1,
        2 to FEATURE_2,
        3 to FEATURE_3,
    )

    private fun selectedId() = subscriptionMap[state.value.selectedButtonPos]

    init {
        viewModelScope.apply {
            launch {


                AdKit.premiumHelper.premiumState.collectLatest { premiumState ->
                    Log.d(TAG, "purchasesList: ${premiumState.allPurchases}")

                    val removeAdsPrice = AdKit.premiumHelper.getBillingPrice(REMOVE_ADS_ID)
                    val feature1Price = AdKit.premiumHelper.getBillingPrice(FEATURE_1)
                    val feature2Price = AdKit.premiumHelper.getBillingPrice(FEATURE_2)
                    val feature3Price = AdKit.premiumHelper.getBillingPrice(FEATURE_3)


                    when (removeAdsPrice.type) {
                        OfferType.FREE_TRIAL -> {
                            Log.d(TAG, ": FREE_TRIAL")
                        }

                        OfferType.PAID_TRIAL -> {
                            Log.d(TAG, ": PAID_TRIAL")
                        }

                        OfferType.STRAIGHT -> {
                            Log.d(TAG, ": STRAIGHT")
                        }
                    }

                    Log.d(
                        TAG,
                        "mainOfferText=${feature1Price.mainOfferText} - period=${feature1Price.period} - freeTrialText=${feature1Price.freeTrialText} - paidTrialText=${feature1Price.paidTrialText}"
                    )
                    Log.d(
                        TAG,
                        "mainOfferText=${feature1Price.mainOfferText} - period=${feature1Price.period}- freeTrialText=${feature1Price.freeTrialText} - paidTrialText=${feature1Price.paidTrialText}"
                    )
                    Log.d(
                        TAG,
                        "mainOfferText=${feature2Price.mainOfferText} - period=${feature2Price.period}- freeTrialText=${feature2Price.freeTrialText} - paidTrialText=${feature2Price.paidTrialText}"
                    )
                    Log.d(
                        TAG,
                        "mainOfferText=${feature3Price.mainOfferText} - period=${feature3Price.period}- freeTrialText=${feature3Price.freeTrialText} - paidTrialText=${feature3Price.paidTrialText}"
                    )
                    _state.update {

                        it.copy(
                            purchasesList = premiumState.allPurchases,
                            removeAdsPrice = "${removeAdsPrice.mainOfferText}",
                            feature1Price = "${feature1Price.mainOfferText}",
                            feature2Price = "${feature2Price.mainOfferText}",
                            feature3Price = "${feature3Price.mainOfferText}",
                        )
                    }


                    changeButtonText()


                }
            }
        }
    }

    fun changeButtonText() {

        val selectedId = subscriptionMap[state.value.selectedButtonPos]
        val purchases = state.value.purchasesList

        val buttonText = when {
            purchases.isEmpty() -> "Subscribe"

            selectedId != null && purchases.contains(selectedId) ->
                "Cancel Subscription"

            purchases.isNotEmpty() &&
                    AdKit.premiumHelper.isSubscriptionUpdateSupported() ->
                "Update Subscription"

            else -> state.value.buttonText
        }

        _state.update {
            it.copy(buttonText = buttonText)
        }
    }

   fun loadProducts(
        activity: Activity,
    ) {
        AdKit.premiumHelper.initBilling(activity,
            items = listOf(
                BillingItem.Lifetime("android.test.purchased", BillingItem.Type.REMOVE_ADS),
                BillingItem.Subscription(REMOVE_ADS_ID, BillingItem.Type.REMOVE_ADS),
                BillingItem.Subscription(FEATURE_1, BillingItem.Type.FEATURE),
                BillingItem.Subscription(FEATURE_2, BillingItem.Type.FEATURE),
                BillingItem.Subscription(FEATURE_3, BillingItem.Type.FEATURE),
            )
        )
    }


    fun updateSelectedButtonPos(selectedButtonPos: Int) {
        _state.update {
            it.copy(
                selectedButtonPos = selectedButtonPos
            )
        }
        changeButtonText()
    }

    fun purchase(activity: Activity) {

		// importatnt parameter "isForUpdatePlan"
		// Pass true → update existing subscription
		// Pass false → start a new subscription

        AdKit.premiumHelper.purchase(activity, selectedId(), isForUpdatePlan = false, onUserDismissedPaywall = {
            Log.d(TAG, "subscription purchase: user dismissed the paywall")
        })
    }
}
```

### Implementing Premium Plans

```kotlin
LaunchedEffect(Unit) {
    subscriptionViewModel.loadProducts(
        activity,
    )
}

// Example: Weekly subscription button
Button(
    modifier = Modifier.fillMaxWidth(),
    onClick = { subscriptionViewModel.updateSelectedButtonPos(activity, 0) }
) {
    Text(text = "Weekly ${state.weeklyPrice}")
}

/* 

What happens when you click the Subscribe button:

The code checks your current subscription status and proceeds accordingly:

✅ Already subscribed → Goes to the Cancel Subscription screen
❌ Not subscribed → Proceeds to Subscribe
🔄 Already subscribed but a different plan is selected → Goes to Update Subscription screen

*/
Button(
    modifier = Modifier.fillMaxWidth(),
    onClick = { subscriptionViewModel.purchase(activity) }
) {
    Text(text = state.buttonText)
}
```

---

# Splash Screen ViewModel

### Compose 

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
	val onAdLoaded: Boolean = false,
    val progress: Int = 0
)


class SplashScreenViewModel(
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
        
    }

	fun loadProducts(activity:Activity){

		AdKit.premiumHelper.initBilling(activity,
            items = listOf(
                BillingItem.Lifetime("android.test.purchased", BillingItem.Type.REMOVE_ADS),
                BillingItem.Subscription(REMOVE_ADS_ID, BillingItem.Type.REMOVE_ADS),
                BillingItem.Subscription(FEATURE_1, BillingItem.Type.FEATURE),
                BillingItem.Subscription(FEATURE_2, BillingItem.Type.FEATURE),
                BillingItem.Subscription(FEATURE_3, BillingItem.Type.FEATURE),
            )
        )
	}

    private fun onResume() {
        if (state.value.runSplash) {
            animator?.resume()
        }
        viewModelScope.launch {
            _state.update { it.copy(isAppResumed = true) }
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
            _state.update { it.copy(isAppResumed = false) }
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
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            }
        })
    }

    fun checkForUpdate(activity: Activity, launcher: ActivityResultLauncher<IntentSenderRequest>) {
        inAppUpdateManager.setUpdateStateCallback { updateState ->
            when (updateState) {
                UpdateState.Available -> inAppUpdateManager.startUpdateFlow(launcher)
                UpdateState.Downloaded -> inAppUpdateManager.updateComplete()
                UpdateState.Failed -> initConsent(activity)
                UpdateState.Idle -> {}
            }
        }
        inAppUpdateManager.checkUpdate(activity)
    }

    private fun collections() {
        viewModelScope.apply {
            launch {
                consentManager.googleConsent.collectLatest { initializeSplash() }
            }
            launch {
                firebaseHelper.apply {
                    configFetched.collectLatest {
                        try {
//                            assignRemoteValues(this)
                            runSplash()
                        } catch (e: Exception) {
                            runSplash()
                        }
                    }
                }
            }
            launch {
                AdKit.premiumHelper.premiumState.collectLatest { premiumState ->
                    _state.update { it.copy(isPurchased = premiumState.isPremium) }
                }
            }
        }
    }

    fun initConsent(activity: Activity) {
        viewModelScope.launch {
            if (state.value.isConsentManager.not()) {
                _state.update { it.copy(isConsentManager = true) }
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
                _state.update { it.copy(initializeSplash = true) }
                fetchFirebase()
            }
        }
    }

    private fun fetchFirebase() {
        if (state.value.fireBaseFetch.not()) {
            _state.update { it.copy(fireBaseFetch = true) }
            firebaseHelper.fetchRemoteValues(isDebug = BuildConfig.DEBUG)
        }
    }

    private fun runSplash() {
        viewModelScope.launch {
            if (state.value.runSplash.not()) {
                _state.update { it.copy(runSplash = true) }
            }
        }
    }

    private fun startProgressAnimation() {
        animator?.cancel()
        animator = ValueAnimator.ofInt(0, 100).apply {
            duration = 25_000L
            addUpdateListener { animation ->
                val value = animation.animatedValue as? Int
                 _state.update { it.copy(progress = value ?: 50) }
            }
            start()
        }
    }

    fun initSplashAd(mContext: Activity) {
        if (!isInterAdCalled) {
            isInterAdCalled = true
            AdKit.splashAdController.initSplashInterstitial(
                placementKey = "splash_inter",
                adIdKey = "splash_inter",
                loadAndShow = false,
				splashTime = firebaseLong("splash_time", 16),
                activity = mContext,
                listener = object : InterstitialControllerListener {
                    override fun onAdShow() {
                        super.onAdShow()
                        isInterAdShowed = true
                        animator?.cancel()
                        viewModelScope.launch {
                            _state.update { it.copy(progress = 100) }
                        }
                    }
                    override fun onAdClosed(isInterShowed: Boolean, reason: String) {
                        animator?.cancel()
                        _state.update {
                            it.copy(progress = 100, moveToMain = true)
                        }
                    }
					override fun onAdLoaded(reason: String) {
                        super.onAdLoaded(reason)
                        _state.update {
                            it.copy(
                                onAdLoaded = true,
                            )
                        }
                    }
                }
            )
        }
    }

    fun resumeSplashAd(activity: Activity) {
        if (!isInterAdShowed && isInterAdCalled) {
            AdKit.splashAdController.resumeAd(activity)
        }
    }

    fun showSplashOnClick(activity: Activity){
        splashAdController.showInterstitial(
            activity = activity,
            object :InterstitialControllerListener{
                override fun onAdClosed(isInterShowed: Boolean, reason: String) {
                    _state.update {
                        it.copy(
                            moveToMain = true,
                        )
                    }
                }

            }
        )
    }

    override fun onCleared() {
        super.onCleared()
        inAppUpdateManager.unRegisterLister()
        animator?.cancel()
    }
}
```
### Splash Screen Implementation

```kotlin
val factory = remember { SplashScreenViewModelFactory() }
    val splashViewModel: SplashScreenViewModel = viewModel(factory = factory)
    val activity = LocalActivity.current as Activity
    val state by splashViewModel.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val launcher = AdKitInAppUpdateFlowResultLauncher(onFail = {
        splashViewModel.initConsent(activity)
    })

    LaunchedEffect(Unit) {
		splashViewModel.loadProducts(activity)
        splashViewModel.checkForUpdate(activity, launcher)
        splashViewModel.observeLifecycle(lifecycleOwner)
    }

    LaunchedEffect(key1 = state.runSplash) {
        if (state.runSplash) {
            splashViewModel.initSplashAd(activity)
        }
    }

    LaunchedEffect(key1 = state.moveToMain) {
        if (state.moveToMain) {
            moveToNext()
        }
    }

    LaunchedEffect(state.isAppResumed) {
        if (state.isAppResumed) {
            splashViewModel.resumeSplashAd(activity)
        }
    }

    SplashScreenContent(state = state, showAd = {
        splashViewModel.showSplashOnClick(activity)
    })
```

### XML 

```kotlin
data class SplashScreenState(
    val isConsentManager: Boolean = false,
    val initializeSplash: Boolean = false,
    val fireBaseFetch: Boolean = false,
    val showRestartDialog: Boolean = false,
    val moveToMain: Boolean = false,
    val isPurchased: Boolean = false,
    val runSplash: Boolean = false,
    val progress: Int = 0
)

class SplashViewModel(
) : ViewModel() {
    private var _state = MutableStateFlow(SplashScreenState())
    val state = _state.asStateFlow()
    private var isInterAdShowed = false
    private var isInterAdCalled = false
    private var animator: ValueAnimator? = null


    init {
        AdKit.analytics.postAnalytics("Splash_launch")
        splashAdController.resetSplash()
        collections()
        startProgressAnimation()

    }

	fun loadProducts(
        activity: Activity,
    ) {

        AdKit.premiumHelper.initBilling(activity,
            items = listOf(
                BillingItem.Lifetime("android.test.purchased", BillingItem.Type.REMOVE_ADS),
                BillingItem.Subscription(REMOVE_ADS_ID, BillingItem.Type.REMOVE_ADS),
                BillingItem.Subscription(FEATURE_1, BillingItem.Type.FEATURE),
                BillingItem.Subscription(FEATURE_2, BillingItem.Type.FEATURE),
                BillingItem.Subscription(FEATURE_3, BillingItem.Type.FEATURE),
            )
        )
    }

    fun onResume(activity: Activity) {
        if (state.value.runSplash) {
            animator?.resume()
        }
        if (!isInterAdShowed && isInterAdCalled) {
            splashAdController.resumeAd(activity)
        }
    }

    fun onPause() {
        if (state.value.runSplash) {
            animator?.pause()
        }
        if (!isInterAdShowed && isInterAdCalled) {
            splashAdController.pauseAd()
        }
    }

    fun checkForUpdate(activity: Activity, launcher: ActivityResultLauncher<IntentSenderRequest>) {
        inAppUpdateManager.setUpdateStateCallback { updateState ->
            when (updateState) {
                UpdateState.Available -> inAppUpdateManager.startUpdateFlow(launcher)
                UpdateState.Downloaded -> inAppUpdateManager.updateComplete()
                UpdateState.Failed -> initConsent(activity)
                UpdateState.Idle -> {}
            }
        }
        inAppUpdateManager.checkUpdate(activity)
    }

    private fun collections() {
        viewModelScope.apply {
            launch {
                consentManager.googleConsent.collectLatest { initializeSplash() }
            }
            launch {
                firebaseHelper.apply {
                    configFetched.collectLatest {
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
                AdKit.premiumHelper.premiumState.collectLatest { premiumState ->
                    _state.update { it.copy(isPurchased = premiumState.isPremium) }
                }
            }
        }
    }

    fun initConsent(activity: Activity) {
        viewModelScope.launch {
            if (state.value.isConsentManager.not()) {
                _state.update { it.copy(isConsentManager = true) }
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
                _state.update { it.copy(initializeSplash = true) }
                fetchFirebase()
            }
        }
    }

    private fun fetchFirebase() {
        if (state.value.fireBaseFetch.not()) {
            _state.update { it.copy(fireBaseFetch = true) }
            firebaseHelper.fetchRemoteValues(isDebug = BuildConfig.DEBUG)
        }
    }

    private fun runSplash() {
        viewModelScope.launch {
            if (state.value.runSplash.not()) {
                _state.update { it.copy(runSplash = true) }
            }
        }
    }

    private fun startProgressAnimation() {
        animator?.cancel()
        animator = ValueAnimator.ofInt(0, 100).apply {
            duration = 25_000L
            addUpdateListener { animation ->
                val value = animation.animatedValue as? Int
                 _state.update { it.copy(progress = value ?: 50) }
            }
            start()
        }
    }

    fun showSplashAd(mContext: Activity) {
        if (!isInterAdCalled) {
            isInterAdCalled = true
			
            splashAdController.initSplashInterstitial(
                placementKey = "splash_inter",
                adIdKey = "splash_inter",
                activity = mContext,
                splashTime = firebaseLong("splash_time", 16),
                listener = object : InterstitialControllerListener {
                    override fun onAdShow() {
                        super.onAdShow()
                        isInterAdShowed = true
                        animator?.cancel()
                        viewModelScope.launch {
                            _state.update { it.copy(progress = 100) }
                        }
                    }

                    override fun onAdClosed(isInterShowed: Boolean, reason: String) {
                        animator?.cancel()

                        _state.update {
                            it.copy(progress = 100, moveToMain = true)
                        }
                    }
                }
            )
        }
    }


    override fun onCleared() {
        super.onCleared()
        inAppUpdateManager.unRegisterLister()
        animator?.cancel()
    }
}

```

### Splash Activity Implementation

```kotlin
class SplashAppActivity : BaseActivity() {


    val viewModel: SplashViewModel by viewModel()

    private var isLaunched = false

    private val updateLauncher = AdKitInAppUpdateManager.registerLauncher(this, onFail = {
        viewModel.initConsent(this)
    })


    private val binding by lazy {
        ActivitySplashAppBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        viewModel.checkForUpdate(mContext, updateLauncher)

		viewModel.loadProducts(mContext)

        lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                when {
                    state.moveToMain -> {
                        if (isLaunched.not()) {
                            isLaunched = true
                            moveToNext()
                            finish()
                        }
                    }

                    state.runSplash -> {
                        Log.d("ioioio", "onCreate: runSplash")
                        viewModel.showSplashAd(mContext)
                    }
                }

            }
        }

    }

    fun moveToNext() {
        startActivity(Intent(mContext, OnBoardingActivity::class.java))
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume(mContext)
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }


    override fun onBackPresses() {

    }

}
```

---


This documentation provides a clean, organized, and visually appealing guide to using the Monetization Kit in your Android app. Each section is clearly separated, with consistent formatting and detailed explanations for seamless integration. Kotlin code blocks are now explicitly marked with triple backticks and the `kotlin` language identifier.
