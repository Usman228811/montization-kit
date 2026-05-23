package com.test.compose.adslibrary.xml

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.test.compose.adslibrary.databinding.ActivityMainXmlBinding
import com.test.compose.adslibrary.ui.base.BaseXmlActivity
import com.test.compose.adslibrary.ui.base.setupEdgeToEdge
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.presentation.viewmodels.NativeAdViewModel

class SettingsXmlActivity : BaseXmlActivity() {

    private lateinit var binding: ActivityMainXmlBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainXmlBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.setupEdgeToEdge()

        binding.btnSettings.isVisible = false

        binding.adFrame.loadBanner(
            this,
            this@SettingsXmlActivity,
            bannerControllerConfig = BannerControllerConfig(
                placementKey = "adaptive_banner",
                adIdKey = "adaptive_banner"
            )
        )
    }
}