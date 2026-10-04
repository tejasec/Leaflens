# Graph Report - Leaflens  (2026-10-04)

## Corpus Check
- 141 files · ~100,698,119 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 707 nodes · 752 edges · 93 communities detected
- Extraction: 94% EXTRACTED · 6% INFERRED · 0% AMBIGUOUS · INFERRED: 48 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `22e0fe7d`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- [[_COMMUNITY_Community 0|Community 0]]
- [[_COMMUNITY_Community 1|Community 1]]
- [[_COMMUNITY_Community 2|Community 2]]
- [[_COMMUNITY_Community 3|Community 3]]
- [[_COMMUNITY_Community 4|Community 4]]
- [[_COMMUNITY_Community 5|Community 5]]
- [[_COMMUNITY_Community 6|Community 6]]
- [[_COMMUNITY_Community 7|Community 7]]
- [[_COMMUNITY_Community 8|Community 8]]
- [[_COMMUNITY_Community 9|Community 9]]
- [[_COMMUNITY_Community 10|Community 10]]
- [[_COMMUNITY_Community 11|Community 11]]
- [[_COMMUNITY_Community 12|Community 12]]
- [[_COMMUNITY_Community 13|Community 13]]
- [[_COMMUNITY_Community 14|Community 14]]
- [[_COMMUNITY_Community 15|Community 15]]
- [[_COMMUNITY_Community 16|Community 16]]
- [[_COMMUNITY_Community 17|Community 17]]
- [[_COMMUNITY_Community 18|Community 18]]
- [[_COMMUNITY_Community 19|Community 19]]
- [[_COMMUNITY_Community 20|Community 20]]
- [[_COMMUNITY_Community 21|Community 21]]
- [[_COMMUNITY_Community 22|Community 22]]
- [[_COMMUNITY_Community 23|Community 23]]
- [[_COMMUNITY_Community 24|Community 24]]
- [[_COMMUNITY_Community 25|Community 25]]
- [[_COMMUNITY_Community 26|Community 26]]
- [[_COMMUNITY_Community 27|Community 27]]
- [[_COMMUNITY_Community 28|Community 28]]
- [[_COMMUNITY_Community 29|Community 29]]
- [[_COMMUNITY_Community 30|Community 30]]
- [[_COMMUNITY_Community 31|Community 31]]
- [[_COMMUNITY_Community 32|Community 32]]
- [[_COMMUNITY_Community 33|Community 33]]
- [[_COMMUNITY_Community 34|Community 34]]
- [[_COMMUNITY_Community 35|Community 35]]
- [[_COMMUNITY_Community 36|Community 36]]
- [[_COMMUNITY_Community 37|Community 37]]
- [[_COMMUNITY_Community 38|Community 38]]
- [[_COMMUNITY_Community 39|Community 39]]
- [[_COMMUNITY_Community 40|Community 40]]
- [[_COMMUNITY_Community 41|Community 41]]
- [[_COMMUNITY_Community 42|Community 42]]
- [[_COMMUNITY_Community 43|Community 43]]
- [[_COMMUNITY_Community 44|Community 44]]
- [[_COMMUNITY_Community 45|Community 45]]
- [[_COMMUNITY_Community 46|Community 46]]
- [[_COMMUNITY_Community 47|Community 47]]
- [[_COMMUNITY_Community 48|Community 48]]
- [[_COMMUNITY_Community 49|Community 49]]
- [[_COMMUNITY_Community 50|Community 50]]
- [[_COMMUNITY_Community 51|Community 51]]
- [[_COMMUNITY_Community 52|Community 52]]
- [[_COMMUNITY_Community 53|Community 53]]
- [[_COMMUNITY_Community 54|Community 54]]
- [[_COMMUNITY_Community 55|Community 55]]
- [[_COMMUNITY_Community 56|Community 56]]
- [[_COMMUNITY_Community 57|Community 57]]
- [[_COMMUNITY_Community 58|Community 58]]
- [[_COMMUNITY_Community 59|Community 59]]
- [[_COMMUNITY_Community 60|Community 60]]
- [[_COMMUNITY_Community 61|Community 61]]
- [[_COMMUNITY_Community 62|Community 62]]
- [[_COMMUNITY_Community 63|Community 63]]
- [[_COMMUNITY_Community 64|Community 64]]
- [[_COMMUNITY_Community 65|Community 65]]
- [[_COMMUNITY_Community 66|Community 66]]
- [[_COMMUNITY_Community 67|Community 67]]
- [[_COMMUNITY_Community 68|Community 68]]
- [[_COMMUNITY_Community 69|Community 69]]
- [[_COMMUNITY_Community 70|Community 70]]
- [[_COMMUNITY_Community 71|Community 71]]
- [[_COMMUNITY_Community 72|Community 72]]
- [[_COMMUNITY_Community 73|Community 73]]
- [[_COMMUNITY_Community 74|Community 74]]
- [[_COMMUNITY_Community 75|Community 75]]
- [[_COMMUNITY_Community 76|Community 76]]
- [[_COMMUNITY_Community 77|Community 77]]
- [[_COMMUNITY_Community 78|Community 78]]
- [[_COMMUNITY_Community 79|Community 79]]
- [[_COMMUNITY_Community 80|Community 80]]
- [[_COMMUNITY_Community 81|Community 81]]
- [[_COMMUNITY_Community 82|Community 82]]
- [[_COMMUNITY_Community 85|Community 85]]
- [[_COMMUNITY_Community 86|Community 86]]
- [[_COMMUNITY_Community 87|Community 87]]
- [[_COMMUNITY_Community 88|Community 88]]
- [[_COMMUNITY_Community 89|Community 89]]
- [[_COMMUNITY_Community 90|Community 90]]
- [[_COMMUNITY_Community 91|Community 91]]
- [[_COMMUNITY_Community 92|Community 92]]
- [[_COMMUNITY_Community 95|Community 95]]
- [[_COMMUNITY_Community 96|Community 96]]

