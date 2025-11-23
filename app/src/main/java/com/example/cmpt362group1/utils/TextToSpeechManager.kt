package com.example.cmpt362group1.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import java.util.Locale

class TextToSpeechManager(
    context: Context,
    onInitialized: (Boolean) -> Unit = {}
) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                isInitialized = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED

                if (isInitialized) useNetworkVoice()
                onInitialized(isInitialized)
            } else {
                onInitialized(false)
            }
        }
    }

    fun speak(text: String) {
        if (!isInitialized) return
        if (text.isEmpty()) return

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.shutdown()
    }

    fun isSpeaking(): Boolean {
        return tts?.isSpeaking?:false
    }

    private fun useNetworkVoice() {
        tts?.let { engine ->
            val voices = engine.voices

            val networkVoices = voices?.filter { voice ->
                !voice.isNetworkConnectionRequired ||
                        voice.quality == Voice.QUALITY_VERY_HIGH ||
                        voice.quality == Voice.QUALITY_HIGH
            }

            val bestVoice = networkVoices?.firstOrNull { voice ->
                voice.locale == Locale.US &&
                        !voice.isNetworkConnectionRequired
            } ?: networkVoices?.firstOrNull()
            ?: voices?.firstOrNull { it.locale == Locale.US }

            bestVoice?.let {
                engine.voice = it
                engine.setSpeechRate(1.0f)
                engine.setPitch(1.0f)
            }
        }
    }
}