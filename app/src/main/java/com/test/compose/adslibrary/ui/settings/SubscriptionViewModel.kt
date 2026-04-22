package com.test.compose.adslibrary.ui.settings

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.test.compose.adslibrary.ui.main.MONTHLY_SUB
import com.test.compose.adslibrary.ui.main.WEEKLY_SUB
import com.test.compose.adslibrary.ui.main.YEARLY_SUB
import io.monetize.kit.sdk.core.utils.init.AdKit
import io.monetize.kit.sdk.domain.model.OfferType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class SettingScreenState(
    val weeklyPrice: String = "",
    val monthlyPrice: String = "",
    val yearlyPrice: String = "",
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
        0 to WEEKLY_SUB,
        1 to MONTHLY_SUB,
        2 to YEARLY_SUB
    )

    private fun selectedId() = subscriptionMap[state.value.selectedButtonPos]

    init {
        AdKit.purchaseHelper.initBilling("android.test.purchased")
        viewModelScope.apply {
            launch {
                AdKit.purchaseHelper.oneTimePurchaseState.collectLatest { oneTimePurchaseState ->
                    Log.d(
                        TAG,
                        "oneTimePurchase: ${oneTimePurchaseState.purchasesList.isNotEmpty()}"
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


                    val weeklyOffer = AdKit.subscriptionHelper.getBillingPrice(WEEKLY_SUB)
                    val monthlyOffer = AdKit.subscriptionHelper.getBillingPrice(MONTHLY_SUB)
                    val yearlyOffer = AdKit.subscriptionHelper.getBillingPrice(YEARLY_SUB)

                    changeButtonText()

                    when (weeklyOffer.type) {
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
                        "mainOfferText=${weeklyOffer.mainOfferText} - period=${weeklyOffer.period} - freeTrialText=${weeklyOffer.freeTrialText} - paidTrialText=${weeklyOffer.paidTrialText}"
                    )
                    Log.d(
                        TAG,
                        "mainOfferText=${monthlyOffer.mainOfferText} - period=${monthlyOffer.period}- freeTrialText=${monthlyOffer.freeTrialText} - paidTrialText=${monthlyOffer.paidTrialText}"
                    )
                    Log.d(
                        TAG,
                        "mainOfferText=${yearlyOffer.mainOfferText} - period=${yearlyOffer.period}- freeTrialText=${yearlyOffer.freeTrialText} - paidTrialText=${weeklyOffer.paidTrialText}"
                    )
                    _state.update {

                        it.copy(
                            purchasesList = subscriptionState.purchasesList,
                            weeklyPrice = "${weeklyOffer.mainOfferText}",
                            monthlyPrice = "${monthlyOffer.mainOfferText}",
                            yearlyPrice = "${yearlyOffer.mainOfferText}",
                        )
                    }


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

    fun loadProducts(activity: Activity, list: List<String>) {
        AdKit.subscriptionHelper.initBilling(activity, list)
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
        AdKit.purchaseHelper.purchaseProduct(activity, onUserDismissedPaywall = {
            Toast.makeText(
                activity,
                "one time purchase: user dismissed the paywall",
                Toast.LENGTH_SHORT
            ).show()
            Log.d("usman", "one time purchase: user dismissed the paywall")

        })
    }
}