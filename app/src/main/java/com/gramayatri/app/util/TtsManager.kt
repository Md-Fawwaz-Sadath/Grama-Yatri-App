package com.gramayatri.app.util

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TtsManager(
    context: Context,
    private val languageCodeProvider: () -> String
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isReady = false

    override fun onInit(status: Int) {
        isReady = status == TextToSpeech.SUCCESS
        if (isReady) {
            tts?.setSpeechRate(0.95f)
            tts?.setPitch(1.0f)
        }
    }

    fun speak(text: String): SpeakResult {
        if (!isReady) return SpeakResult.NOT_READY

        val locale = currentLocale()
        val languageResult = tts?.setLanguage(locale)
        if (
            languageResult == TextToSpeech.LANG_MISSING_DATA ||
            languageResult == TextToSpeech.LANG_NOT_SUPPORTED
        ) {
            return SpeakResult.LANGUAGE_UNAVAILABLE
        }

        tts?.setSpeechRate(0.95f)
        tts?.setPitch(1.0f)
        val utteranceId = "gramayatri-${System.currentTimeMillis()}"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        return SpeakResult.SPOKEN
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }

    private fun currentLocale(): Locale =
        when (languageCodeProvider()) {
            Constants.LANGUAGE_KANNADA -> Locale("kn", "IN")
            else -> Locale.ENGLISH
        }

    enum class SpeakResult {
        SPOKEN,
        NOT_READY,
        LANGUAGE_UNAVAILABLE
    }
}
