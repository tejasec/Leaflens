package com.example.smartagriculture.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class AudioState { IDLE, PLAYING, ERROR }

class AudioDossierManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)

    private val _playbackState = MutableStateFlow(AudioState.IDLE)
    val playbackState: StateFlow<AudioState> = _playbackState.asStateFlow()

    private var isInitialized = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _playbackState.value = AudioState.PLAYING
                }

                override fun onDone(utteranceId: String?) {
                    _playbackState.value = AudioState.IDLE
                }

                override fun onError(utteranceId: String?) {
                    _playbackState.value = AudioState.ERROR
                }
            })
            isInitialized = true
        } else {
            Log.e("AudioDossierManager", "TextToSpeech Initialization Failed")
            _playbackState.value = AudioState.ERROR
        }
    }

    /**
     * Speaks the diagnostic dossier in the target language ("en", "hi", "mr", "gu", "te", "ta").
     */
    fun speakDossier(
        cropName: String,
        diseaseName: String,
        confidencePct: Int,
        organicRemedy: String,
        languageCode: String = "hi"
    ) {
        if (!isInitialized) return

        val targetLocale = when (languageCode.lowercase()) {
            "hi" -> Locale("hi", "IN")
            "mr" -> Locale("mr", "IN")
            "gu" -> Locale("gu", "IN")
            "te" -> Locale("te", "IN")
            "ta" -> Locale("ta", "IN")
            else -> Locale("en", "IN")
        }

        // Verify TTS engine language support with English fallback
        val result = tts?.setLanguage(targetLocale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.w("AudioDossierManager", "Locale $targetLocale not installed on device. Falling back to English.")
            tts?.language = Locale("en", "IN")
        }

        val speechText = buildLocalizedScript(
            cropName = cropName,
            diseaseName = diseaseName,
            confidencePct = confidencePct,
            organicRemedy = organicRemedy,
            languageCode = languageCode
        )

        tts?.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, "LeafLensAudioID")
    }

    fun buildLocalizedScript(
        cropName: String,
        diseaseName: String,
        confidencePct: Int,
        organicRemedy: String,
        languageCode: String
    ): String {
        val isHealthy = diseaseName.equals("Healthy", ignoreCase = true) ||
                diseaseName.contains("healthy", ignoreCase = true)

        return when (languageCode.lowercase()) {
            "hi" -> {
                if (isHealthy) {
                    "निदान परिणाम: आपकी $cropName की फसल पूरी तरह से स्वस्थ है। सटीकता $confidencePct प्रतिशत है।"
                } else {
                    "निदान परिणाम: $cropName की फसल पर $diseaseName की पहचान हुई है। सटीकता $confidencePct प्रतिशत है। मुख्य जैविक उपचार: $organicRemedy"
                }
            }
            "mr" -> {
                if (isHealthy) {
                    "निदान निकाल: तुमचे $cropName पीक पूर्णपणे निरोगी आहे. अचूकता $confidencePct टक्के आहे।"
                } else {
                    "निदान निकाल: $cropName पिकावर $diseaseName आढळले आहे. अचूकता $confidencePct टक्के आहे. मुख्य सेंद्रिय उपाय: $organicRemedy"
                }
            }
            "gu" -> {
                if (isHealthy) {
                    "નિદાન પરિણામ: તમારો $cropName પાક સંપૂર્ણપણે સ્વસ્થ છે. ચોકસાઈ $confidencePct ટકા છે."
                } else {
                    "નિદાન પરિણામ: $cropName પાક પર $diseaseName ની ઓળખ થઈ છે. ચોકસાઈ $confidencePct ટકા છે. મુખ્ય જૈવિક ઉપચાર: $organicRemedy"
                }
            }
            "te" -> {
                if (isHealthy) {
                    "వ్యాధి నిర్ధారణ ఫలితం: మీ $cropName పంట పూర్తిగా ఆరోగ్యకరంగా ఉంది. ఖచ్చితత్వం $confidencePct శాతం."
                } else {
                    "వ్యాధి నిర్ధారణ ఫలితం: $cropName పంటపై $diseaseName గుర్తించబడింది. ఖచ్చితత్వం $confidencePct శాతం. ప్రధాన సేంద్రీయ నివారణ: $organicRemedy"
                }
            }
            "ta" -> {
                if (isHealthy) {
                    "நோய் கண்டறிதல் முடிவு: உங்கள் $cropName பயிர் முற்றிலும் ஆரோக்கியமாக உள்ளது. துல்லியம் $confidencePct சதவீதம்."
                } else {
                    "நோய் கண்டறிதல் முடிவு: $cropName பயிரில் $diseaseName கண்டறியப்பட்டுள்ளது. துல்லியம் $confidencePct சதவீதம். முதன்மை இயற்கை தீர்வு: $organicRemedy"
                }
            }
            else -> {
                if (isHealthy) {
                    "Diagnosis Result: Your $cropName crop is completely healthy with $confidencePct percent confidence."
                } else {
                    "Diagnosis Result: Detected $diseaseName on $cropName with $confidencePct percent confidence. Primary organic treatment: $organicRemedy"
                }
            }
        }
    }

    fun stopAudio() {
        if (tts?.isSpeaking == true) {
            tts?.stop()
        }
        _playbackState.value = AudioState.IDLE
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
