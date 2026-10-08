# AGENTS.md

## Project overview
LeafLens AI is an explainable, offline-first crop disease triage assistant (college capstone).
It spans three stacks in one repo:

| Path | What it is |
|---|---|
| `app/` | Android app (Kotlin, package `com.example.smartagriculture`): TFLite on-device inference + Grad-CAM, hybrid Compose + XML fragments, Room, CameraX, Firebase Auth, Retrofit cloud fallback |
| `backend/` | FastAPI cloud fallback service (Python), runs in Docker on port `8000` |
| `evaluation/`, `docs/`, `MODEL_CARD.md` | ML benchmark script and capstone deliverables — keep in sync with model/eval changes |
| `PlantVillage-Dataset/`, `mlruns/` | Training data and MLflow run artifacts. Treat as read-only; never edit or delete |

## Dev environment tips
- Android: JDK 21, compileSdk/targetSdk 37, minSdk 26. Gradle Kotlin DSL with a
  version catalog — add dependencies in `gradle/libs.versions.toml`, not hard-coded in
  `app/build.gradle.kts`.
- Backend: Python 3.10+, deps in `backend/requirements.txt`, virtualenv at `.venv/` (repo root).
- **Build gotcha:** the Gradle build expects a `flutter_ui/` sibling directory *outside this repo*
  (`settings.gradle.kts` includes `../flutter_ui/.android/include_flutter.groovy`, and
  `app/build.gradle.kts` depends on `:flutter`). A clean clone will not build the app without it.
- Secrets live in `local.properties` (`gemini.api.key`, `flutter.sdk`) — gitignored.
  Never hardcode or commit them; the app exposes the key via `BuildConfig`.
- App code is organized by feature under `app/src/main/java/com/example/smartagriculture/`:
  `fragments/`, `ml/`, `quality/`, `database/`, `network/`, `repository/`, `viewmodel/`, `adapter/`.
  Match the package of the feature you are changing.

## Commands

### File-scoped (preferred — fast feedback)
```bash
# Backend: single test file / single test
PYTHONPATH=backend pytest backend/tests/test_api.py -v
PYTHONPATH=backend pytest backend/tests/test_api.py -k "test_health" -v

# Android: single test class
./gradlew :app:testDebugUnitTest --tests "com.example.smartagriculture.quality.QualityGateTest"
```

### Full suite / builds (only when explicitly requested)
```bash
./gradlew test                      # all Android unit tests
./gradlew :app:assembleDebug        # full debug APK build (slow)
docker compose up -d --build        # build + start backend on :8000
python3 evaluation/baseline_comparison.py   # ML benchmark (slow)
python3 backend/mlflow_tracking.py  # log an MLflow run
```

## Testing
- Backend: pytest with FastAPI `TestClient` in `backend/tests/test_api.py` (9 integration tests:
  health, predict valid/blurry, chatbot fallback, schemes). No server needed.
- Android: JUnit + Robolectric + Mockito + coroutines-test in `app/src/test/`.
  Instrumented tests in `app/src/androidTest/` require a device/emulator — do not run them casually.
- **Trap:** root-level `test_api.py` and `test_api_models.py` are ad-hoc Gemini API smoke scripts,
  not the test suite. Always target `backend/tests/` explicitly; never run bare `pytest` at the root.
- There is no CI pipeline (no `.github/workflows`). Local runs are the only gate.

## Code conventions
- Kotlin: follow existing patterns per package. XML screens use ViewBinding; the auth flow
  (`compose/LoginScreen.kt`, `compose/RegisterScreen.kt`) is Compose. Keep logic out of fragments —
  put it in ViewModels and Repositories.
- Room: entities/DAOs live in `database/`. Any schema change needs a version bump and a migration.
- ML code lives in `ml/` (`TFLiteClassifier`, `CropHealthClassifier`, `GradCamEngine`,
  `FeatureEmbeddingExtractor`) and `quality/` (`QualityGate`, `ImageQualityChecker`).
