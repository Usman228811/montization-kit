package com.test.compose.adslibrary.xml.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.test.compose.adslibrary.R
import com.test.compose.adslibrary.xml.MainXmlActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class SplashXmlActivity : AppCompatActivity() {

    private var splashXmlViewModel: SplashXmlViewModel? = null


    private fun initAdsConfigs() {
//        AdKit.initializer.initAdsConfigs(
//            AdsControllerConfig(
//                openAdId = "ca-app-pub-3940256099942544/9257395921",
//                splashId = "ca-app-pub-3940256099942544/1033173712",
//                appInterIds = listOf(
//                    "ca-app-pub-3940256099942544/1033173712",
//                    "ca-app-pub-3940256099942544/1033173712"
//                ),
//                splashInterEnable = true,
//                openAdEnable = true,
//                splashTime = 16L,
//                interLoadingEnable = true,
//                openAdLoadingEnable = true
//            )
//        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_xml)

        initAdsConfigs()

        splashXmlViewModel =
            ViewModelProvider(this, SplashXmlViewModelFactory())[SplashXmlViewModel::class]

        lifecycleScope.launch {

            splashXmlViewModel?.let {

                it.checkConsent(this@SplashXmlActivity)

                it.state.collectLatest { state ->
                    if (state.showSplashAd) {
                        it.showSplashAd(this@SplashXmlActivity)
                    }
                    if (state.moveNext) {
                        startActivity(Intent(this@SplashXmlActivity, MainXmlActivity::class.java))
                        finish()
                    }
                }
            }

        }

    }

    override fun onResume() {
        super.onResume()
        splashXmlViewModel?.resumeAd(this)
    }

    override fun onPause() {
        super.onPause()
        splashXmlViewModel?.pauseAd()
    }
}