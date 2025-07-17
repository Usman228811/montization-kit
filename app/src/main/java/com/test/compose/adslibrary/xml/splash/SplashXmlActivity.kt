package com.test.compose.adslibrary.xml.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.test.compose.adslibrary.R
import com.test.compose.adslibrary.xml.MainXmlActivity
import io.monetize.kit.sdk.core.utils.in_app_update.AdKitInAppUpdateManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class SplashXmlActivity : AppCompatActivity() {

    private var splashXmlViewModel: SplashXmlViewModel? = null

    private lateinit var updateLauncher: ActivityResultLauncher<IntentSenderRequest>




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_xml)

        updateLauncher = AdKitInAppUpdateManager.registerLauncher(this) {
            //
        }


        splashXmlViewModel =
            ViewModelProvider(this, SplashXmlViewModelFactory())[SplashXmlViewModel::class]


//        splashXmlViewModel?.checkUpdate(this@SplashXmlActivity, updateLauncher)


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