package io.monetize.kit.sdk.core.utils.in_app_update

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import io.monetize.kit.sdk.core.utils.AdSdkInternetController

sealed class UpdateState {
    data object Idle : UpdateState()
    data object Downloaded : UpdateState()
    data object Failed : UpdateState()
    data object Available : UpdateState()
}

class AdSdkInAppUpdateManager(private val context: Context, private val internetController: AdSdkInternetController) {

    private var appUpdateManager: AppUpdateManager? = null
    private var updateListener: InstallStateUpdatedListener? = null
    private var updateStateCallback: ((UpdateState) -> Unit)? = null
    private var appUpdateInfo: AppUpdateInfo? = null

    fun setUpdateStateCallback(callback: (UpdateState) -> Unit) {
        updateStateCallback = callback
    }

    fun checkUpdate() {
        if (internetController.isConnected) {
            try {
                appUpdateManager = AppUpdateManagerFactory.create(context)
                appUpdateManager?.let { appUpdateManager ->
                    appUpdateManager.appUpdateInfo.apply {
                        addOnSuccessListener { p0 ->
                            this@AdSdkInAppUpdateManager.appUpdateInfo = p0
                            if (p0 != null && p0.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && p0.isUpdateTypeAllowed(
                                    AppUpdateType.IMMEDIATE
                                )
                            ) {
                                updateStateCallback?.invoke(UpdateState.Available)
                            } else {
                                updateStateCallback?.invoke(UpdateState.Failed)
                            }
                        }
                        addOnCanceledListener {
                            updateStateCallback?.invoke(UpdateState.Failed)
                        }
                        addOnFailureListener { exception ->
                            updateStateCallback?.invoke(UpdateState.Failed)
                        }
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

        updateListener?.let {
            appUpdateManager?.registerListener(it)
        }
    }

    fun unRegisterLister() {
        updateListener?.let {
            appUpdateManager?.unregisterListener(it)
        }
        
        updateStateCallback = null
        updateListener = null
        appUpdateInfo = null
    }

    fun updateComplete(){
        appUpdateManager?.completeUpdate()
    }

    fun startUpdateFlow(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        appUpdateInfo?.let { info ->
            registerListener()
            appUpdateManager?.startUpdateFlowForResult(
                info,
                launcher,
                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
            )
        } ?: { updateStateCallback?.invoke(UpdateState.Failed) }
    }
}