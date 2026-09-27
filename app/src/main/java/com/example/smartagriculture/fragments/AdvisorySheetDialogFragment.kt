package com.example.smartagriculture.fragments

import android.content.Intent
import android.os.Bundle
import androidx.core.net.toUri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.example.smartagriculture.R
import org.json.JSONObject
import java.io.InputStream
import java.util.Locale

/**
 * Dual-Track Advisory & Encyclopedia Redirection BottomSheet Dialog (Features 2, 3, & 4).
 * Displays Organic/Bio-control vs Chemical solutions, safety badges (PHI & REI),
 * and one-tap deep links to Wikipedia and extension portals.
 */
class AdvisorySheetDialogFragment : BottomSheetDialogFragment() {

    private var diseaseId: String = "early_blight"

    companion object {
        fun newInstance(diseaseId: String): AdvisorySheetDialogFragment {
            val fragment = AdvisorySheetDialogFragment()
            val args = Bundle().apply {
                putString("diseaseId", diseaseId)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        diseaseId = arguments?.getString("diseaseId") ?: "early_blight"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_advisory_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTitle = view.findViewById<TextView>(R.id.tvAdvisoryTitle)
        val tabOrganic = view.findViewById<Button>(R.id.tabOrganic)
        val tabChemical = view.findViewById<Button>(R.id.tabChemical)
        val tvContent = view.findViewById<TextView>(R.id.tvAdvisoryContent)
        val chipPhi = view.findViewById<Chip>(R.id.chipPhi)
        val chipRei = view.findViewById<Chip>(R.id.chipRei)
        val btnWikipedia = view.findViewById<Button>(R.id.btnWikipedia)
        val btnExtensionPortal = view.findViewById<Button>(R.id.btnExtensionPortal)

        val advisoryData = loadRemediesJson(diseaseId)

        if (advisoryData != null) {
            val diseaseName = advisoryData.optString("disease_name", "Crop Disease Advisory")
            tvTitle?.text = diseaseName

            val organicObj = advisoryData.optJSONObject("organic_solution")
            val chemicalObj = advisoryData.optJSONObject("chemical_solution")
            val externalObj = advisoryData.optJSONObject("external_resources")

            val organicText = buildString {
                append("🌱 ${organicObj?.optString("title", "Organic Solution")}\n\n")
                append("• Treatment: ${organicObj?.optString("treatment")}\n")
                append("• Instructions: ${organicObj?.optString("instructions")}\n")
                append("• Benefits: ${organicObj?.optString("benefits")}\n")
            }

            val chemicalText = buildString {
                append("🧪 ${chemicalObj?.optString("title", "Chemical Solution")}\n\n")
                append("• Active Ingredient: ${chemicalObj?.optString("active_ingredient")}\n")
                append("• Dosage per Liter: ${chemicalObj?.optString("dosage_per_liter")}\n")
                append("• Instructions: ${chemicalObj?.optString("instructions")}\n")
            }

            // Default: Show Organic Solution
            tvContent?.text = organicText
            chipPhi?.visibility = View.GONE
            chipRei?.visibility = View.GONE

            tabOrganic?.setOnClickListener {
                tvContent?.text = organicText
                chipPhi?.visibility = View.GONE
                chipRei?.visibility = View.GONE
            }

            tabChemical?.setOnClickListener {
                tvContent?.text = chemicalText
                val phiDays = chemicalObj?.optInt("phi_days", 7) ?: 7
                val reiHours = chemicalObj?.optInt("rei_hours", 24) ?: 24

                chipPhi?.text = String.format(Locale.US, "PHI: %d Days", phiDays)
                chipRei?.text = String.format(Locale.US, "REI: %d Hours", reiHours)
                chipPhi?.visibility = View.VISIBLE
                chipRei?.visibility = View.VISIBLE
            }

            val wikiUrl = externalObj?.optString("wikipedia_url")
            val extensionUrl = externalObj?.optString("extension_portal_url")

            btnWikipedia?.setOnClickListener {
                if (!wikiUrl.isNullOrBlank()) {
                    openWebPage(wikiUrl)
                }
            }

            btnExtensionPortal?.setOnClickListener {
                if (!extensionUrl.isNullOrBlank()) {
                    openWebPage(extensionUrl)
                }
            }
        }
    }

    private fun loadRemediesJson(targetDiseaseId: String): JSONObject? {
        return try {
            val inputStream: InputStream = requireContext().assets.open("data/remedies.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(jsonString)
            val diseases = root.optJSONArray("diseases") ?: return null

            for (i in 0 until diseases.length()) {
                val diseaseObj = diseases.getJSONObject(i)
                if (diseaseObj.optString("id") == targetDiseaseId) {
                    return diseaseObj
                }
            }
            if (diseases.length() > 0) diseases.getJSONObject(0) else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun openWebPage(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(intent)
    }
}
