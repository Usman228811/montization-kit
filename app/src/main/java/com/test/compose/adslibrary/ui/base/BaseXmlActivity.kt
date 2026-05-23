package com.test.compose.adslibrary.ui.base

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import io.monetize.kit.sdk.core.utils.init.AdKit


fun View?.setupEdgeToEdge(imePadding: Boolean = false) {
    this?.let { root ->
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val isImeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())

            view.updatePadding(
                left = navigationBars.left,
                top = statusBars.top,
                right = navigationBars.right,
                bottom = if (imePadding && isImeVisible) ime.bottom else navigationBars.bottom
            )

            insets
        }

        ViewCompat.requestApplyInsets(root)
    }
}

abstract class BaseXmlActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (screenName != "splash" && AdKit.consentManager.canRequestAds.not() && AdKit.internetController.isConnected) {
            Log.d("consent_check", "onCreate: consent_check")
            AdKit.consentManager.gatherConsent(this)
        }
    }

    private var screenName = ""

    fun setScreenName(screenName: String) {
        this.screenName = screenName
    }
}