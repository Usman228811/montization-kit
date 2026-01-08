package com.test.compose.adslibrary.ui.settings

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.monetize.kit.sdk.core.utils.init.AdKit
import io.monetize.kit.sdk.domain.usecase.PriceModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class SettingScreenState(
    val weeklyPrice: String = "",
    val monthlyPrice: String = "",
    val yearlyPrice: String = "",
    val subscribedId: String = "",
    val selectedButtonPos: Int = 0,
    val buttonText: String = "subscribe",
    val oneTimePrice: String = "",
)

class SubscriptionViewModelFactory(
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SubscriptionViewModel() as T
    }
}

class SubscriptionViewModel(
) : ViewModel() {

    private var _state = MutableStateFlow(SettingScreenState())
    val state = _state.asStateFlow()

    private val subscriptionMap = mapOf(
        0 to "weekly_without_free_trail",
        1 to "monthly",
        2 to "yearly"
    )

    private fun selectedId() = subscriptionMap[state.value.selectedButtonPos]

    init {
        AdKit.purchaseHelper.initBilling("android.test.purchased")
        viewModelScope.apply {
            launch {
                AdKit.purchaseHelper.productPriceFlow.collectLatest { model ->
                    _state.update {
                        it.copy(
                            oneTimePrice = model.price
                        )
                    }
                }
            }
            launch {
                AdKit.subscriptionHelper.subscriptionProducts.collectLatest {
                    _state.update {
                        val price = getBillingPrice(
                            "weekly_without_free_trail",
                            "paid-trail",
                            "P1W"
                        )

                        it.copy(
                            weeklyPrice = "${price.price} ${price.offerPrice}",
                            monthlyPrice = getBillingPrice("monthly", "", "P1M").price,
                            yearlyPrice = getBillingPrice("yearly", "", "P1Y").price,
                        )
                    }
                }
            }

            launch {
                AdKit.subscriptionHelper.isAppSubscribed.collectLatest { isSubscribed ->
                    Log.d("purchase_status", "isAppSubscribed: $isSubscribed")
                }
            }
            launch {
                AdKit.purchaseHelper.appPurchased.collectLatest { oneTimePurchased ->
                    Log.d("purchase_status", "oneTimePurchased: $oneTimePurchased")
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
        AdKit.subscriptionHelper.initBilling(activity, list)
    }


    private fun getBillingPrice(
        productId: String,
        offerId: String,
        billingPeriod: String
    ): PriceModel {
        return AdKit.subscriptionHelper.getBillingPrice(productId, offerId, billingPeriod)


    }

    fun updateSelectedButtonPos(activity: Activity, selectedButtonPos: Int) {
        _state.update {
            it.copy(
                selectedButtonPos = selectedButtonPos
            )
        }
        AdKit.subscriptionHelper.querySubscriptionProducts(activity)
    }

    fun purchase(activity: Activity) {
        AdKit.subscriptionHelper.purchase(activity, selectedId())
    }

    fun purchaseProduct(activity: Activity) {
        AdKit.purchaseHelper.purchaseProduct(activity)
    }
}