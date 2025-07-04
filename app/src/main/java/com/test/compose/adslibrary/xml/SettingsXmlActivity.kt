package com.test.compose.adslibrary.xml

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.test.compose.adslibrary.databinding.ActivityMainXmlBinding
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.presentation.viewmodels.NativeAdViewModel

class SettingsXmlActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainXmlBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainXmlBinding.inflate(layoutInflater)
        setContentView(binding.root)



    }
}