- Python: keep FastAPI endpoints thin. Pydantic response schemas mirror the Android Retrofit
  contracts exactly (camelCase field names) — change both sides together.
- Never hardcode API keys, endpoints, or confidence thresholds; read them from config
  (`AppConfig.kt`, env vars).
- Prefer small, focused diffs. Do not mix refactors with feature changes.

## Model retraining
There is **no training pipeline in this repo** — the MobileNetV2 was trained offline. The repo only
holds the deployable artifacts and the certified evaluation constants. Retraining is therefore an
artifact-replacement operation, and it requires explicit approval before touching anything.

Before retraining at all, prefer the designed alternative: for **novel regional diseases**, the
app supports on-device few-shot enrollment (3–5 photos → 1024-d embeddings → Room k-NN). See
`EnrollPathogenFragment.kt` and `FewShotRepository.kt`. Retraining the base model is the heavy,
last-resort path.

### Artifact contract (what a retrained model must satisfy)
- Format: INT8 post-training-quantized TFLite, 224x224x3 input, ~2.5 MB, TFLite 2.16 runtime
- Deployed in **two copies that must stay identical**:
  - `app/src/main/assets/crop_disease_model.tflite`
  - `app/src/main/assets/models/mobilenet_v2_crop_quant.tflite`
- Class list: `app/src/main/assets/models/labels.txt` — exactly 25 classes, order must match
  the model's output indices
- Training/eval protocol per `MODEL_CARD.md` §4: splits at leaf-specimen level (not image level),
  hold-out test set quarantined from training and temperature-calibration search

### Retraining checklist (all steps required)
1. Retrain offline on the PlantVillage-derived set (`PlantVillage-Dataset/` is the read-only
   upstream source; do not modify it in place)
2. Recompute **temperature scaling** on the validation split — the current `T = 1.35` is certified
   to the old weights and is invalid for new logits
3. Re-run `python3 evaluation/baseline_comparison.py` and regenerate
   `evaluation/baseline_comparison_results.json`
4. Replace **both** `.tflite` copies; update `labels.txt` if the class set changed
5. Update the certified constants everywhere they are duplicated — a change in one place is a bug:
   - `backend/mlflow_tracking.py` (params + metrics dicts)
   - `backend/app/main.py` (confidence threshold, treatment database keyed by disease label)
   - `evaluation/baseline_comparison.py` (Black Book constants at top of file)
   - Android: `AppConfig.kt` and the `ml/`/`quality/` packages (tau, blur threshold 100.0)
6. Update `MODEL_CARD.md` and `docs/evaluation_dossier.md` with the new metrics; then
   `python3 backend/mlflow_tracking.py` to log the run
7. Run `./gradlew test` — `GradCamEngineTest`, `FeatureEmbeddingExtractorTest`, and
   `QualityGateTest` validate the pipeline against the bundled model

Never ship a model whose calibrated ECE or hold-out metrics regress vs. `MODEL_CARD.md` without
recording the regression there.

## Safety and permissions
Allowed without prompting:
- read/list files
- run single-file backend tests, single Android test classes
- run lint/format on files you touched

Ask first:
- installing new packages (`pip install`, adding Gradle deps)
- full builds (`./gradlew :app:assembleDebug`) or full test suites
- rebuilding Docker images
- modifying or replacing `.tflite` assets / `labels.txt` (see Model retraining)
- retraining or recalibrating the model
- `git push`, deleting files, changing `local.properties` or `google-services.json`

## PR checklist (before committing)
- Backend changes: `PYTHONPATH=backend pytest backend/tests/test_api.py -v` is green
- Android changes: `./gradlew test` is green
- New code paths have tests
- Diff is small and focused; commit message says what changed and how it was tested
- Docs that describe changed behavior (`docs/`, `MODEL_CARD.md`, `README.md`) are updated

## When stuck
Ask a clarifying question or state assumptions in the commit/PR notes. If the Gradle build fails
on a clean setup, check the `flutter_ui/` sibling-directory requirement before debugging code.