## God Nodes (most connected - your core abstractions)
1. `CropHealthActivity` - 10 edges
2. `CropHealthClassifier` - 10 edges
3. `VoiceAssistantService` - 10 edges
4. `FewShotRepositoryTest` - 10 edges
5. `RecaptureCameraFragment` - 9 edges
6. `ScanFragment` - 9 edges
7. `TFLiteClassifier` - 9 edges
8. `UserDao` - 8 edges
9. `CropRepository` - 8 edges
10. `CropActivityDao` - 7 edges

## Surprising Connections (you probably didn't know these)
- `RegisterScreen()` --calls--> `AuthHeader()`  [INFERRED]
  app/src/main/java/com/example/smartagriculture/compose/RegisterScreen.kt → app/src/main/java/com/example/smartagriculture/compose/components/AuthComponents.kt
- `LoginScreen()` --calls--> `AuthTextField()`  [INFERRED]
  app/src/main/java/com/example/smartagriculture/compose/LoginScreen.kt → app/src/main/java/com/example/smartagriculture/compose/components/AuthComponents.kt
- `LoginScreen()` --calls--> `AuthButton()`  [INFERRED]
  app/src/main/java/com/example/smartagriculture/compose/LoginScreen.kt → app/src/main/java/com/example/smartagriculture/compose/components/AuthComponents.kt
- `RegisterScreen()` --calls--> `AuthTextField()`  [INFERRED]
  app/src/main/java/com/example/smartagriculture/compose/RegisterScreen.kt → app/src/main/java/com/example/smartagriculture/compose/components/AuthComponents.kt
- `RegisterScreen()` --calls--> `AuthButton()`  [INFERRED]
  app/src/main/java/com/example/smartagriculture/compose/RegisterScreen.kt → app/src/main/java/com/example/smartagriculture/compose/components/AuthComponents.kt

## Communities (104 total, 65 thin omitted)

### Community 0 - "Community 0"
Cohesion: 0.12
Nodes (9): ScanResultFragment, DiagnosticDossier, PdfReportGenerator, onDone(), onError(), onStart(), TtsState, VoiceAssistantService (+1 more)

### Community 1 - "Community 1"
Cohesion: 0.11
Nodes (11): ClassResult, TFLiteClassifier, ScanHistoryItem, CloudSuccess, CropHealthUiState, CropHealthViewModel, Error, Idle (+3 more)

### Community 2 - "Community 2"
Cohesion: 0.13
Nodes (7): BenchmarkAdapter, BenchmarkGalleryFragment, SampleItem, ViewHolder, ClassificationResult, CropHealthClassifier, Diagnosis

### Community 3 - "Community 3"
Cohesion: 0.1
Nodes (5): PrototypeEntity, VectorTypeConverter, FewShotRepository, PrototypeMatch, FewShotRepositoryTest

### Community 4 - "Community 4"
Cohesion: 0.13
Nodes (6): SchemesAdapter, SchemeViewHolder, SchemeEntity, SchemesFragment, Scheme, SchemesRepository

### Community 5 - "Community 5"
Cohesion: 0.13
Nodes (4): ScanAnalyzingFragment, ScanDetailsFragment, DiseaseAnalysisResult, GeminiService

### Community 6 - "Community 6"
Cohesion: 0.15
Nodes (5): ChatMessageAdapter, ChatViewHolder, AskCropDoctorBottomSheet, newInstance(), ChatMessage

### Community 7 - "Community 7"
Cohesion: 0.17
Nodes (16): chatbot_chat(), ChatbotRequest, ChatbotResponse, CloudPredictionResponse, generate_gradcam_overlay(), get_online_schemes(), GovtScheme, health_check() (+8 more)

