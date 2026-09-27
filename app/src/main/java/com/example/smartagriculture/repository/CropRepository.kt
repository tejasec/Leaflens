package com.example.smartagriculture.repository

import com.example.smartagriculture.R
import com.example.smartagriculture.model.CropDetailItem
import com.example.smartagriculture.model.CropDiseaseItem
import com.example.smartagriculture.model.CropTimelineStep
import com.example.smartagriculture.model.MyCropItem

object CropRepository {

    private val diseaseEarlyBlight = CropDiseaseItem(
        id = 101,
        diseaseName = "Early Blight",
        scientificName = "Alternaria solani",
        description = "Early blight is a common fungal disease that affects tomato plants. It causes dark, concentric spots on older leaves, which may lead to premature leaf drop if untreated.",
        symptoms = listOf(
            "Dark brown spots with concentric rings on leaves",
            "Yellowing around leaf spots",
            "Affected leaves dry up and drop prematurely"
        ),
        organicCare = "• Remove affected leaves and dispose of them properly\n• Apply neem oil spray (5 ml per litre of water)\n• Use compost to improve soil health\n• Maintain proper plant spacing for good air circulation\n• Use resistant varieties (where available)",
        chemicalCare = "• Apply copper-based fungicide at initial symptom appearance\n• Spray Chlorothalonil or Mancozeb at 7-10 day intervals\n• Rotate fungicide classes to prevent resistance",
        prevention = "• Practice crop rotation with non-solanaceous crops\n• Mulch around plant base to prevent soil splash\n• Avoid overhead watering; use drip irrigation",
        similarDiseases = listOf("Late Blight", "Septoria Leaf Spot")
    )

    private val diseaseLateBlight = CropDiseaseItem(
        id = 102,
        diseaseName = "Late Blight",
        scientificName = "Phytophthora infestans",
        description = "Late blight is a destructive water-mold disease causing large, dark, water-soaked leaf lesions during cool, wet conditions.",
        symptoms = listOf(
            "Irregular water-soaked spots on leaf edges",
            "White fungal growth under leaf surface in moist weather",
            "Dark lesions on stems and green fruit"
        ),
        organicCare = "• Remove and destroy infected plants immediately\n• Spray copper sulfate solution early in morning\n• Ensure maximum sunlight and aeration",
        chemicalCare = "• Apply systemic fungicides like Metalaxyl or Cymoxanil\n• Maintain protective spray schedule during rainy periods",
        prevention = "• Use certified disease-free seeds and transplants\n• Destroy volunteer tomato/potato plants",
        similarDiseases = listOf("Early Blight", "Bacterial Spot")
    )

    private val diseaseBacterialSpot = CropDiseaseItem(
        id = 103,
        diseaseName = "Bacterial Spot",
        scientificName = "Xanthomonas spp.",
        description = "Bacterial spot causes small dark spots with yellow halos on leaves and raised scabs on fruit.",
        symptoms = listOf(
            "Small dark spots with yellow halos",
            "Blistered, scabby lesions on young fruit",
            "Yellowing and dropping of leaves"
        ),
        organicCare = "• Spray copper hydroxide mixed with mancozeb\n• Remove infected lower leaves",
        chemicalCare = "• Apply streptomycin sulfate or copper bactericides",
        prevention = "• Treat seeds with hot water before planting\n• Avoid handling plants when foliage is wet",
        similarDiseases = listOf("Early Blight", "Septoria Leaf Spot")
    )

    private val diseaseLeafCurl = CropDiseaseItem(
        id = 104,
        diseaseName = "Leaf Curl (Viral)",
        scientificName = "Tomato leaf curl virus",
        description = "Transmitted by whiteflies, causing severe leaf curling, stunting, and reduced fruit yield.",
        symptoms = listOf(
            "Leaves curl upwards and turn yellow",
            "Stunted plant growth and small leaves",
            "Flower drop and poor fruit setting"
        ),
        organicCare = "• Control whitefly vector using yellow sticky traps\n• Spray neem seed kernel extract (NSKE 5%)",
        chemicalCare = "• Spray Imidacloprid or Thiamethoxam to control whiteflies",
        prevention = "• Grow seedling under insect-proof net mesh",
        similarDiseases = listOf("Bacterial Spot")
    )

    private val diseaseSeptoria = CropDiseaseItem(
        id = 105,
        diseaseName = "Septoria Leaf Spot",
        scientificName = "Septoria lycopersici",
        description = "Fungal infection causing small round spots with grey centers and dark borders.",
        symptoms = listOf(
            "Small round spots with grey centers",
            "Tiny black specks (fruiting bodies) inside spots",
            "Progressive defoliation from bottom up"
        ),
        organicCare = "• Remove lower infected leaves\n• Spray copper octanoate fungicide",
        chemicalCare = "• Apply Chlorothalonil or Azoxystrobin",
        prevention = "• Maintain 3-year crop rotation schedule",
        similarDiseases = listOf("Early Blight")
    )

