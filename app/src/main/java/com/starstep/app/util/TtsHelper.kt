package com.starstep.app.util

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * 语音引导：中文播报当前步骤。
 * 不可用 / 静音时静默降级为屏幕文字提示，不影响推进（PRD 边界规则）。
 */
class TtsHelper(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var ready = false

    init {
        runCatching {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = tts?.setLanguage(Locale.CHINESE)
                    ready = result != TextToSpeech.LANG_MISSING_DATA &&
                            result != TextToSpeech.LANG_NOT_SUPPORTED
                }
            }
        }
    }

    fun speak(text: String) {
        if (!ready || text.isBlank()) return
        runCatching {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "starstep_utterance")
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        ready = false
    }
}
