package com.test.compose.adslibrary.xml

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.test.compose.adslibrary.AppClass
import com.test.compose.adslibrary.AppClass.Companion.appContext
import com.test.compose.adslibrary.databinding.ActivityMainXmlBinding
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.core.utils.callbacks.AdCallBack

class MainXmlActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainXmlBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainXmlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        (appContext as AppClass).initializeAppClass()


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

                override fun onAdClick() {
                }

            }
        )

    }
}