package com.example.smartagriculture

import android.content.Context
import org.robolectric.RuntimeEnvironment
import com.example.smartagriculture.audio.AudioDossierManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AudioDossierManagerTest {

    private lateinit var context: Context
    private lateinit var manager: AudioDossierManager

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        manager = AudioDossierManager(context)
    }

    @Test
    fun testBuildLocalizedScript_Healthy_English() {
        val script = manager.buildLocalizedScript(
            cropName = "Tomato",
            diseaseName = "Healthy",
            confidencePct = 98,
            organicRemedy = "None",
            languageCode = "en"
        )
        assertTrue(script.contains("Tomato"))
        assertTrue(script.contains("completely healthy"))
        assertTrue(script.contains("98 percent"))
    }

    @Test
    fun testBuildLocalizedScript_Diseased_Hindi() {
        val script = manager.buildLocalizedScript(
            cropName = "टमाटर",
            diseaseName = "अगेती झुलसा",
            confidencePct = 92,
            organicRemedy = "नीम का तेल छिड़कें",
            languageCode = "hi"
        )
        assertTrue(script.contains("निदान परिणाम"))
        assertTrue(script.contains("अगेती झुलसा"))
        assertTrue(script.contains("92 प्रतिशत"))
        assertTrue(script.contains("नीम का तेल छिड़कें"))
    }

    @Test
    fun testBuildLocalizedScript_Diseased_Marathi() {
        val script = manager.buildLocalizedScript(
            cropName = "टोमॅटो",
            diseaseName = "करपा",
            confidencePct = 89,
            organicRemedy = "सेंद्रिय कीटकनाशक वापरा",
            languageCode = "mr"
        )
        assertTrue(script.contains("निदान निकाल"))
        assertTrue(script.contains("करपा"))
        assertTrue(script.contains("89 टक्के"))
        assertTrue(script.contains("सेंद्रिय कीटकनाशक वापरा"))
    }

    @Test
    fun testBuildLocalizedScript_Healthy_Marathi() {
        val script = manager.buildLocalizedScript(
            cropName = "टोमॅटो",
            diseaseName = "Healthy",
            confidencePct = 95,
            organicRemedy = "None",
            languageCode = "mr"
        )
        assertTrue(script.contains("निरोगी आहे"))
        assertTrue(script.contains("95 टक्के"))
    }

    @Test
    fun testBuildLocalizedScript_Gujarati() {
        val script = manager.buildLocalizedScript(
            cropName = "કપાસ",
            diseaseName = "સુકારો",
            confidencePct = 90,
            organicRemedy = "લીંબોળીનું તેલ",
            languageCode = "gu"
        )
        assertTrue(script.contains("નિદાન પરિણામ"))
        assertTrue(script.contains("સુકારો"))
        assertTrue(script.contains("90 ટકા"))
    }

    @Test
    fun testBuildLocalizedScript_Telugu() {
        val script = manager.buildLocalizedScript(
            cropName = "వరి",
            diseaseName = "బ్లాస్ట్",
            confidencePct = 91,
            organicRemedy = "వేప నూనె",
            languageCode = "te"
        )
        assertTrue(script.contains("వ్యాధి నిర్ధారణ ఫలితం"))
        assertTrue(script.contains("బ్లాస్ట్"))
    }

    @Test
    fun testBuildLocalizedScript_Tamil() {
        val script = manager.buildLocalizedScript(
            cropName = "நெல்",
            diseaseName = "குலை நோய்",
            confidencePct = 93,
            organicRemedy = "வேப்ப எண்ணெய்",
            languageCode = "ta"
        )
        assertTrue(script.contains("நோய் கண்டறிதல் முடிவு"))
        assertTrue(script.contains("குலை நோய்"))
    }
}
