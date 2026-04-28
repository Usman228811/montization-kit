package com.test.compose.adslibrary.xml

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.test.compose.adslibrary.AppClass
import com.test.compose.adslibrary.AppClass.Companion.appContext
import com.test.compose.adslibrary.R
import com.test.compose.adslibrary.databinding.ActivityMainXmlBinding
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.core.utils.callbacks.AdCallBack
import androidx.core.graphics.drawable.toDrawable
import io.monetize.kit.sdk.presentation.ui.banner.AdKitBannerAdViewXml
import io.monetize.kit.sdk.presentation.ui.native_ad.AdKitNativeAdViewXml

class MainXmlActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainXmlBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainXmlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        (appContext as AppClass).initializeAppClass()

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBackPress()
                }
            })


        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this@MainXmlActivity, SettingsXmlActivity::class.java))
        }

        binding.adFrameNative.loadNative(
            this@MainXmlActivity,
            this,
            nativeControllerConfig = NativeControllerConfig(
                placementKey = "home_native",
                adIdKey = "home_native",
            ),
            adCallBack = object : AdCallBack {
                override fun onAdFailed(reason: String) {

                }

                override fun onAdShow() {

                }

                override fun onAdClick() {
                }

            }
        )

        binding.adFrame.loadBanner(
            this@MainXmlActivity,
            owner = this,
            bannerControllerConfig = BannerControllerConfig(
                placementKey = "home_banner",
                adIdKey = "home_banner",
            ), adCallBack = object : AdCallBack {
                override fun onAdFailed(reason: String) {

                }

                override fun onAdShow() {

                }

                override fun onAdClick() {
                }

            }
        )

    }

    fun onBackPress(){
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_custom, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        val btnOk = dialogView.findViewById<Button>(R.id.btnOk)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val native_ad = dialogView.findViewById<AdKitNativeAdViewXml>(R.id.native_ad)
        native_ad.loadNative(
            this@MainXmlActivity,
            this,
            nativeControllerConfig = NativeControllerConfig(
                "exit_native",
                "exit_native",
            ),

        )
        val banner_ad = dialogView.findViewById<AdKitBannerAdViewXml>(R.id.banner_ad)
        banner_ad.loadBanner(
            this@MainXmlActivity,
            this,
            bannerControllerConfig = BannerControllerConfig(
                placementKey = "exit_banner",
                adIdKey = "banner_common"
            ),

        )

        dialog.setOnDismissListener {
            native_ad.destroyNativeAd()
            banner_ad.destroyBannerAd()
        }


        btnOk.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}