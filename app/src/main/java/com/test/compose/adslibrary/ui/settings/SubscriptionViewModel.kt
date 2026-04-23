package com.test.compose.adslibrary.ui.settings

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.test.compose.adslibrary.ui.main.FEATURE_1
import com.test.compose.adslibrary.ui.main.FEATURE_2
import com.test.compose.adslibrary.ui.main.FEATURE_3
import com.test.compose.adslibrary.ui.main.REMOVE_ADS_ID
import io.monetize.kit.sdk.core.utils.init.AdKit
import io.monetize.kit.sdk.domain.model.OfferType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class SettingScreenState(
    val removeAdsPrice: String = "",
    val feature1Price: String = "",
    val feature2Price: String = "",
    val feature3Price: String = "",
    val selectedButtonPos: Int = 0,
    val buttonText: String = "subscribe",
    val buttonTextLifeTime: String = "purchase one time",
    val oneTimePrice: String = "",
    val purchasesList: List<String> = emptyList()
)

class SubscriptionViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SubscriptionViewModel() as T
    }
}

class SubscriptionViewModel : ViewModel() {

    private var _state = MutableStateFlow(SettingScreenState())
    val state = _state.asStateFlow()

    companion object {
        const val TAG = "SubscriptionViewModelTAG"
    }

    private val subscriptionMap = mapOf(
        0 to REMOVE_ADS_ID,
        1 to FEATURE_1,
        2 to FEATURE_2,
        3 to FEATURE_3,
    )

    private fun selectedId() = subscriptionMap[state.value.selectedButtonPos]

    init {
        AdKit.purchaseHelper.initBilling(removeAdsIds = listOf("android.test.purchased"), listOf())
        viewModelScope.apply {
            launch {
                AdKit.purchaseHelper.oneTimePurchaseState.collectLatest { oneTimePurchaseState ->
                    Log.d(
                        TAG,
                        "oneTimePurchase: ${oneTimePurchaseState.purchasesList.isNotEmpty()}"
                    )
                    Log.d(
                        TAG,
                        "oneTimePurchaseList: ${oneTimePurchaseState.purchasesList}"
                    )
                    _state.update {
                        it.copy(
                            oneTimePrice = AdKit.purchaseHelper.getBillingPrice("android.test.purchased"),
                            buttonTextLifeTime = if (oneTimePurchaseState.purchasesList.isNotEmpty()) "purchased" else "purchase one time"
                        )
                    }
                }
            }
            launch {


                AdKit.subscriptionHelper.subscriptionState.collectLatest { subscriptionState ->
                    Log.d(TAG, "purchasesList: ${subscriptionState.purchasesList} ")


                    val removeAdsPrice = AdKit.subscriptionHelper.getBillingPrice(REMOVE_ADS_ID)
                    val feature1Price = AdKit.subscriptionHelper.getBillingPrice(FEATURE_1)
                    val feature2Price = AdKit.subscriptionHelper.getBillingPrice(FEATURE_2)
                    val feature3Price = AdKit.subscriptionHelper.getBillingPrice(FEATURE_3)


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
                            purchasesList = subscriptionState.purchasesList,
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
                    AdKit.subscriptionHelper.isSubscriptionUpdateSupported() ->
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
        AdKit.subscriptionHelper.initBilling(
            activity = activity,
            removeAdsIds = listOf(REMOVE_ADS_ID),
            featureIds = listOf(FEATURE_1, FEATURE_2, FEATURE_3)
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
        AdKit.subscriptionHelper.purchase(activity, selectedId(), false, onUserDismissedPaywall = {
            Log.d("usman", "subscription purchase: user dismissed the paywall")
        })
    }

    fun purchaseProduct(activity: Activity) {
        AdKit.purchaseHelper.purchaseProduct(
            activity,
            productId = "android.test.purchased",
            onUserDismissedPaywall = {
                Toast.makeText(
                    activity,
                    "one time purchase: user dismissed the paywall",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("usman", "one time purchase: user dismissed the paywall")

            })
    }
}