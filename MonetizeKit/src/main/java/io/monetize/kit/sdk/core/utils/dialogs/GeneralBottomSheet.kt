package io.monetize.kit.sdk.core.utils.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import network.chaintech.sdpcomposemultiplatform.sdp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdSdkGeneralBottomSheet(
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    onDismissRequest: () -> Unit,
    titleComposable: (@Composable (() -> Unit))? = null,
    descriptionComposable: (@Composable (() -> Unit))? = null,
    positiveButtonComposable: (@Composable (() -> Unit))? = null,
    negativeButtonComposable: (@Composable (() -> Unit))? = null,
) {

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { onDismissRequest() },
    ) {
        BottomSheet(
            titleComposable = titleComposable,
            descriptionComposable = descriptionComposable,
            positiveButtonComposable = positiveButtonComposable,
            negativeButtonComposable = negativeButtonComposable,
        )
    }
}

@Composable
private fun BottomSheet(
    titleComposable: (@Composable (() -> Unit))? = null,
    descriptionComposable: (@Composable (() -> Unit))? = null,
    positiveButtonComposable: (@Composable (() -> Unit))? = null,
    negativeButtonComposable: (@Composable (() -> Unit))? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 13.sdp)
    ) {


        if (titleComposable != null) {
            titleComposable()
        }
        if (descriptionComposable != null) {
            Spacer(modifier = Modifier.height(height = 8.sdp))
            descriptionComposable()
        }
        Spacer(modifier = Modifier.height(height = 20.sdp))
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (negativeButtonComposable != null) {
                negativeButtonComposable()
            }

            if (positiveButtonComposable != null) {
                positiveButtonComposable()
            }
        }
    }
}