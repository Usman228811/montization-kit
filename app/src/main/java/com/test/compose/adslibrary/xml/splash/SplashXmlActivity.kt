package com.test.compose.adslibrary.xml.splash

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
    private var isLaunched = false

    private val updateLauncher = AdKitInAppUpdateManager.registerLauncher(this, onFail = {
        splashXmlViewModel?.initConsent(this@SplashXmlActivity)
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_xml)

        splashXmlViewModel =
            ViewModelProvider(this, SplashXmlViewModelFactory())[SplashXmlViewModel::class]


        splashXmlViewModel?.let { viewModel ->

            viewModel.checkForUpdate(this@SplashXmlActivity, updateLauncher)

            lifecycleScope.launch {

                viewModel.state.collectLatest { state ->
                    when {
                        state.moveToMain -> {
                            if (isLaunched.not()) {
                                isLaunched = true
                                moveToNext()
                                finish()
                            }
                        }

                        state.runSplash -> {
                            Log.d("ioioio", "onCreate: runSplash")
                            viewModel.showSplashAd(this@SplashXmlActivity)
                        }
                    }

                }
            }
        }

    }

    fun moveToNext() {
        startActivity(Intent(this, MainXmlActivity::class.java))
    }

    override fun onResume() {
        super.onResume()
        splashXmlViewModel?.onResume(this@SplashXmlActivity)
    }

    override fun onPause() {
        super.onPause()
        splashXmlViewModel?.onPause()
    }


}