package com.test.compose.adslibrary.xml

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.test.compose.adslibrary.AppClass
import com.test.compose.adslibrary.AppClass.Companion.appContext
import com.test.compose.adslibrary.databinding.ActivityMainXmlBinding
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.presentation.viewmodels.BannerAdViewModel
import io.monetize.kit.sdk.presentation.viewmodels.BannerAdViewModelFactory
import io.monetize.kit.sdk.presentation.viewmodels.NativeAdViewModel
import io.monetize.kit.sdk.presentation.viewmodels.NativeAdViewModelFactory

class MainXmlActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainXmlBinding


    private var nativeAdViewModel: NativeAdViewModel? = null
    private var bannerAdViewModel: BannerAdViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainXmlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        (appContext as AppClass).initializeAppClass()



        nativeAdViewModel =
            ViewModelProvider(this, NativeAdViewModelFactory())[NativeAdViewModel::class.java]

        bannerAdViewModel =
            ViewModelProvider(this, BannerAdViewModelFactory())[BannerAdViewModel::class.java]


        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this@MainXmlActivity, SettingsXmlActivity::class.java))
        }

        binding.adFrameNative.loadNative(
            this@MainXmlActivity,
            nativeAdViewModel,
            nativeControllerConfig = NativeControllerConfig(
                key = "home_native",
                adId = "ca-app-pub-3940256099942544/2247696110",
                isAdEnable = true,
                adType = 2
            )
        )

        binding.adFrame.loadBanner(
            this@MainXmlActivity,
            bannerAdViewModel,
            bannerControllerConfig = BannerControllerConfig(
                key = "home_banner",
                adId = "ca-app-pub-3940256099942544/9214589741",
                isAdEnable = true
            )
        )

    }
}