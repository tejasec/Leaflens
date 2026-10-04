# Industry Problem Brief: LeafLens AI (BAI-03)

**Academic Portfolio:** K.E.S. Shroff College / University of Mumbai — AY 2026–27  
**Project Code:** BAI-03  
**Target Maturity:** Technology Readiness Level 4–5 (Industry Prototype)  
**Effort Benchmark:** 100 Hours Minimum Individual Ownership per Student  

---

## 1. Executive Summary & Problem Validation

Agricultural productivity forms the bedrock of rural livelihoods and global food security, sustaining more than 58% of rural households across India and developing economies. However, phytopathogenic infections—including fungal blights, bacterial spots, viral mosaics, and pest infestations—inflict an estimated 20% to 40% loss on annual crop yields globally.

While academic computer vision models have achieved impressive benchmark accuracies, their practical field adoption across smallholder agrarian communities is severely impeded by four operational bottlenecks:

1. **Connectivity Dead Zones (Rural WAN Inoperability):** Over 70% of cultivated agricultural acreage in developing nations falls into 2G, Edge, or total cellular dead zones. Existing cloud-dependent diagnostic APIs completely fail in these environments.
2. **Black-Box Distrust & Skepticism:** Unexplained neural network classifications generate farmer skepticism. When an application predicts "Late Blight" without highlighting the specific lesion or justifying the output, farmers reject the advice.
3. **Garbage-In, Overconfident-Out:** Field captures suffer from motion blur, camera defocus, solar glare, and non-foliar backgrounds. Standard uncalibrated neural networks output 98%+ confidence on completely degraded or out-of-distribution inputs, leading to disastrous misdiagnoses.
4. **Model Rigidity to Regional Pathogens:** Pre-trained static deep networks cannot adapt to novel, localized, or emerging crop diseases without expensive, multi-month cloud dataset re-collection and retraining.

LeafLens AI resolves these systemic challenges through an edge-native, explainable mobile assistant featuring pre-flight OpenCV quality gating, INT8-quantized MobileNetV2 inference, on-device Grad-CAM heatmaps, temperature-calibrated uncertainty safeguards, and on-device metric-space few-shot adaptation.

---

## 2. Stakeholder Map & User Personas

| Stakeholder Persona | Key Operational Pain Points | LeafLens AI Feature Solution |
| :--- | :--- | :--- |
| **Smallholder Farmer** *(Primary User)* | • Zero internet connectivity in remote fields.<br>• Inability to distinguish look-alike blights.<br>• Risk of purchasing incorrect/adulterated chemicals. | • 100% offline edge inference (<180 ms).<br>• Visual Grad-CAM heatmap showing active spots.<br>• Dual-track non-chemical + chemical advice. |
| **KVK Extension Officer** *(Field Scout)* | • High scout-to-farmer ratio (1:1,000+).<br>• Subjective manual symptom grading.<br>• Lack of standardized diagnostic audit logs. | • Automated lesion severity grading (HSV).<br>• Standardized Room DB scan case history.<br>• Few-shot on-device registration of novel diseases. |
| **Agronomist / Researcher** | • Model overconfidence on blurry or out-of-distribution inputs.<br>• Lack of calibration transparency. | • Temperature-calibrated softmax ($T = 1.35$).<br>• Expected Calibration Error (ECE) reduced to 5.3%.<br>• Mandatory expert-override pathway. |

---

## 3. Agile Backlog & User Stories

### Epic 1: Edge Acquisition & Pre-Flight Quality Gate
* **US-01 (Blur Rejection):** *As a farmer*, I want the application to alert me if my captured leaf photo is blurry, *so that* I do not receive an erroneous disease diagnosis caused by camera motion.
* **US-02 (Exposure & Foliage Validation):** *As a user*, I want the app to verify that a green plant leaf is present in good lighting, *so that* photos of soil, hands, or shadows are rejected immediately.

### Epic 2: Explainable On-Device Diagnosis
* **US-03 (Offline Inference):** *As a rural scout*, I want immediate disease diagnosis without cellular data, *so that* I can triage crop conditions in remote fields.
* **US-04 (Grad-CAM Visual Heatmap):** *As a farmer*, I want to see an interactive heatmap spotlighting the infected leaf area, *so that* I can trust the AI's visual reasoning.

### Epic 3: Responsible AI & Uncertainty Handling
* **US-05 (Calibrated Confidence Warning):** *As a field scout*, I want the app to warn me when prediction confidence drops below 70%, *so that* I do not apply expensive fungicides based on an uncertain guess.
* **US-06 (Human Override):** *As an experienced farmer*, I want the ability to bypass quality gates or request agronomist review, *so that* edge cases can still be logged and inspected.

### Epic 4: Treatment Advisory & Community Ecosystem
* **US-07 (Dual-Track Care Guidance):** *As a sustainable grower*, I want both organic remedies (neem extract) and regulated chemical recommendations with Post-Harvest Intervals, *so that* I can protect yields safely.
* **US-08 (Mandi Prices & Govt Schemes):** *As a farmer*, I want live commodity prices and government subsidies in the same app, *so that* I can make informed economic decisions.

---

## 4. Non-Functional Requirements (NFRs)

* **NFR-01 (Inference Latency):** Mean on-device inference latency must remain below **200 ms** on mid-range Android processors (Snapdragon 695 / Exynos 850) with $\ge$ 4 GB RAM.
* **NFR-02 (Bandwidth Conservation):** Baseline edge diagnosis must consume **0 MB WAN data** (>95% bandwidth savings over cloud APIs).
* **NFR-03 (Storage & Memory Footprint):** App memory footprint during inference must remain below **25 MB RAM**, and the INT8 quantized model file must not exceed **3.0 MB**.
* **NFR-04 (Responsible AI Safety):** The system must present a prominent statutory non-diagnostic legal disclaimer across all interfaces, disclaiming clinical botanical certification.

---

## 5. Misuse & Abuse Cases

1. **Non-Foliar Input Abuse:** Attempting to feed non-plant images (faces, concrete floors, machinery). *Mitigation:* OpenCV Green Foliage Ratio filter rejects images where foliar area $< 15\%$.
2. **Severe Optical Distortion:** Submitting heavily blurred or out-of-focus captures. *Mitigation:* Laplacian variance threshold ($\sigma^2 < 100.0$) intercepts blur before neural network inference.
3. **Over-reliance on Chemical Recommendations:** Blindly applying chemical fungicides without checking harvest safety intervals. *Mitigation:* Prominent display of Post-Harvest Interval (PHI) days, Restricted-Entry Interval (REI) hours, and organic non-chemical alternatives prioritized first.
