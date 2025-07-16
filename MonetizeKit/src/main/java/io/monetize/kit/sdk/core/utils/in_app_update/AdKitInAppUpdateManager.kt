package io.monetize.kit.sdk.core.utils.in_app_update

import android.app.Activity
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import io.monetize.kit.sdk.core.utils.init.AdKit.internetController

sealed class UpdateState {
    data object Idle : UpdateState()
    data object Downloaded : UpdateState()
    data object Failed : UpdateState()
    data object Available : UpdateState()
}


class AdKitInAppUpdateManager private constructor() {

    companion object {
        @Volatile
        private var instance: AdKitInAppUpdateManager? = null

        fun getInstance(): AdKitInAppUpdateManager {
            return instance ?: synchronized(this) {
                instance ?: AdKitInAppUpdateManager().also { instance = it }
            }
        }

        /**
         * For XML-based activities: register the result launcher easily
         */
        fun registerLauncher(
            activity: ComponentActivity,
            onFail: () -> Unit
        ): ActivityResultLauncher<IntentSenderRequest> {
            return activity.registerForActivityResult(
                ActivityResultContracts.StartIntentSenderForResult()
            ) { result ->
                when (result.resultCode) {
                    Activity.RESULT_CANCELED,
                    ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> {
                        onFail()
                    }
                }
            }
        }
    }

    private var appUpdateManager: AppUpdateManager? = null
    private var updateListener: InstallStateUpdatedListener? = null
    private var updateStateCallback: ((UpdateState) -> Unit)? = null
    private var appUpdateInfo: AppUpdateInfo? = null

    fun setUpdateStateCallback(callback: (UpdateState) -> Unit) {
        updateStateCallback = callback
    }

    fun checkUpdate(activity: Context) {
        if (internetController.isConnected) {
            try {
                appUpdateManager = AppUpdateManagerFactory.create(activity)
                appUpdateManager?.appUpdateInfo?.apply {
                    addOnSuccessListener { info ->
                        appUpdateInfo = info
                        if (
                            info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                            info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                        ) {
                            updateStateCallback?.invoke(UpdateState.Available)
                        } else {
                            updateStateCallback?.invoke(UpdateState.Failed)
                        }
                    }
                    addOnCanceledListener {
                        updateStateCallback?.invoke(UpdateState.Failed)
                    }
                    addOnFailureListener {
                        updateStateCallback?.invoke(UpdateState.Failed)
                    }
                }
            } catch (_: Exception) {
                updateStateCallback?.invoke(UpdateState.Failed)
            }
        } else {
            updateStateCallback?.invoke(UpdateState.Failed)
        }
    }

    private fun registerListener() {
        updateListener = InstallStateUpdatedListener { installState ->
            if (installState.installStatus() == InstallStatus.DOWNLOADED) {
                updateStateCallback?.invoke(UpdateState.Downloaded)
            }
        }
        updateListener?.let { listener ->
            appUpdateManager?.registerListener(listener)
        }
    }

    fun unRegisterListener() {
        updateListener?.let {
            appUpdateManager?.unregisterListener(it)
        }
        updateStateCallback = null
        updateListener = null
        appUpdateInfo = null
    }

    fun updateComplete() {
        appUpdateManager?.completeUpdate()
    }

    fun startUpdateFlow(
        launcher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        appUpdateInfo?.let { info ->
            registerListener()
            appUpdateManager?.startUpdateFlowForResult(
                info,
                launcher,
                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
            )
        } ?: run {
            updateStateCallback?.invoke(UpdateState.Failed)
        }
    }
}
