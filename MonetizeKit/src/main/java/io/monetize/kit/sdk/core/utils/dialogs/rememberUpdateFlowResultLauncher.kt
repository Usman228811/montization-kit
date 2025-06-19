package io.monetize.kit.sdk.core.utils.dialogs

import android.app.Activity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import com.google.android.play.core.install.model.ActivityResult

@Composable
fun AdSdkInAppUpdateFlowResultLauncher(
    onFail: () -> Unit,
): ManagedActivityResultLauncher<IntentSenderRequest, androidx.activity.result.ActivityResult> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_CANCELED,
            ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> {
                onFail()
            }
        }
    }
}
