package com.example.step_flow

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

private fun AppLanguage.toTagOrNull(): String? = when (this) {
    AppLanguage.System -> null
    AppLanguage.English -> "en"
    AppLanguage.Polish -> "pl"
}

fun applyAppLanguage(language: AppLanguage) {
    val tag = language.toTagOrNull()

    val locales = if (tag == null) {
        LocaleListCompat.getEmptyLocaleList() 
    } else {
        LocaleListCompat.forLanguageTags(tag)
    }

    
    val currentTags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
    val desiredTags = locales.toLanguageTags()
    if (currentTags != desiredTags) {
        AppCompatDelegate.setApplicationLocales(locales)
    }
}
