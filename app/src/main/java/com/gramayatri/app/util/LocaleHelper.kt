package com.gramayatri.app.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {
    fun wrapContext(context: Context): Context {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val languageCode = prefs.getString(
            Constants.KEY_LANGUAGE_CODE,
            Constants.LANGUAGE_ENGLISH
        ) ?: Constants.LANGUAGE_ENGLISH

        val locale = when (languageCode) {
            Constants.LANGUAGE_KANNADA -> Locale("kn", "IN")
            else -> Locale.ENGLISH
        }
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
        }
        return context.createConfigurationContext(configuration)
    }
}