    val allCropsList = listOf(
        CropDetailItem(
            id = 1,
            name = "Tomato",
            scientificName = "Solanum lycopersicum",
            category = "Vegetables",
            growingSeason = "Mar – Jun",
            idealTemp = "20 – 30°C",
            soilType = "Loamy soil",
            waterRequirement = "Moderate",
            about = "Tomato is a widely grown vegetable crop, rich in vitamins and minerals. It is susceptible to various bacterial, fungal and viral diseases.",
            proTip = "Healthy crops lead to better yield and higher income!",
            imageRes = R.drawable.bg_1,
            diseases = listOf(diseaseEarlyBlight, diseaseLateBlight, diseaseBacterialSpot, diseaseLeafCurl, diseaseSeptoria)
        ),
        CropDetailItem(
            id = 2,
            name = "Chilli",
            scientificName = "Capsicum annuum",
            category = "Vegetables",
            growingSeason = "Jun – Oct",
            idealTemp = "20 – 32°C",
            soilType = "Sandy Loam",
            waterRequirement = "Moderate",
            about = "Chilli is an important spice and cash crop valued for its pungency and vitamin C content.",
            proTip = "Avoid excessive nitrogen to prevent foliage overgrowth and flower drop.",
            imageRes = R.drawable.bg_2,
            diseases = listOf(diseaseLeafCurl, diseaseBacterialSpot)
        ),
        CropDetailItem(
            id = 3,
            name = "Potato",
            scientificName = "Solanum tuberosum",
            category = "Vegetables",
            growingSeason = "Oct – Feb",
            idealTemp = "15 – 24°C",
            soilType = "Loamy soil",
            waterRequirement = "Moderate",
            about = "Potato is a high-yield tuber crop requiring cool weather and well-drained fertile soil.",
            proTip = "Earthing up soil around plants prevents greening of tubers.",
            imageRes = R.drawable.bg_3,
            diseases = listOf(diseaseLateBlight, diseaseEarlyBlight)
        ),
        CropDetailItem(
            id = 4,
            name = "Rice",
            scientificName = "Oryza sativa",
            category = "Cereals",
            growingSeason = "Jun – Nov",
            idealTemp = "22 – 35°C",
            soilType = "Clayey soil",
            waterRequirement = "High",
            about = "Rice is a primary staple food crop that thrives in flooded, water-retentive clayey soils.",
            proTip = "Maintain optimum water depth during flowering for maximum grain filling.",
            imageRes = R.drawable.bg_4,
            diseases = listOf(diseaseBacterialSpot)
        ),
        CropDetailItem(
            id = 5,
            name = "Wheat",
            scientificName = "Triticum aestivum",
            category = "Cereals",
            growingSeason = "Nov – Apr",
            idealTemp = "15 – 25°C",
            soilType = "Clay Loam",
            waterRequirement = "Moderate",
            about = "Wheat is a major cereal grain grown in winter requiring cool temperatures during growth.",
            proTip = "Crown root initiation stage (21 days) is critical for first irrigation.",
            imageRes = R.drawable.bg_5,
            diseases = listOf(diseaseEarlyBlight)
        ),
        CropDetailItem(
            id = 6,
            name = "Maize",
            scientificName = "Zea mays",
            category = "Cereals",
            growingSeason = "Jun – Sep",
            idealTemp = "21 – 30°C",
            soilType = "Sandy Loam",
            waterRequirement = "Moderate",
            about = "Maize is a versatile cereal used for food, fodder, and industrial raw material.",
            proTip = "Keep fields weed-free for the first 30–45 days after sowing.",
            imageRes = R.drawable.bg_6,
            diseases = listOf(diseaseBacterialSpot)
        ),
        CropDetailItem(
            id = 7,
            name = "Brinjal",
            scientificName = "Solanum melongena",
            category = "Vegetables",
            growingSeason = "Year-round",
            idealTemp = "21 – 32°C",
            soilType = "Silt Loam",
            waterRequirement = "Moderate",
            about = "Brinjal (Eggplant) is a popular warm-season vegetable harvested over several months.",
            proTip = "Prune lower old leaves to encourage new shoots and fruit formation.",
            imageRes = R.drawable.bg_1,
            diseases = listOf(diseaseLeafCurl)
        ),
        CropDetailItem(
            id = 8,
            name = "Cucumber",
            scientificName = "Cucumis sativus",
            category = "Vegetables",
            growingSeason = "Feb – Jun",
            idealTemp = "18 – 28°C",
            soilType = "Sandy Loam",
            waterRequirement = "Moderate",
            about = "Cucumber is a fast-growing vine crop requiring warm climate and regular watering.",
            proTip = "Provide vertical trellising for cleaner fruits and higher yields.",
            imageRes = R.drawable.bg_2,
            diseases = listOf(diseaseLateBlight)
        ),
        CropDetailItem(
            id = 9,
            name = "Okra",
            scientificName = "Abelmoschus esculentus",
            category = "Vegetables",
            growingSeason = "Mar – Jul",
            idealTemp = "22 – 35°C",
            soilType = "Loose Loam",
            waterRequirement = "Moderate",
            about = "Okra (Ladyfinger) is a resilient warm-weather crop rich in dietary fiber.",
            proTip = "Harvest tender pods every 2-3 days to promote continuous flowering.",
            imageRes = R.drawable.bg_3,
            diseases = listOf(diseaseLeafCurl)
        )
    )

