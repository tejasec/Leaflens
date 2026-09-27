package com.example.smartagriculture.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

enum class TtsState {
    IDLE,
    SPEAKING,
    PAUSED
}

data class VoiceDiagnosisResult(
    val diseaseName: String,
    val severityGrade: String,
    val healthScore: Float,
    val organicTreatment: String
)

/**
 * Multilingual Voice Doctor Assistant Service (Feature 13).
 * Supports English (en-IN), Hindi (hi-IN), and Marathi (mr-IN) with play, pause,
 * and stop audio readout state management.
 */
class VoiceAssistantService(
    context: Context,
    private val onStateChanged: ((TtsState) -> Unit)? = null
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var currentState: TtsState = TtsState.IDLE
    private var isInitialized: Boolean = false
    private var currentLanguage: String = "en-IN"

    init {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                updateState(TtsState.SPEAKING)
            }

            override fun onDone(utteranceId: String?) {
                updateState(TtsState.IDLE)
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                updateState(TtsState.IDLE)
            }
        })
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            setLanguage(currentLanguage)
        } else {
            isInitialized = false
            updateState(TtsState.IDLE)
        }
    }

    /**
     * Sets current language locale: "en-IN" (English), "hi-IN" (Hindi), or "mr-IN" (Marathi).
     */
    fun setLanguage(languageCode: String): Boolean {
        this.currentLanguage = languageCode
        if (!isInitialized || tts == null) return false

        val locale = when (languageCode) {
            "hi-IN", "hi" -> Locale.Builder().setLanguage("hi").setRegion("IN").build()
            "mr-IN", "mr" -> Locale.Builder().setLanguage("mr").setRegion("IN").build()
            else -> Locale.Builder().setLanguage("en").setRegion("IN").build()
        }

        val result = tts?.setLanguage(locale)
        return result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
    }

    /**
     * Constructs localized audio diagnosis summary and speaks aloud.
     */
    fun speakDiagnosis(result: VoiceDiagnosisResult) {
        if (!isInitialized || tts == null) return

        val textToSpeak = when (currentLanguage) {
            "hi-IN", "hi" -> {
                "फसल निदान विवरण: ${result.diseaseName}. गंभीर स्तर: ${result.severityGrade}. " +
                        "स्वास्थ्य स्कोर: 100 में से ${result.healthScore.toInt()}. प्राथमिक जैविक उपचार: ${result.organicTreatment}."
            }
            "mr-IN", "mr" -> {
                "पिकाचे निदान सारांश: ${result.diseaseName}. तीव्रतेचा स्तर: ${result.severityGrade}. " +
                        "आरोग्य गुण: 100 पैकी ${result.healthScore.toInt()}. प्राथमिक सेंद्रिय उपचार: ${result.organicTreatment}."
            }
            else -> {
                "Diagnosis Summary: ${result.diseaseName}. Severity Grade: ${result.severityGrade}. " +
                        "Health Score: ${result.healthScore.toInt()} out of 100. Primary Organic Treatment: ${result.organicTreatment}."
            }
        }

        stop()
        val utteranceId = "utterance_${System.currentTimeMillis()}"
        tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    /**
     * Pauses or stops active speech readout.
     */
    fun pause() {
        if (currentState == TtsState.SPEAKING) {
            tts?.stop()
            updateState(TtsState.PAUSED)
        }
    }

    /**
     * Stops active speech playback and resets state to IDLE.
     */
    fun stop() {
        tts?.stop()
        updateState(TtsState.IDLE)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        updateState(TtsState.IDLE)
    }

    private fun updateState(newState: TtsState) {
        this.currentState = newState
        onStateChanged?.invoke(newState)
    }

    fun getCurrentState(): TtsState = currentState
}