### Community 8 - "Community 8"
Cohesion: 0.15
Nodes (12): create_blurry_leaf_image_bytes(), create_sharp_leaf_image_bytes(), LeafLens AI - Backend Automated Integration Test Suite =========================, Validates that strict quality enforcement returns HTTP 400 Bad Request., Generates a high-contrast 224x224 synthetic leaf with edges (Laplacian Var > 100, Generates a uniform smooth green image with zero high frequencies (Laplacian Var, Validates that multipart uploads using either 'file' or 'image' field work., Validates that blurry input returns low-confidence rejection without raising 500 (+4 more)

### Community 9 - "Community 9"
Cohesion: 0.16
Nodes (9): AuthButton(), AuthHeader(), AuthTextField(), SocialButton(), SocialLoginButtons(), LoginScreen(), RegisterScreen(), LoginFragment (+1 more)

### Community 10 - "Community 10"
Cohesion: 0.16
Nodes (4): MarketAdapter, MarketViewHolder, MarketFragment, MarketPrice

### Community 11 - "Community 11"
Cohesion: 0.16
Nodes (14): embed_image_html(), get_color_map(), get_layer_vis_square(), load_image(), Resizes an image and returns it as a np.array      Arguments:     image -- a PIL, Returns an image embedded in HTML base64 format     (Based on Caffe's web_demo), Returns a vis_square for the given layer data      Arguments:     data -- a np.n, Visualize each image in a grid of size approx sqrt(n) by sqrt(n)     Returns a n (+6 more)

### Community 12 - "Community 12"
Cohesion: 0.19
Nodes (3): CropListAdapter, ViewHolder, AllCropsFragment

### Community 13 - "Community 13"
Cohesion: 0.2
Nodes (3): ScanHistoryAdapter, ViewHolder, HistoryFragment

### Community 14 - "Community 14"
Cohesion: 0.15
Nodes (5): CropDetailItem, CropDiseaseItem, CropTimelineStep, MyCropItem, CropRepository

### Community 15 - "Community 15"
Cohesion: 0.21
Nodes (3): CalendarAdapter, CalendarViewHolder, CalendarFragment

### Community 16 - "Community 16"
Cohesion: 0.18
Nodes (4): DashboardAdapter, ViewHolder, HomeFragment, DashboardItem

### Community 17 - "Community 17"
Cohesion: 0.18
Nodes (4): PestAdapter, PestViewHolder, PestFragment, Pest

### Community 18 - "Community 18"
Cohesion: 0.22
Nodes (3): CropDiseaseAdapter, ViewHolder, CropDetailsFragment

### Community 19 - "Community 19"
Cohesion: 0.2
Nodes (4): SoilAdapter, SoilViewHolder, SoilFragment, Soil

### Community 21 - "Community 21"
Cohesion: 0.22
Nodes (3): PopularCropAdapter, ViewHolder, CropGuideFragment

### Community 25 - "Community 25"
Cohesion: 0.39
Nodes (3): DetailedQualityResult, QualityGate, QualityResult

### Community 31 - "Community 31"
Cohesion: 0.25
Nodes (5): apply_temperature_scaling(), compute_ece(), LeafLens AI - Empirical Baseline Comparison & Robustness Evaluation ============, Computes Expected Calibration Error (ECE) across confidence bins., Scales logits by temperature T and applies softmax.

### Community 38 - "Community 38"
Cohesion: 0.29
Nodes (3): PlantVillage, Returns SplitGenerators., PlantVillage Dataset.

### Community 48 - "Community 48"
Cohesion: 0.4
Nodes (3): MandiApiService, MandiRecord, MandiResponse

### Community 49 - "Community 49"
Cohesion: 0.4
Nodes (3): CurrentWeather, WeatherApiService, WeatherResponse

### Community 51 - "Community 51"
Cohesion: 0.5
Nodes (3): InfectionGrade, SeverityAnalyzer, SeverityResult

### Community 69 - "Community 69"
Cohesion: 0.83
Nodes (3): create_data_zip(), main(), upload_files()

## Knowledge Gaps
- **48 isolated node(s):** `AppConfig`, `AboutFragment`, `CropDetailItem`, `CropDiseaseItem`, `Crop` (+43 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **65 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `DiseaseAnalysisResult` connect `Community 5` to `Community 0`?**
  _High betweenness centrality (0.010) - this node is a cross-community bridge._
- **Why does `CropHealthClassifier` connect `Community 2` to `Community 5`?**
  _High betweenness centrality (0.007) - this node is a cross-community bridge._
- **Why does `ScanHistoryItem` connect `Community 1` to `Community 0`?**
  _High betweenness centrality (0.007) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `CropHealthClassifier` (e.g. with `.onViewCreated()` and `.onViewCreated()`) actually correct?**
  _`CropHealthClassifier` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `AppConfig`, `AboutFragment`, `CropDetailItem` to the rest of the system?**
  _48 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.12 - nodes in this community are weakly interconnected._
- **Should `Community 1` be split into smaller, more focused modules?**
  _Cohesion score 0.11 - nodes in this community are weakly interconnected._