package com.test.compose.adslibrary.ui.settings

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.monetize.kit.sdk.core.utils.init.AdKit
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