package com.test.compose.adslibrary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.test.compose.adslibrary.databinding.ActivityMainXmlBinding

class MainXmlActivity : AppCompatActivity() {

//    private val bannerAdViewModel: BannerAdViewModel by inject()
//    private val nativeAdViewModel: NativeAdViewModel by inject()
    private lateinit var binding: ActivityMainXmlBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainXmlBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        binding.adFrameNative.loadNative(
//            this@MainXmlActivity,
//            nativeAdViewModel,
//            nativeControllerConfig = NativeControllerConfig(
//                key = "home_native",
//                adId = "ca-app-pub-3940256099942544/2247696110",
//                isAdEnable = true,
//                adType = 2
//            )
//        )
//
//        binding.adFrame.loadBanner(
//            this@MainXmlActivity,
//            bannerAdViewModel,
//            bannerControllerConfig = BannerControllerConfig(
//                key = "home_banner",
//                adId = "ca-app-pub-3940256099942544/9214589741",
//                isAdEnable = true
//            )
//        )

    }
}