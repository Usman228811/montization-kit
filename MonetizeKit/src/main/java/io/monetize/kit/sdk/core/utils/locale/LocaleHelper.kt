package io.monetize.kit.sdk.core.utils.locale

import android.content.Context
import java.util.Locale


class LocaleHelper {
    companion object {
        @Volatile
        private var instance: LocaleHelper? = null

        internal fun getInstance(
        ): LocaleHelper {
            return instance ?: synchronized(this) {
                instance ?: LocaleHelper(
                ).also { instance = it }
            }
        }
    }


    fun setAppLanguage(context: Context?, languageCode: String): Context? {
        if (context == null) {
            return null
        }
        return updateResources(context, languageCode)
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = when (language) {
            "zh_rCN" -> {
                Locale.SIMPLIFIED_CHINESE
            }

            "zh_rTW" -> {
                Locale.TRADITIONAL_CHINESE
            }

            else -> {
                Locale(language)
            }
        }
        Locale.setDefault(locale)
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }
}
