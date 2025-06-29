package com.test.compose.adslibrary.ui.settings

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.koin.androidx.compose.koinViewModel

@Composable
fun SubscriptionScreen(
    subscriptionViewModel: SubscriptionViewModel = koinViewModel()
) {

    val activity = LocalActivity.current as Activity

    val state by subscriptionViewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {

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

        Button(modifier = Modifier.fillMaxWidth(),
            onClick = {
                subscriptionViewModel.updateSelectedButtonPos(0)

            }
        ) {
            Text(
                text = "weekly ${state.weeklyPrice}"
            )
        }
        Button(modifier = Modifier.fillMaxWidth(),
            onClick = {

                subscriptionViewModel.updateSelectedButtonPos(1)
            }
        ) {
            Text(
                text = "monthly ${state.monthlyPrice}"
            )
        }
        Button(modifier = Modifier.fillMaxWidth(),
            onClick = {

                subscriptionViewModel.updateSelectedButtonPos(2)
            }
        ) {
            Text(
                text = "yearly ${state.yearlyPrice}"
            )
        }
        Button(modifier = Modifier.fillMaxWidth(),
            onClick = {
                subscriptionViewModel.purchase()
            }
        ) {
            Text(
                text = state.buttonText
            )
        }
    }

}