    private val trackedCrops = mutableListOf(
        MyCropItem(
            id = 1,
            cropName = "Tomato",
            status = "Growing",
            plantedDate = "12 Jul 2026",
            imageRes = R.drawable.bg_1,
            timeline = listOf(
                CropTimelineStep("Sowing", "12 Jul 2026", isCompleted = true, isCurrent = false),
                CropTimelineStep("Vegetative", "28 Jul 2026", isCompleted = true, isCurrent = false),
                CropTimelineStep("Flowering", "10 Aug 2026", isCompleted = false, isCurrent = true),
                CropTimelineStep("Fruiting", "Expected: 25 Aug 2026", isCompleted = false, isCurrent = false),
                CropTimelineStep("Harvesting", "Expected: 15 Sep 2026", isCompleted = false, isCurrent = false)
            )
        ),
        MyCropItem(
            id = 2,
            cropName = "Chilli",
            status = "Vegetative",
            plantedDate = "20 Jul 2026",
            imageRes = R.drawable.bg_2,
            timeline = listOf(
                CropTimelineStep("Sowing", "20 Jul 2026", isCompleted = true, isCurrent = false),
                CropTimelineStep("Vegetative", "05 Aug 2026", isCompleted = false, isCurrent = true),
                CropTimelineStep("Flowering", "Expected: 22 Aug 2026", isCompleted = false, isCurrent = false),
                CropTimelineStep("Harvesting", "Expected: 28 Sep 2026", isCompleted = false, isCurrent = false)
            )
        ),
        MyCropItem(
            id = 3,
            cropName = "Brinjal",
            status = "Flowering",
            plantedDate = "05 Aug 2026",
            imageRes = R.drawable.bg_1,
            timeline = listOf(
                CropTimelineStep("Sowing", "05 Aug 2026", isCompleted = true, isCurrent = false),
                CropTimelineStep("Vegetative", "18 Aug 2026", isCompleted = true, isCurrent = false),
                CropTimelineStep("Flowering", "30 Aug 2026", isCompleted = false, isCurrent = true),
                CropTimelineStep("Harvesting", "Expected: 10 Oct 2026", isCompleted = false, isCurrent = false)
            )
        )
    )

    fun getPopularCrops(): List<CropDetailItem> = allCropsList.take(6)

    fun getAllCrops(): List<CropDetailItem> = allCropsList

    fun getCropById(id: Int): CropDetailItem? {
        return allCropsList.find { it.id == id } ?: allCropsList.firstOrNull()
    }

    fun getCropByName(name: String): CropDetailItem? {
        return allCropsList.find { it.name.equals(name, ignoreCase = true) } ?: allCropsList.first()
    }

    fun getMyCrops(): List<MyCropItem> = trackedCrops

    fun addMyCrop(cropName: String) {
        val existing = trackedCrops.find { it.cropName.equals(cropName, ignoreCase = true) }
        if (existing == null) {
            val template = allCropsList.find { it.name.equals(cropName, ignoreCase = true) }
            val newItem = MyCropItem(
                id = System.currentTimeMillis().toInt(),
                cropName = cropName,
                status = "Growing",
                plantedDate = "Today",
                imageRes = template?.imageRes ?: R.drawable.bg_1,
                timeline = listOf(
                    CropTimelineStep("Sowing", "Today", isCompleted = true, isCurrent = true),
                    CropTimelineStep("Vegetative", "Expected: +14 days", isCompleted = false, isCurrent = false),
                    CropTimelineStep("Flowering", "Expected: +30 days", isCompleted = false, isCurrent = false),
                    CropTimelineStep("Harvesting", "Expected: +60 days", isCompleted = false, isCurrent = false)
                )
            )
            trackedCrops.add(newItem)
        }
    }

    fun removeMyCrop(cropName: String) {
        trackedCrops.removeAll { it.cropName.equals(cropName, ignoreCase = true) }
    }
}
