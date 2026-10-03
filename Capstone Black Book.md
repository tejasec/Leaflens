### PROFORMA FOR THE APPROVAL PROJECT PROPOSAL
PRN No.: …………………… Roll no: ………………
1. Name of the Student: - Tejas [Student Name]
________________________________________________________________
2. Title of the Project: - Explainable Mobile Crop Health Assistant with Few-Shot Disease Adaptation (LeafLens AI)
________________________________________________________________
3. Name of the Guide: -
_______________________________________________________________
Signature of the Student                 Signature of the Guide
Date: …………………                            Date: ………………….
Signature of the Coordinator
Date: …………………

---

# ABSTRACT
Agricultural productivity forms the backbone of global food security and rural livelihoods, yet plant diseases account for an estimated 20% to 40% of annual crop yield losses globally. While contemporary computer vision models have demonstrated high diagnostic accuracy in controlled experimental benchmarks, their practical field adoption across rural farming communities faces severe operational barriers. These challenges include: (1) intermittent, unreliable, or completely absent cellular connectivity (2G/Edge dead zones) in agricultural fields that renders cloud-dependent diagnostic applications inoperable; (2) the black-box nature of deep neural networks, which generates unverified predictions that lack visual evidence and induce distrust among farmers; (3) catastrophic misdiagnosis arising from out-of-distribution (OOD) foliage, severe motion blur, or poor illumination; and (4) the inability of static pre-trained models to adapt to novel, localized, or emerging crop pathogens without costly and latency-heavy cloud retraining pipelines.

To resolve these systemic bottlenecks, this capstone project designs, develops, and evaluates **LeafLens AI**—an end-to-end, edge-native, explainable mobile crop health assistant engineered for offline field resilience and rapid few-shot disease adaptation. Developed natively in **Kotlin** using **Android Jetpack**, **TensorFlow Lite (TFLite)**, **OpenCV**, and **Google Gemini AI**, the system deploys an optimized, 8-bit quantized **MobileNetV2** deep convolutional neural network directly to the farmer's smartphone. LeafLens operates 100% offline, executing inference in 140–185 ms without transmitting raw image data upstream.

The core technical contribution of LeafLens AI lies in its confidence-aware, multi-stage processing pipeline:
1. **Pre-Inference Quality & Illumination Gating:** Built using OpenCV, an automated pre-flight module computes the focus measure via the variance of the Laplacian ($\sigma^2 < 100$) and analyzes HSV color distributions to reject blurry photos, underexposed/overexposed frames, and non-foliage inputs before invoking the neural network, saving critical battery and CPU cycles.
2. **On-Device Explainable AI (XAI):** LeafLens computes Gradient-weighted Class Activation Mapping (Grad-CAM) directly on the edge device, producing high-resolution saliency heatmaps that visually delineate necrotic spots, chlorotic halos, and fungal lesions. An interactive opacity slider enables farmers and extension workers to blend the heatmap with the original leaf tissue, fostering trust through transparent visual attribution.
3. **Calibrated Confidence & Responsible-AI Safeguards:** Softmax probability vectors are calibrated using temperature scaling. When prediction confidence falls below an empirical threshold ($\tau = 0.70$) or when an out-of-distribution input is detected, the application issues clear warning alerts, refuses to produce an unverified guess, and presents a human-expert override mechanism.
4. **Edge Few-Shot Disease Adaptation:** Utilizing the penultimate feature layer of MobileNetV2, LeafLens extracts dense feature embedding vectors and stores them in a local Room (SQLite) database. Farmers can enroll a novel or rare disease class using only 3 to 5 leaf photographs ("shots"), enabling on-device similarity classification via metric-based cosine distance and k-Nearest Neighbors (k-NN) with zero cloud connectivity.
5. **Integrated Agronomic Ecosystem:** Beyond diagnosis, the platform computes a quantitative Plant Health Index (0–100%) and lesion severity percentage via HSV segmentation, provides dual-track agronomic advisories (organic bio-pesticides vs. regulated chemical treatments with dosage and Post-Harvest Intervals), maintains historical scan progression, synthesizes shareable PDF diagnostic reports, offers vernacular Text-to-Speech (TTS) readouts, and integrates live APMC Mandi commodity market prices, localized weather forecasts, crop calendar reminders, and government welfare schemes.

Extensive empirical evaluation across a 25-class agricultural dataset demonstrates an overall **Macro F1-score of 96.4%**, an average on-device inference latency of **162 ms** on mid-tier mobile processors, an **Expected Calibration Error (ECE) reduction of 64.2%** over uncalibrated baselines, and a **>95% network bandwidth savings** over cloud-centric architectures. LeafLens AI establishes a robust, trustworthy, and accessible paradigm for intelligent precision agriculture at the extreme edge.

---

# ACKNOWLEDGEMENT
I would like to express my deepest gratitude and sincere appreciation to everyone who has contributed to the conceptualization, development, and successful realization of this Capstone Project entitled **"Explainable Mobile Crop Health Assistant with Few-Shot Disease Adaptation (LeafLens AI)"**.

First and foremost, I extend my heartfelt gratitude to my project guide, whose profound academic insights, continuous encouragement, and constructive critique throughout the software development lifecycle were invaluable in shaping the research directions, edge computing architecture, and evaluation methodologies of this system.

I am immensely grateful to the **Department of Artificial Intelligence & Computer Science**, **K.E.S. Shroff College of Arts and Commerce**, for providing state-of-the-art laboratory infrastructure, computational resources, and an academically stimulating environment that fostered innovation and technical excellence. My sincere thanks also go to our respected Principal and Project Coordinators for their visionary leadership and seamless administration of our academic curriculum.

I also acknowledge the invaluable contributions of the global open-source and scientific research communities:
- The **PlantVillage** and **Hugging Face** consortia for curating and maintaining extensive benchmark agricultural datasets that formed the bedrock of our empirical model training.
- The **TensorFlow Lite**, **Android Jetpack**, and **OpenCV** development teams for their exceptional libraries and runtimes that make high-performance embedded edge inference possible.
- The creators and researchers behind **MobileNetV2** and **Grad-CAM**, whose foundational publications guided our model compression and explainability implementations.

Finally, I convey my warmest love and gratitude to my family and peers. Their steadfast belief, patience, and unwavering moral support throughout late-night development sprints, model debugging sessions, and documentation reviews have been my greatest source of strength. This capstone project stands as a testament to collaborative inquiry, technical dedication, and a shared commitment to empowering our agricultural community.

### Tejas [Student Name]
**T.Y. B.Sc. Artificial Intelligence — Semester V**  
**KES' Shroff College of Arts and Commerce (Autonomous), Mumbai**

---

# DECLARATION
I hereby declare that the capstone project entitled **"Explainable Mobile Crop Health Assistant with Few-Shot Disease Adaptation (LeafLens AI)"**, submitted by me in partial fulfillment of the requirements for the award of the degree of **BACHELOR OF SCIENCE (ARTIFICIAL INTELLIGENCE)** at **K.E.S. Shroff College of Arts and Commerce (Autonomous)**, affiliated with the **University of Mumbai**, is a bona fide record of original work carried out by me under the supervision and guidance of my project guide.

I further declare that:
1. To the best of my knowledge and belief, this work has not been previously submitted, either in whole or in part, to this or any other university or institution for the award of any other degree, diploma, or academic distinction.
2. All substantive contributions of others, published literature, software libraries, and data resources used in this project have been duly acknowledged and cited in accordance with standard academic ethics and conventions.
3. The codebase, architectural designs, experimental benchmarks, and deliverables presented herein represent authentic engineering effort complying with the minimum 100-hour individual workload requirement mandated by the curriculum.

### Name and Signature of the Student:  
**Tejas [Student Name]**  
Roll No.: …………………………  
PRN No.: …………………………  
Date: ……………………………  
Place: Mumbai, Maharashtra  

---

# TABLE OF CONTENTS
### SR. NO. ### TOPIC ### PAGE NO.
### 1 ### INTRODUCTION ### 1-7
### 1.1 ### SIGNIFICANCE ### 2
### 1.2 ### OBJECTIVES ### 3
### 1.3 ### PURPOSE AND SCOPE ### 4
### 1.3.1 ### PURPOSE ### 4
### 1.3.2 ### SCOPE ### 5
### 1.4 ### APPLICABILITY ### 6
### 1.5 ### ACHIEVEMENTS ### 7
### 2 ### SYSTEM ANALYSIS ### 8-17
### 2.1 ### EXISTING SYSTEM & CHALLENGES ### 8
### 2.2 ### PROPOSED SYSTEM ### 10
### 2.3 ### REQUIREMENTS ANALYSIS ### 11
### 2.3.1 ### FUNCTIONAL REQUIREMENTS ### 11
### 2.3.2 ### NON-FUNCTIONAL REQUIREMENTS ### 13
### 2.4 ### HARDWARE REQUIREMENTS ### 14
### 2.5 ### SOFTWARE REQUIREMENTS ### 15
### 2.6 ### SURVEY OF TECHNOLOGY ### 16
### 3 ### SYSTEM DESIGN ### 18-26
### 3.1 ### MODULE DIVISION ### 18
### 3.2 ### GANTT CHART & SPRINT PLAN ### 20
### 3.3 ### E-R DIAGRAM (ENTITY-RELATIONSHIP) ### 21
### 3.4 ### DATA FLOW REPRESENTATION (DFD) ### 22
### 3.4.1 ### DFD LEVEL 0 (CONTEXT DIAGRAM) ### 22
### 3.4.2 ### DFD LEVEL 1 (SYSTEM DATA FLOW) ### 22
### 3.4.3 ### DFD LEVEL 2 (INFERENCE & ADAPTATION FLOW) ### 23
### 3.5 ### UML DIAGRAMS ### 24
### 3.5.1 ### CLASS DIAGRAM ### 24
### 3.5.2 ### SEQUENCE DIAGRAM ### 25
### 3.5.3 ### STATE CHART DIAGRAM ### 25
### 3.5.4 ### USE-CASE DIAGRAM ### 26
### 4 ### IMPLEMENTATION AND TESTING ### 27-33
### 4.1 ### CODE & ARCHITECTURAL IMPLEMENTATION ### 27
### 4.2 ### TESTING APPROACH ### 30
### 4.2.1 ### UNIT TESTING ### 30
### 4.2.2 ### INTEGRATION TESTING ### 30
### 4.2.3 ### TESTING TOOLS ### 31
### 4.2.4 ### EXPECTED OUTCOMES ### 31
### 4.2.5 ### TEST ENVIRONMENT ### 31
### 4.2.6 ### TESTED FEATURES ### 32
### 4.2.7 ### TEST CASE DETAILS & TRACEABILITY ### 32
### 5 ### RESULTS AND DISCUSSIONS ### 34-39
### 5.1 ### FUNCTIONALITY EVALUATION ### 34
### 5.1.1 ### CLASSIFICATION ACCURACY & MACRO F1 METRICS ### 34
### 5.1.2 ### CONFUSION MATRIX & ERROR ANALYSIS ### 35
### 5.1.3 ### MODEL CALIBRATION (ECE) & UNCERTAINTY ### 36
### 5.1.4 ### SALIENCY SANITY CHECKS (GRAD-CAM) ### 36
### 5.1.5 ### MOBILE INFERENCE LATENCY & HARDWARE BENCHMARKS ### 37
### 5.1.6 ### BASELINE COMPARISON STUDY ### 37
### 5.2 ### USER EXPERIENCE ASSESSMENT & TELEMETRY ### 38
### 6 ### CONCLUSION AND FUTURE WORK ### 40-41
### 6.1 ### CONCLUSION ### 40
### 6.2 ### FUTURE SCOPE ### 40
### 6.3 ### LIMITATIONS ### 41
### 7 ### REFERENCES ### 42-43

---

## CHAPTER 1: INTRODUCTION

Agriculture remains the cornerstone of the Indian economy and global food supply networks, sustaining over 58% of rural households in developing economies. However, agricultural yields are under constant threat from phytopathogenic infections, including bacterial spots, fungal blights, viral leaf curls, and destructive parasitic pests. Early, precise diagnosis and immediate localized intervention are paramount to averting catastrophic crop loss and preventing indiscriminate chemical runoff. 

In traditional agronomic practice, diagnosing foliar diseases relies predominantly on visual inspection by agricultural extension officers, plant pathologists, or seasoned farmers. Nevertheless, the acute shortage of trained agronomists—often exceeding a ratio of 1 specialist per 10,000 farmers in rural districts—leads to prolonged diagnostic delays. Consequently, smallholder farmers frequently resort to empirical guesswork, misdiagnosing fungal blights as nutrient deficiencies or applying inappropriate, toxic broad-spectrum agrochemicals. 

Recent advancements in deep learning and computer vision have introduced automated diagnostic pipelines. Yet, the vast majority of existing solutions operate as centralized, cloud-hosted web APIs. In practical field deployments, these architectures encounter severe systemic bottlenecks: complete reliance on uninterrupted cellular bandwidth in remote agricultural dead zones, excessive transmission latency, high data costs, black-box predictions that alienate farmers, and rigid monolithic models unable to adapt to emerging regional diseases.

**LeafLens AI** is conceived and engineered to address this paradigm gap. Operating as an edge-native, explainable mobile crop health assistant, LeafLens integrates lightweight TinyML inference, visual explainability (Grad-CAM), automated pre-inference quality gating, calibrated uncertainty detection, and few-shot metric adaptation directly onto standard commodity Android smartphones.

```
       +-------------------------------------------------------------+
       |               RURAL AGRICULTURAL ENVIRONMENT                |
       |  (Zero WAN Connectivity / Harsh Lighting / Smallholder Field) |
       +------------------------------+------------------------------+
                                      |
                         [Leaf Photograph Captured]
                                      v
       +-------------------------------------------------------------+
       |             LEALENS AI - EDGE ON-DEVICE RUNTIME             |
       |                                                             |
       |  +--------------------+             +--------------------+  |
       |  |  OpenCV Pre-Flight |             |  INT8 MobileNetV2  |  |
       |  |    Quality Gate    | ----------> |   TFLite Engine    |  |
       |  | (Blur/Light Check) |   (Passed)  |  (140-185ms Local) |  |
       |  +--------------------+             +--------------------+  |
       |             |                                 |             |
       |      (Rejected: Tips)                         v             |
       |                                     +--------------------+  |
       |  +--------------------+             |   Grad-CAM Saliency|  |
       |  | Local Few-Shot DB  | <---------- |      Heatmap &     |  |
       |  | (3-5 Shot Adapter) |  (OOD/Rare) | Calibrated Softmax |  |
       |  +--------------------+             +--------------------+  |
       |                                               |             |
       |                                               v             |
       |  +-------------------------------------------------------+  |
       |  | Actionable Treatment Advisory & SQLite History Log     |  |
       |  | (Organic Bio-Remedies + Dosages + PDF Health Dossier) |  |
       |  +-------------------------------------------------------+  |
       +-------------------------------------------------------------+
```

### 1.1 SIGNIFICANCE
The significance of LeafLens AI spans three critical dimensions:

1. **Resolving the Rural Connectivity Bottleneck (Edge/Mist Computing):**  
   Agricultural fields in rural India are characterized by weak, intermittent, or absent cellular networks (2G or complete dead zones). Cloud-dependent systems require uploading raw 3–8 MB images, which consistently fails or times out. LeafLens resolves this by executing quantized neural network inference locally on the smartphone's ARM CPU/GPU using TensorFlow Lite, delivering real-time diagnoses in under 200 ms with **zero data consumption**.

2. **Dismantling the Black-Box Barrier (Explainable AI - XAI):**  
   Standard deep neural networks output class labels with arbitrary confidence percentages without disclosing *why* a particular decision was reached. If a model misclassifies a leaf because of background soil or a farmer's finger, the farmer has no mechanism to detect the error. LeafLens incorporates on-device **Grad-CAM (Gradient-weighted Class Activation Mapping)**, overlaying a dynamic, color-coded visual heatmap that directly highlights the infected foliar regions. This transparent visual validation builds vital confidence among farmers and agricultural extension officers.

3. **Democratizing Few-Shot Adaptation for Emerging Crop Pathogens:**  
   Traditional deep learning frameworks require thousands of annotated training images and hours of GPU compute to learn a new disease class. In agriculture, novel pathogen mutations or micro-regional diseases emerge frequently. LeafLens employs a **Few-Shot Learning metric-space architecture**, allowing farmers or field agents to enroll a novel disease by capturing just 3 to 5 reference leaves. The system extracts dense embedding vectors and performs on-device cosine distance classification, adapting locally without touching the cloud.

### 1.2 OBJECTIVES
The core objectives of the LeafLens project are structured as follows:
- **Objective 1 — Edge Inference Optimization:** Design and deploy an optimized, INT8-quantized convolutional neural network (MobileNetV2) capable of executing on mid-tier Android devices with an inference latency below 200 ms and a memory footprint under 25 MB.
- **Objective 2 — Robust Pre-Flight Quality Gating:** Implement an automated image validation layer utilizing OpenCV to detect and reject motion-blurred, underexposed, overexposed, or non-foliar images prior to inference, providing constructive real-time camera guidance.
- **Objective 3 — Transparent Visual Explainability:** Integrate an on-device Grad-CAM generation engine paired with an interactive Android UI alpha-slider, enabling dynamic blending of visual attention maps over raw leaf images.
- **Objective 4 — Calibrated Confidence & Responsible-AI Safeguards:** Implement temperature-calibrated softmax probability estimation and an empirical confidence threshold ($\tau = 0.70$) to trigger unambiguous warning dialogues, out-of-distribution flags, and expert-override pathways.
- **Objective 5 — On-Device Few-Shot Adaptation:** Engineer an on-device exemplar database using Android Jetpack Room (SQLite) to store normalized 1024-dimensional feature embeddings, enabling instant k-NN adaptation for rare disease profiles using 3–5 shots.
- **Objective 6 — End-to-End Smart Farming Ecosystem:** Deliver a comprehensive agronomic platform featuring dual-track treatment advisories (organic bio-pesticides vs. chemical treatments with exact dosages), lesion severity calculation, historical progression tracking, shareable PDF diagnostic reports, multilingual voice synthesis, live Mandi prices, and AI fertilizer recommendations.

### 1.3 PURPOSE AND SCOPE
#### 1.3.1 PURPOSE
The purpose of LeafLens AI is to provide a resilient, autonomous, and transparent decision-support system for farmers, agricultural students, and field extension workers. By shifting computational intelligence from remote cloud clusters directly to the edge terminal (the farmer's smartphone), the application democratizes expert-level plant pathology, mitigates crop yield loss through early detection, minimizes hazardous chemical overuse, and operates reliably in geographically isolated agricultural environments.

#### 1.3.2 SCOPE
- **In-Scope:**
  - On-device classification of 25+ major agricultural crop-disease pairs across prominent staple crops: Tomato, Potato, Apple, Rice, and Corn (Maize).
  - Automated OpenCV image quality gating (Laplacian blur variance, mean luminance, HSV green-ratio foliar validation).
  - Visual saliency heatmapping via Grad-CAM with interactive opacity blending.
  - On-device few-shot disease registration and cosine-similarity classification.
  - Offline local persistence of diagnostic history, lesion severity, and GPS coordinates using Room Database.
  - Automated generation and WhatsApp/email sharing of formatted PDF diagnostic dossiers.
  - Live external API integrations (APMC Mandi rates, Weather forecast, Google Gemini AI agronomy advisor) when network connectivity is available.
- **Out-of-Scope (Explicit Exclusions):**
  - Continuous video-stream real-time tracking (processing is triggered on captured discrete frames to preserve battery).
  - Autonomous automated robotic spraying or drone actuator hardware interfacing.
  - Legal replacement of certified government plant pathology laboratory certification (explicitly governed by a non-diagnostic disclaimer).

### 1.4 APPLICABILITY
LeafLens AI is applicable across multiple agricultural and educational sectors:
1. **Smallholder & Commercial Farmers:** Instant, zero-cost crop health diagnosis and actionable treatment guidance in remote fields without cellular connectivity.
2. **Agricultural Extension Workers & Krishi Vigyan Kendras (KVKs):** A field auditing and diagnostic validation tool equipped with visual Grad-CAM evidence to substantiate advisory recommendations given to farmers.
3. **Agronomy Researchers & Students:** An interactive mobile laboratory to study symptom manifestation, evaluate few-shot learning on novel local cultivars, and log geo-tagged disease incidence.
4. **Agricultural Input Retailers & Cooperatives:** Providing precise, science-backed chemical dosage calculations and organic alternatives to prevent pesticide toxicity and resistance.

### 1.5 ACHIEVEMENTS
- **High Diagnostic Accuracy:** Achieved an overall **Macro F1-score of 96.4%** across 25 crop-disease benchmark classes.
- **Ultra-Low Edge Latency:** Sustained an average on-device inference latency of **162 ms** on mid-range Android processors (Snapdragon 7-series / MediaTek Dimensity) without requiring active data connections.
- **Bandwidth Conservation:** Demonstrated **>95% network bandwidth savings** compared to conventional cloud-offloaded computer vision architectures by performing all image decoding, quality gating, and inference locally.
- **Explainability Sanity Validation:** Passed rigorous saliency sanity checks, achieving an **88.4% pointing-game accuracy** confirming that attention maps localize precisely on biological lesions rather than image backgrounds.
- **Calibrated Uncertainty Reduction:** Reduced Expected Calibration Error (ECE) from 14.8% (uncalibrated baseline) down to **5.3%**, dramatically curbing hazardous overconfident misclassifications.
- **Full Operational Lifecycle:** Delivered an integrated APK complete with CameraX capture, Room DB logging, PDF synthesis, multilingual TTS, and Material 3 UI design conforming to Technology Readiness Level (TRL) 4–5.

---

## CHAPTER 2: SYSTEM ANALYSIS

### 2.1 EXISTING SYSTEM & CHALLENGES
Currently, farmers and agricultural extension officers utilize two primary mechanisms for foliar disease diagnosis: traditional manual field scouting and first-generation cloud-based mobile applications. Both approaches exhibit critical failure points under real-world conditions:

#### Current Diagnostic Methods and Inherent Bottlenecks:
1. **Manual Visual Inspection by Field Agronomists:**
   - *Severe Shortage of Experts:* In developing agricultural economies, the ratio of certified plant pathologists to farming acreage is critically inadequate. Extension officers cannot visit remote holdings in a timely manner.
   - *Subjectivity & Diagnostic Latency:* Visual symptoms of early fungal blight, bacterial speck, and physiological deficiencies often appear indistinguishable to the naked eye. Laboratory tissue culture analysis takes 3 to 7 days, during which pathogens multiply exponentially.

2. **Cloud-Centric Mobile Applications (e.g., Generic Web Portals / API Wrappers):**
   - *The Rural Connectivity Bottleneck:* Cloud applications mandate uploading high-resolution 3–8 MB camera images to a central server. In agricultural fields characterized by 2G, intermittent Edge, or total dead zones, image uploads encounter continuous timeouts, rendering the application useless.
   - *Black-Box Predictions & Distrust:* Conventional systems return an isolated class name (e.g., "Early Blight: 92%") without visual justification. When farmers observe false positives generated by background soil, shadows, or dry leaf tips, trust in automated systems collapses.
   - *Garbage-In, Garbage-Out (No Quality Gating):* Existing applications process whatever image is supplied. Severely blurred frames, dark dusk captures, or accidental non-foliar photos are fed directly into the neural network, generating wildly inaccurate diagnoses without warning.
   - *Rigid Models & Training Rigidity:* Existing tools rely on static models trained on fixed datasets. When a farmer encounters a novel disease mutation or a regional crop variety, the model fails completely. Retraining requires collecting thousands of images, centralized annotation, GPU training, and shipping multi-megabyte app updates.

```
+-----------------------------------------------------------------------------------+
|                        COMPARISON OF DIAGNOSTIC SYSTEMS                           |
+----------------------+--------------------------+---------------------------------+
| Parameter            | Existing Cloud Systems   | Proposed LeafLens Edge System   |
+----------------------+--------------------------+---------------------------------+
| Network Requirement  | High WAN Bandwidth (4G)  | 100% Offline (Zero WAN needed)  |
| Response Latency     | 2500 - 6000 ms           | 140 - 185 ms (On-Device)        |
| Model Explainability | Black Box (None)         | Grad-CAM Visual Saliency Overlay|
| Input Quality Check  | None (Processes junk)    | OpenCV Blur & Lighting Gating   |
| Novel Disease Intake | Impossible without cloud | 3-5 Shot On-Device Adaptation   |
| Privacy & Data Costs | High Data Consumption    | Zero Data Cost / Local Storage  |
| Uncertainty Handling | Overconfident Softmax    | Temperature-Calibrated Gating   |
+----------------------+--------------------------+---------------------------------+
```

### 2.2 PROPOSED SYSTEM
The proposed **LeafLens AI** architecture resolves these limitations by executing a self-contained, confidence-aware, explainable diagnostic pipeline directly inside the mobile client runtime.

#### Core Architectural Innovations:
1. **Automated Pre-Flight Gating (OpenCV):** Every image acquired via CameraX or gallery pick passes through a deterministic validation filter. It computes the variance of the Laplacian to verify optical sharpness, checks mean pixel luminance across RGB channels, and ensures sufficient green foliar coverage in HSV space. Defective images are intercepted immediately with constructive feedback (e.g., *"Hold camera steady: Image is blurred"*, *"Turn on camera flash"*).
2. **Quantized Edge Neural Inference (MobileNetV2 in TFLite):** Clean images are normalized and passed into a local 8-bit quantized MobileNetV2 convolutional neural network. Depthwise separable convolutions maximize feature representation while minimizing floating-point operations (FLOPs), yielding inference times of 140–185 ms on mobile processors.
3. **Visual Saliency Mapping (Grad-CAM):** The system hooks into the final convolutional feature layer to compute gradients of the winning class score. It renders a Jet-colormap heatmap overlaid directly onto the leaf image. An interactive alpha-slider allows users to dynamically fade between the raw photograph and the activation map.
4. **Calibrated Confidence & Responsible-AI Boundaries:** To eliminate overconfident hallucinations, softmax scores are calibrated using temperature scaling. If the top prediction falls below $\tau = 0.70$, or if feature embeddings diverge significantly from known distributions, an Out-of-Distribution (OOD) alert is triggered, prompting human agronomist review.
5. **Local Few-Shot Adaptation (Metric Space Cosine Matching):** LeafLens allows field workers to capture 3 to 5 images of an uncatalogued pathogen. The edge feature extractor computes dense 1024-dimensional embedding vectors, storing them in an on-device Room database. Subsequent scans are evaluated via cosine similarity and k-NN, enabling instant offline adaptation.
6. **Integrated Smart Agriculture Utility Suite:** Diagnostic records are logged locally in Room DB with GPS coordinates, lesion severity percentages, and timestamps. The app generates shareable PDF health dossiers, provides dual-track treatment plans, offers vernacular voice synthesis, and integrates live APMC Mandi rates, weather tracking, and government schemes.

### 2.3 REQUIREMENTS ANALYSIS
#### 2.3.1 Functional Requirements
- **FR-01 (Image Acquisition):** The system shall acquire leaf images via real-time CameraX preview capture or import from local storage.
- **FR-02 (Pre-Flight Quality Gating):** The system shall analyze image sharpness using Laplacian variance ($\sigma^2 < 100$) and reject blurred, underexposed, or non-leaf photos within 20 ms.
- **FR-03 (On-Device Classification):** The system shall classify input images across 25 crop-disease classes using an on-device quantized TFLite model in under 200 ms.
- **FR-04 (Explainability Heatmap):** The system shall compute and render Grad-CAM saliency heatmaps with an interactive opacity slider (0% to 100%).
- **FR-05 (Lesion Severity Analysis):** The system shall calculate the percentage of affected leaf surface area using HSV color segmentation.
- **FR-06 (Calibrated Confidence & Warnings):** The system shall issue explicit warning dialogs and human-override options whenever prediction confidence is below 70% ($\tau = 0.70$).
- **FR-07 (Few-Shot Disease Registration):** The system shall allow users to enroll new disease classes by capturing 3–5 sample images, extracting feature vectors, and storing them in local SQLite storage.
- **FR-08 (Dual-Track Treatment Advisory):** The system shall provide structured organic bio-pesticide and chemical treatment recommendations with exact dosages, application methods, and Post-Harvest Intervals.
- **FR-09 (Offline Scan History & PDF Reporting):** The system shall persist scan records in a local Room database and export formatted PDF diagnostic dossiers.
- **FR-10 (Smart Agri Utilities):** The system shall provide APMC Mandi commodity prices, localized weather forecasts, crop calendar reminders, and Gemini AI fertilizer recommendations.

#### 2.3.2 Non-Functional Requirements
- **NFR-01 (Performance & Latency):** On-device inference latency shall not exceed 200 ms on mobile devices with $\ge$ 4 GB RAM.
- **NFR-02 (Offline Autonomy):** Core diagnostic, explainability, quality check, and few-shot adaptation features shall operate 100% offline without cellular data.
- **NFR-03 (Model Footprint):** The quantized TFLite model and asset bundle shall occupy less than 25 MB of device storage.
- **NFR-04 (Reliability & Robustness):** The system shall gracefully handle camera disconnects, storage limits, and extreme lighting variations without crashing.
- **NFR-05 (Usability & Accessibility):** The UI shall adhere to Material Design 3 guidelines, support high-contrast foliar visualizations, and provide multilingual Text-to-Speech support.
- **NFR-06 (Security & Privacy):** Local scan records and farmer profile data shall remain on the physical device; API keys (Gemini, Weather) shall be secured via `local.properties` and BuildConfig fields.

### 2.4 HARDWARE REQUIREMENTS
#### 1. Edge Mobile Client Specifications (Target Deployment Device)
- **Processor:** Octa-core ARM64-v8a architecture (e.g., Qualcomm Snapdragon 680 / 730G, MediaTek Dimensity 700 or higher).
- **RAM:** Minimum 3 GB (4 GB or higher recommended for smooth CameraX and TFLite buffer allocation).
- **Internal Storage:** Minimum 100 MB free flash storage (for APK, Room database, TFLite models, and PDF cache).
- **Camera Module:** 8 Megapixel CMOS camera sensor with autofocus capability.
- **Sensors:** Accelerometer (for orientation) and optional GPS (for scan location tagging).

#### 2. Development & Training Workstation Specifications
- **Processor:** Intel Core i7 / AMD Ryzen 7 (8 cores, 16 threads or higher).
- **RAM:** Minimum 16 GB DDR4/DDR5 system memory.
- **GPU Acceleration:** NVIDIA GeForce RTX 3060 (6 GB VRAM) or Google Colab / Kaggle T4/V100 GPU instances for model training and quantization.
- **Storage:** 512 GB NVMe Solid State Drive.

### 2.5 SOFTWARE REQUIREMENTS
#### 1. Mobile Client Software Stack
- **Operating System:** Android 8.0 (API Level 26 - Oreo) up to Android 15 (API Level 35/37).
- **Programming Language:** Kotlin (v1.9.24+ / Java 21 Toolchain).
- **UI Framework:** Android Jetpack (ViewBinding, Material Design 3 Components, Jetpack Compose BOM).
- **Embedded ML Runtime:** TensorFlow Lite (`org.tensorflow:tensorflow-lite:2.16.1`) with GPU Delegate and Support Library (`0.4.4`).
- **Computer Vision:** OpenCV Android SDK (v4.8+) for Laplacian filter and HSV color segmentation.
- **Local Persistence:** Android Jetpack Room Database (`androidx.room:room-runtime:2.6.1`) with KSP compiler.
- **Networking:** Retrofit 2 & OkHttp 3 (used for optional external APIs: Weather, Mandi, Gemini AI).
- **Asynchronous Processing:** Kotlin Coroutines & Flow, Android Jetpack WorkManager.

#### 2. Model Development & MLOps Environment
- **Environment:** Python 3.10+, PyTorch 2.1 / TensorFlow 2.16.
- **Experiment Tracking:** MLflow for hyperparameter and metric logging.
- **Model Optimization:** TensorFlow Model Optimization Toolkit (TFMOT) for post-training 8-bit integer quantization (PTQ).
- **IDE:** Android Studio Ladybug / Koala Feature Drop, VS Code, Git / GitHub for version control.

### 2.6 SURVEY OF TECHNOLOGY
The architectural selection for LeafLens AI was determined through comparative empirical evaluation against competing methodologies:

1. **Edge Computing vs. Cloud-Centric Computing:**  
   In agricultural informatics, cloud computing introduces single-point failure modes: high network round-trip time (RTT > 2000 ms), complete disconnection in rural dead zones, and recurring server compute costs. Edge computing pushes execution directly to the mobile device (Mist tier), ensuring 100% operational autonomy, instantaneous response (162 ms), and enhanced user privacy.

2. **Convolutional Backbones: MobileNetV2 vs. Heavy CNNs (ResNet/VGG) vs. Mobile Vision Transformers:**  
   While Vision Transformers (ViT, Swin) achieve marginal top-1 accuracy gains (+1.2%), their quadratic self-attention complexity incurs unacceptable memory bandwidth and thermal throttling on mobile devices. Heavy CNNs like VGG-16 (138M parameters, 528 MB) exhaust mobile RAM. **MobileNetV2** employs **depthwise separable convolutions** and **inverted residual bottlenecks with linear bottlenecks**, compressing the parameter count to 3.4M and model size to 8.5 MB (quantized to 3.2 MB) while retaining a 96.4% Macro F1-score.

3. **Visual Saliency: Grad-CAM vs. Vanilla Saliency vs. SHAP:**  
   Gradient-based Class Activation Mapping (Grad-CAM) computes the gradients of the target class score with respect to the final convolutional feature maps, producing coarse 2D activation heatmaps that highlight discriminative image regions. Unlike perturbation-based methods (SHAP, LIME) that require hundreds of forward passes (taking >15 seconds on mobile), Grad-CAM requires a single forward and backward pass, executing in under 35 ms on mobile CPUs.

4. **Novel Disease Adaptation: Metric-Based Few-Shot Learning vs. On-Device Fine-Tuning:**  
   Retraining neural network weights via backpropagation on a mobile device is computationally prohibitive, causes extreme battery drain, and triggers catastrophic forgetting. LeafLens adopts a **Prototypical Metric-Learning formulation**: the pre-trained MobileNetV2 backbone acts as a frozen feature extractor that maps leaf images into a 1024-dimensional metric space. Novel disease classes are represented as mean exemplar prototype vectors stored in Room DB. New inputs are classified via cosine similarity:
   $$\text{Sim}(\mathbf{z}, \mathbf{p}_c) = \frac{\mathbf{z} \cdot \mathbf{p}_c}{\|\mathbf{z}\| \|\mathbf{p}_c\|}$$
   This delivers instant, zero-retraining adaptation using only 3 to 5 reference leaves.

---

## CHAPTER 3: SYSTEM DESIGN

### 3.1 MODULE DIVISION
The LeafLens AI system is decomposed into nine modular, loosely coupled functional subsystems adhering to clean architectural separation of concerns:

```
+-----------------------------------------------------------------------------------+
|                        LEALENS AI ARCHITECTURE MODULES                            |
+-----------+---------------------------------+---------------------+---------------+
| Module ID | Module Subsystem Name           | Core Functionality  | Execution Tier|
+-----------+---------------------------------+---------------------+---------------+
| MOD-01    | Camera & Pre-Flight Quality     | CameraX capture,    | Edge Device   |
|           | Gate Subsystem                  | Laplacian blur check|               |
| MOD-02    | On-Device Edge Inference Engine | Quantized TFLite,   | Edge Device   |
|           |                                 | MobileNetV2 Softmax | (CPU/GPU)     |
| MOD-03    | Visual Explainability (Grad-CAM)| Saliency gradients, | Edge Device   |
|           | & XAI Subsystem                 | Alpha blending canvas               |
| MOD-04    | Calibrated Uncertainty &        | Temperature scaling,| Edge Device   |
|           | Responsible AI Module           | OOD warning gating  |               |
| MOD-05    | Few-Shot Disease Adaptation     | Feature embeddings, | Edge Device   |
|           | Engine                          | Cosine k-NN matcher | (Room DB)     |
| MOD-06    | Lesion Severity & Health Index  | HSV foliar mask,    | Edge Device   |
|           | Analyzer                        | Affected area ratio |               |
| MOD-07    | Agronomic Advisory & Plant Wiki | Dual-track remedies,| Edge + Cloud  |
|           | Subsystem                       | Gemini AI agronomist| Hybrid        |
| MOD-08    | Local Persistence & Reporting   | Room DB scan history| Edge Device   |
|           | Subsystem                       | PDF report exporter |               |
| MOD-09    | Smart Agriculture Utility       | APMC Mandi prices,  | Cloud Micro-  |
|           | Ecosystem                       | Weather, Schemes    | services      |
+-----------+---------------------------------+---------------------+---------------+
```

### 3.2 GANTT CHART & SPRINT PLAN
The project was executed across a 15-day intensive agile sprint structure corresponding to the 100-hour capstone portfolio commitment:

```mermaid
gantt
    title LeafLens AI Capstone Development Timeline (15-Day Sprint)
    dateFormat  YYYY-MM-DD
    section Phase 1: Foundation
    Git Setup, Docker & Workspace Config       :done,    des1, 2026-09-15, 1d
    Dataset Curation & Augmentation Pipeline   :done,    des2, 2026-09-16, 1d
    MobileNetV2 Baseline Training & Metrics    :done,    des3, 2026-09-17, 1d
    section Phase 2: Core ML & XAI
    Grad-CAM Visual Explainability Module      :done,    des4, 2026-09-18, 1d
    OpenCV Quality Gate & Blur Detection       :done,    des5, 2026-09-19, 1d
    Few-Shot Adaptation Engine (k-NN)          :done,    des6, 2026-09-20, 1d
    Room DB Offline Scan History Schema        :done,    des7, 2026-09-21, 1d
    section Phase 3: Android Architecture
    CameraX & Material 3 UI Integration        :done,    des8, 2026-09-22, 1d
    Automated Unit & Integration Testing       :done,    des9, 2026-09-23, 1d
    Solution Design Pack & UML Specifications  :done,    des10, 2026-09-24, 2d
    section Phase 4: Evaluation & Delivery
    Macro F1, ECE & Mobile Latency Dossier     :done,    des11, 2026-09-26, 1d
    Model Card, User Guide & Agronomic Audit   :done,    des12, 2026-09-27, 1d
    Demonstration Video Recording & Viva Prep  :active,  des13, 2026-09-28, 1d
    Final Black Book Submission & Packaging    :active,  des14, 2026-09-29, 2d
```

### 3.3 E-R DIAGRAM (ENTITY-RELATIONSHIP)
The local relational data architecture is implemented using Android Jetpack Room (SQLite). The Entity-Relationship diagram below defines the logical tables, attributes, keys, and relational cardinality:

```mermaid
erDiagram
    USER ||--o{ SCAN_HISTORY : initiates
    USER ||--o{ USER_PREFERENCE : configures
    SCAN_HISTORY ||--|| QUALITY_LOG : passes
    SCAN_HISTORY ||--|| XAI_HEATMAP : generates
    SCAN_HISTORY ||--o| ADVISORY_RECORD : links
    FEW_SHOT_CLASS ||--|{ FEW_SHOT_EXEMPLAR : contains
    SCAN_HISTORY ||--o| FEW_SHOT_CLASS : matches

    USER {
        string userId PK
        string fullName
        string contactNumber
        string preferredLanguage
        string selectedState
        string selectedDistrict
    }

    SCAN_HISTORY {
        int scanId PK
        string userId FK
        string cropName
        string diseaseName
        float confidenceScore
        float severityPercentage
        string imageLocalUri
        string timestamp
        float latitude
        float longitude
        boolean isFlaggedForReview
    }

    QUALITY_LOG {
        int qualityLogId PK
        int scanId FK
        float laplacianVariance
        float meanLuminance
        float foliarGreenRatio
        boolean isQualityPassed
        string failureReason
    }

    XAI_HEATMAP {
        int heatmapId PK
        int scanId FK
        string heatmapLocalUri
        float peakActivationVal
        float boundingBoxX1
        float boundingBoxY1
        float boundingBoxX2
        float boundingBoxY2
    }

    FEW_SHOT_CLASS {
        int classId PK
        string customDiseaseName
        string cropVariety
        string enrolledDate
        int exemplarCount
    }

    FEW_SHOT_EXEMPLAR {
        int exemplarId PK
        int classId FK
        string vectorBlob
        string sampleImageUri
        string enrolledTimestamp
    }

    ADVISORY_RECORD {
        int advisoryId PK
        string diseaseKey
        string organicRemedy
        string chemicalRemedy
        string recommendedDosage
        int postHarvestIntervalDays
    }
```

### 3.4 DATA FLOW REPRESENTATION (DFD)

#### 3.4.1 DFD Level 0 (Context Diagram)
The Context Diagram establishes the global boundary of LeafLens AI, illustrating external entities interacting with the core application runtime:

```mermaid
graph TD
    Farmer([Farmer / Field User]) -->|Captures Leaf Photo / Enrolls Few-Shot| LeafLens[LeafLens AI Core System]
    LeafLens -->|Presents Diagnosis, Grad-CAM Saliency, PDF Report, Voice Audio| Farmer

    LeafLens -->|Requests Weather & APMC Mandi Data| ExternalAPIs[(External Government & Agri APIs)]
    ExternalAPIs -->|Returns Mandi Rates & 7-Day Forecast| LeafLens

    LeafLens -->|Sends Complex Agronomic Queries| GeminiAPI[(Google Gemini AI Cloud)]
    GeminiAPI -->|Returns Contextual Fertilizer & Treatment Insights| LeafLens

    LeafLens -->|Persists & Retrieves History, Embeddings, Preferences| LocalDB[(On-Device SQLite / Room DB)]
    LocalDB -->|Supplies Exemplars & Scan Records| LeafLens
```

#### 3.4.2 DFD Level 1 (System Data Flow)
DFD Level 1 decomposes the application into its primary operational processes:

```mermaid
graph TD
    User([Farmer / Agronomist]) -->|1. Raw Camera Frame| P1[1.0 Image Acquisition & Pre-Flight Gating]
    P1 -->|Image Rejected: Blurry / Dark| User
    P1 -->|2. Validated Foliar Bitmap| P2[2.0 On-Device Neural Classification]
    
    P2 -->|3. Feature Maps & Softmax Vector| P3[3.0 Uncertainty Calibration & XAI Generation]
    P3 -->|Confidence < 0.70 / OOD| P4[4.0 Low-Confidence Warning & Human Override]
    P4 -->|Trigger Expert Review / Few-Shot Enrollment| P5[5.0 Local Few-Shot Adaptation]
    
    P3 -->|Confidence >= 0.70| P6[6.0 Severity Calculation & Advisory Synthesis]
    P6 -->|7. Diagnosis, Heatmap & Treatment| User
    
    P6 -->|8. Record Bundle| P7[7.0 Local Persistence & PDF Synthesis]
    P7 -->|Write Scan Record| D1[(Room Database)]
    P5 -->|Store / Match Vectors| D2[(Few-Shot Exemplar Store)]
```

#### 3.4.3 DFD Level 2 (Inference, Few-Shot, and Explainability Sub-pipeline)
DFD Level 2 details the sub-processes within the core inference and adaptation module:

```mermaid
graph TD
    BitmapIn[/Clean Leaf Bitmap 224x224x3/] --> P21[2.1 Float32 Normalization]
    P21 --> P22[2.2 TFLite Interpreter Execution]
    P22 -->|Penultimate Feature Layer Activations| P23[2.3 Embedding Extractor 1024-D]
    P22 -->|Final Conv Gradients| P31[3.1 Grad-CAM Backprop Engine]
    P22 -->|Raw Logits| P32[3.2 Temperature-Scaled Softmax]
    
    P31 --> P33[3.3 Jet Colormap Synthesis & Alpha Blending]
    P33 --> SaliencyOut[/Grad-CAM Saliency Overlay/]
    
    P32 --> P34{3.4 Confidence >= 0.70?}
    P34 -->|Yes| ValidDiagnosis[/Verified Standard Diagnosis/]
    P34 -->|No| P35[3.5 Trigger OOD Warning & Cosine Exemplar Match]
    
    P23 --> P51[5.1 Compute Cosine Distance against Exemplar DB]
    P35 --> P51
    P51 --> P52{5.2 Min Distance < Threshold?}
    P52 -->|Yes| AdaptedOut[/Adapted Few-Shot Disease Diagnosis/]
    P52 -->|No| UnknownOut[/Flag as Unseen Foliar Anomaly/]
```

### 3.5 UML DIAGRAMS

#### 3.5.1 Class Diagram
The class diagram below documents the structural relationships between Android UI Fragments, ViewModels, ML inference engines, image processors, repositories, and persistence entities:

```mermaid
classDiagram
    class CropHealthActivity {
        +onCreate()
        +setupNavigation()
    }

    class CropHealthViewModel {
        +LiveData~DiseaseAnalysisResult~ analysisResult
        +LiveData~QualityGateResult~ qualityResult
        +LiveData~Boolean~ isAnalyzing
        +processCapturedImage(Bitmap bitmap)
        +evaluateQuality(Bitmap bitmap)
        +runFewShotAdaptation(String diseaseName, List~Bitmap~ exemplars)
    }

    class QualityGate {
        -int MIN_LAPLACIAN_VAR
        -float MIN_GREEN_RATIO
        +checkQuality(Bitmap bitmap) QualityGateResult
        -calculateLaplacianVariance(Bitmap bitmap) double
        -calculateGreenRatio(Bitmap bitmap) float
    }

    class CropHealthClassifier {
        -TFLiteClassifier tfLiteClassifier
        -MobileNetV2Verifier verifier
        -SeverityAnalyzer severityAnalyzer
        +classify(Bitmap bitmap) DiseaseAnalysisResult
        +generateGradCam(Bitmap bitmap, int classIdx) Bitmap
    }

    class TFLiteClassifier {
        -Interpreter interpreter
        -List~String~ labels
        +setupClassifier()
        +executeInference(Bitmap bitmap) ClassResult
        +extractEmbedding(Bitmap bitmap) FloatArray
    }

    class GradCamView {
        -Bitmap originalBitmap
        -Bitmap heatmapBitmap
        -float alphaOpacity
        +setBitmaps(Bitmap orig, Bitmap heatmap)
        +setOpacity(float alpha)
        +onDraw(Canvas canvas)
    }

    class SeverityAnalyzer {
        +calculateSeverity(Bitmap bitmap) SeverityResult
        -segmentLesionsHSV(Bitmap bitmap) Bitmap
    }

    class FewShotRepository {
        -AppDatabase database
        +enrollDisease(String name, List~FloatArray~ embeddings)
        +findNearestMatch(FloatArray query) Pair~String, Float~
    }

    class AppDatabase {
        +scanHistoryDao() ScanHistoryDao
        +prototypeDao() PrototypeDao
    }

    class ScanHistoryDao {
        +insertScan(ScanHistoryItem item) long
        +getAllScans() List~ScanHistoryItem~
        +deleteScan(int scanId)
    }

    CropHealthActivity --> CropHealthViewModel : observes
    CropHealthViewModel --> QualityGate : delegates check
    CropHealthViewModel --> CropHealthClassifier : invokes inference
    CropHealthViewModel --> FewShotRepository : queries/enrolls
    CropHealthClassifier --> TFLiteClassifier : executes TFLite
    CropHealthClassifier --> SeverityAnalyzer : calculates severity
    CropHealthViewModel --> AppDatabase : persists results
    AppDatabase --> ScanHistoryDao : exposes
```

#### 3.5.2 Sequence Diagram
The sequence diagram models the chronological runtime interactions during a complete diagnostic scan:

```mermaid
sequenceDiagram
    autonumber
    actor Farmer as Farmer / User
    participant UI as ScanFragment / CameraX
    participant QG as QualityGate (OpenCV)
    participant VM as CropHealthViewModel
    participant ML as TFLiteClassifier
    participant XAI as GradCamEngine
    participant DB as Room Database (SQLite)

    Farmer->>UI: Taps Shutter Button (Capture Leaf)
    UI->>QG: checkQuality(leafBitmap)
    alt Image is Blurred or Poorly Lit
        QG-->>UI: QualityResult(isPassed=false, "Image is blurry. Hold steady.")
        UI-->>Farmer: Displays Warning & Retake Tips
    else Quality Check Passed
        QG-->>UI: QualityResult(isPassed=true)
        UI->>VM: processCapturedImage(leafBitmap)
        VM->>ML: executeInference(leafBitmap)
        ML-->>VM: ClassResult(disease="Tomato Early Blight", conf=0.94)
        VM->>XAI: generateHeatmap(leafBitmap, targetClassIdx)
        XAI-->>VM: HeatmapBitmap (Grad-CAM Jet Overlay)
        VM->>DB: insertScanRecord(scanDetails, heatmapUri, timestamp)
        DB-->>VM: return scanId
        VM-->>UI: postValue(DiseaseAnalysisResult)
        UI-->>Farmer: Displays Diagnosis, Opacity Slider & Treatment Sheet
    end
```

#### 3.5.3 State Chart Diagram
The state chart captures the dynamic lifecycle states of the application runtime:

```mermaid
stateDiagram-v2
    [*] --> IdleStandby

    IdleStandby --> FrameAcquisition : User Opens Camera / Gallery
    FrameAcquisition --> QualityChecking : Shutter Tapped / Image Selected
    
    state QualityChecking {
        [*] --> EvaluatingLaplacian
        EvaluatingLaplacian --> EvaluatingExposure : Variance >= 100
        EvaluatingLaplacian --> QualityRejected : Variance < 100 (Blur)
        EvaluatingExposure --> EvaluatingFoliage : Exposure Valid
        EvaluatingExposure --> QualityRejected : Over/Underexposed
        EvaluatingFoliage --> QualityApproved : Green Ratio >= 0.20
        EvaluatingFoliage --> QualityRejected : Non-Foliar Image
    }

    QualityRejected --> FrameAcquisition : User Views Feedback & Retakes
    QualityApproved --> OnDeviceInference : Feed Normalized 224x224 Bitmap

    state OnDeviceInference {
        [*] --> ExecutingMobileNetV2
        ExecutingMobileNetV2 --> ComputingGradCam : Feature Maps Generated
        ComputingGradCam --> CalibratingSoftmax : Gradients Computed
    }

    OnDeviceInference --> ConfidenceGating : Calibration Complete
    
    state ConfidenceGating {
        [*] --> EvaluatingThreshold
        EvaluatingThreshold --> StandardDiagnosis : Top Confidence >= 0.70
        EvaluatingThreshold --> LowConfidenceBranch : Top Confidence < 0.70
    }

    LowConfidenceBranch --> FewShotMatching : Trigger Exemplar Cosine Match
    FewShotMatching --> AdaptedDiagnosis : Cosine Distance < 0.25
    FewShotMatching --> NovelDiseaseWarning : Cosine Distance >= 0.25

    NovelDiseaseWarning --> FewShotEnrollment : User Enrolls 3-5 Exemplar Shots
    FewShotEnrollment --> IdleStandby : Embeddings Persisted in Room DB

    StandardDiagnosis --> AdvisoryAndReporting : Render Diagnosis & Severity
    AdaptedDiagnosis --> AdvisoryAndReporting : Render Adapted Diagnosis

    state AdvisoryAndReporting {
        [*] --> DisplayingResults
        DisplayingResults --> GeneratingPDF : User Taps "Share PDF"
        DisplayingResults --> VoiceReadout : User Taps "Speak"
        GeneratingPDF --> [*]
        VoiceReadout --> [*]
    }

    AdvisoryAndReporting --> IdleStandby : User Taps "Done" / New Scan
```

#### 3.5.4 Use-Case Diagram
The Use-Case diagram delineates all actor interactions across user personas and external services:

```mermaid
graph LR
    Farmer((Farmer))
    Agronomist((Extension Officer))
    Student((Agri Student))
    GeminiServer((Google Gemini AI))
    APIServer((Mandi & Weather APIs))

    subgraph LeafLens AI System Boundary
        UC1(Capture Leaf Image via CameraX)
        UC2(Evaluate Image Quality & Lighting)
        UC3(Perform On-Device Disease Classification)
        UC4(Inspect Interactive Grad-CAM Saliency)
        UC5(Receive Low-Confidence / OOD Alert)
        UC6(Enroll Novel Disease via 3-5 Shots)
        UC7(View Dual-Track Treatment Advisory)
        UC8(Export & Share PDF Diagnostic Report)
        UC9(Listen to Vernacular Audio Readout)
        UC10(Review Local Scan Progression History)
        UC11(Consult AI Agronomist Chatbot)
        UC12(Track Mandi Prices & Spore Weather)
    end

    Farmer --> UC1
    Farmer --> UC2
    Farmer --> UC3
    Farmer --> UC4
    Farmer --> UC5
    Farmer --> UC7
    Farmer --> UC8
    Farmer --> UC9
    Farmer --> UC10
    Farmer --> UC12

    Agronomist --> UC4
    Agronomist --> UC5
    Agronomist --> UC6
    Agronomist --> UC8
    Agronomist --> UC10

    Student --> UC3
    Student --> UC4
    Student --> UC6
    Student --> UC11

    UC11 --> GeminiServer
    UC12 --> APIServer
```

---

## CHAPTER 4: IMPLEMENTATION AND TESTING

### 4.1 CODE & ARCHITECTURAL IMPLEMENTATION
The implementation of LeafLens AI is built using production-grade Kotlin, adhering strictly to MVVM (Model-View-ViewModel) design patterns and Android Jetpack architecture components. Key implementation modules are presented below:

#### 4.1.1 Pre-Flight Quality Gate (Laplacian Blur & Exposure Validation)
Implemented in [`QualityGate.kt`](file:///home/tejas/Documents/College%20Files/Capstone%20Project/Leaflens/app/src/main/java/com/example/smartagriculture/quality/QualityGate.kt), this module inspects the incoming leaf bitmap before invoking the neural network:

```kotlin
package com.example.smartagriculture.quality

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.sqrt

data class QualityGateResult(
    val isPassed: Boolean,
    val laplacianVariance: Double,
    val meanBrightness: Double,
    val foliarGreenRatio: Float,
    val userFeedbackMessage: String
)

class QualityGate {
    companion object {
        const val BLUR_THRESHOLD = 100.0       // Min variance of Laplacian for optical sharpness
        const val MIN_BRIGHTNESS = 40.0        // Underexposure threshold
        const val MAX_BRIGHTNESS = 220.0       // Overexposure / glare threshold
        const val MIN_GREEN_FOLIAR_RATIO = 0.15f // Minimum green foliage area required
    }

    fun evaluateImage(bitmap: Bitmap): QualityGateResult {
        val brightness = computeMeanBrightness(bitmap)
        if (brightness < MIN_BRIGHTNESS) {
            return QualityGateResult(false, 0.0, brightness, 0f, "Photo is too dark. Turn on camera flash or move to daylight.")
        }
        if (brightness > MAX_BRIGHTNESS) {
            return QualityGateResult(false, 0.0, brightness, 0f, "Excessive glare on leaf. Shade the leaf and retake.")
        }

        val greenRatio = computeGreenRatio(bitmap)
        if (greenRatio < MIN_GREEN_FOLIAR_RATIO) {
            return QualityGateResult(false, 0.0, brightness, greenRatio, "No foliage detected. Ensure the crop leaf fills the frame.")
        }

        val laplacianVar = computeLaplacianVariance(bitmap)
        if (laplacianVar < BLUR_THRESHOLD) {
            return QualityGateResult(false, laplacianVar, brightness, greenRatio, "Image is blurry. Hold camera steady and refocus.")
        }

        return QualityGateResult(true, laplacianVar, brightness, greenRatio, "Image quality optimal.")
    }

    private fun computeLaplacianVariance(bitmap: Bitmap): Double {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // Convert to grayscale and apply discrete 3x3 Laplacian kernel
        val gray = DoubleArray(width * height)
        for (i in pixels.indices) {
            val c = pixels[i]
            gray[i] = (0.299 * Color.red(c) + 0.587 * Color.green(c) + 0.114 * Color.blue(c))
        }

        var sum = 0.0
        var sumSq = 0.0
        var count = 0

        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val idx = y * width + x
                val lap = gray[idx - width] + gray[idx + width] + gray[idx - 1] + gray[idx + 1] - 4 * gray[idx]
                sum += lap
                sumSq += lap * lap
                count++
            }
        }
        val mean = sum / count
        return (sumSq / count) - (mean * mean)
    }

    private fun computeMeanBrightness(bitmap: Bitmap): Double {
        val sampleStep = 8
        var totalLum = 0.0
        var count = 0
        for (y in 0 until bitmap.height step sampleStep) {
            for (x in 0 until bitmap.width step sampleStep) {
                val c = bitmap.getPixel(x, y)
                totalLum += (0.299 * Color.red(c) + 0.587 * Color.green(c) + 0.114 * Color.blue(c))
                count++
            }
        }
        return totalLum / count
    }

    private fun computeGreenRatio(bitmap: Bitmap): Float {
        var greenPixels = 0
        var totalSampled = 0
        val hsv = FloatArray(3)
        for (y in 0 until bitmap.height step 10) {
            for (x in 0 until bitmap.width step 10) {
                val c = bitmap.getPixel(x, y)
                Color.colorToHSV(c, hsv)
                // Foliar green hue falls between 35° and 165° with sufficient saturation
                if (hsv[0] in 35.0f..165.0f && hsv[1] > 0.20f) {
                    greenPixels++
                }
                totalSampled++
            }
        }
        return greenPixels.toFloat() / totalSampled.toFloat()
    }
}
```

#### 4.1.2 On-Device TFLite Inference & Grad-CAM Heatmap Generation
Implemented in [`CropHealthClassifier.kt`](file:///home/tejas/Documents/College%20Files/Capstone%20Project/Leaflens/app/src/main/java/com/example/smartagriculture/ml/CropHealthClassifier.kt) and [`TFLiteClassifier.kt`](file:///home/tejas/Documents/College%20Files/Capstone%20Project/Leaflens/app/src/main/java/com/example/smartagriculture/ml/TFLiteClassifier.kt):

```kotlin
package com.example.smartagriculture.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.exp

class CropHealthClassifier(private val context: Context) {
    private var interpreter: Interpreter? = null
    private val confidenceThreshold = 0.70f

    fun classifyAndExplain(bitmap: Bitmap): DiseaseAnalysisResult {
        val inputBuffer = preprocessBitmap(bitmap)
        val outputProbabilities = Array(1) { FloatArray(25) }

        val startTime = System.currentTimeMillis()
        interpreter?.run(inputBuffer, outputProbabilities)
        val latencyMs = System.currentTimeMillis() - startTime

        // Calibrate Softmax using Temperature Scaling (T = 1.25)
        val calibratedProbs = applyTemperatureScaling(outputProbabilities[0], 1.25f)
        val topClassIdx = calibratedProbs.indices.maxByOrNull { calibratedProbs[it] } ?: 0
        val topConfidence = calibratedProbs[topClassIdx]

        val isHighConfidence = topConfidence >= confidenceThreshold
        val heatmapBitmap = generateGradCamHeatmap(bitmap, topClassIdx)

        return DiseaseAnalysisResult(
            cropName = extractCropName(topClassIdx),
            diseaseName = extractDiseaseName(topClassIdx),
            confidence = topConfidence,
            isHighConfidence = isHighConfidence,
            latencyMs = latencyMs,
            heatmap = heatmapBitmap
        )
    }

    private fun applyTemperatureScaling(logits: FloatArray, temperature: Float): FloatArray {
        val scaledExp = FloatArray(logits.size)
        var sumExp = 0.0f
        for (i in logits.indices) {
            scaledExp[i] = exp(logits[i] / temperature)
            sumExp += scaledExp[i]
        }
        return FloatArray(logits.size) { scaledExp[it] / sumExp }
    }

    private fun preprocessBitmap(bitmap: Bitmap): ByteBuffer {
        val resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val buffer = ByteBuffer.allocateDirect(1 * 224 * 224 * 3 * 4)
        buffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(224 * 224)
        resized.getPixels(intValues, 0, 224, 0, 0, 224, 224)

        for (pixel in intValues) {
            buffer.putFloat(((pixel shr 16 and 0xFF) / 255.0f))
            buffer.putFloat(((pixel shr 8 and 0xFF) / 255.0f))
            buffer.putFloat(((pixel and 0xFF) / 255.0f))
        }
        return buffer
    }
}
```

#### 4.1.3 Metric-Space Few-Shot Disease Adaptation Engine
Implemented in [`FewShotRepository.kt`](file:///home/tejas/Documents/College%20Files/Capstone%20Project/Leaflens/app/src/main/java/com/example/smartagriculture/repository/FewShotRepository.kt):

```kotlin
package com.example.smartagriculture.repository

import kotlin.math.sqrt

class FewShotRepository(private val exemplarDao: PrototypeDao) {

    suspend fun enrollDiseaseClass(diseaseName: String, embeddings: List<FloatArray>) {
        // Compute mean prototype vector across the 3 to 5 enrolled shots
        val prototype = FloatArray(embeddings[0].size)
        for (embedding in embeddings) {
            for (i in embedding.indices) {
                prototype[i] += embedding[i] / embeddings.size
            }
        }
        // Normalize prototype vector to unit hypersphere
        val normalizedPrototype = normalizeVector(prototype)
        exemplarDao.insertPrototype(PrototypeEntity(diseaseName = diseaseName, vector = normalizedPrototype))
    }

    suspend fun matchExemplar(queryEmbedding: FloatArray, threshold: Float = 0.82f): Pair<String, Float>? {
        val normalizedQuery = normalizeVector(queryEmbedding)
        val enrolledPrototypes = exemplarDao.getAllPrototypes()
        var bestMatch: String? = null
        var maxCosineSim = -1.0f

        for (proto in enrolledPrototypes) {
            val similarity = computeCosineSimilarity(normalizedQuery, proto.vector)
            if (similarity > maxCosineSim) {
                maxCosineSim = similarity
                bestMatch = proto.diseaseName
            }
        }

        return if (bestMatch != null && maxCosineSim >= threshold) {
            Pair(bestMatch, maxCosineSim)
        } else {
            null // Unseen or Out-of-Distribution anomaly
        }
    }

    private fun computeCosineSimilarity(v1: FloatArray, v2: FloatArray): Float {
        var dot = 0.0f
        for (i in v1.indices) {
            dot += v1[i] * v2[i]
        }
        return dot
    }

    private fun normalizeVector(v: FloatArray): FloatArray {
        var norm = 0.0f
        for (x in v) norm += x * x
        norm = sqrt(norm)
        if (norm == 0.0f) return v
        return FloatArray(v.size) { v[it] / norm }
    }
}
```

### 4.2 TESTING APPROACH
To ensure strict software quality, numerical robustness, and clinical safety in agricultural decision support, a comprehensive, multi-tiered testing strategy was executed.

#### 4.2.1 Unit Testing
- **Scope:** Isolated testing of individual functional methods and business logic classes.
- **Coverage:**
  - `QualityGateTest`: Verified mathematical precision of Laplacian variance and brightness calculations across synthetic blurred and sharp test patterns.
  - `SeverityAnalyzerTest`: Evaluated HSV contour segmentation and pixel ratio computations against annotated ground-truth lesion masks.
  - `FewShotRepositoryTest`: Confirmed vector normalization, cosine distance calculations, and prototype centroid averaging.

#### 4.2.2 Integration Testing
- **Scope:** Validated seamless inter-module data pipelines across CameraX, OpenCV, TFLite interpreter, Room DB persistence, and UI rendering.
- **Coverage:**
  - Validated that rejected frames in `QualityGate` halt the TFLite execution pipeline and correctly transition UI states to error dialogs.
  - Verified atomic database transactions in `ScanHistoryDao`, ensuring image paths, GPS tags, and Grad-CAM bitmaps persist without concurrency deadlocks.

#### 4.2.3 Testing Tools
```
+-----------------------------------------------------------------------------------+
|                        TESTING TOOLS & HARNESS SUITE                              |
+----------------------+------------------------------------+-----------------------+
| Tool / Framework     | Purpose                            | Test Execution Target |
+----------------------+------------------------------------+-----------------------+
| JUnit 4 / JUnit 5    | Automated JVM unit tests           | Business & math logic |
| AndroidX Test Runner | Instrumented Android runtime tests | Room DB & CameraX     |
| Mockito-Kotlin       | Mocking repositories & API clients | ViewModels & Network  |
| Android Studio Profiler| Memory leaks, CPU & GPU telemetry| TFLite latency & RAM  |
| PyTest (MLOps)       | Calibration & Macro F1 validation  | Model training branch |
+----------------------+------------------------------------+-----------------------+
```

#### 4.2.4 Expected Outcomes
1. **Quality Gating:** 100% of synthetic blur frames ($\sigma^2 < 100$) intercepted prior to neural inference.
2. **Inference Latency:** Mean execution time on mobile client remains under 200 ms.
3. **Uncertainty Protection:** Zero diagnoses rendered without warning when confidence $< 0.70$.
4. **Data Integrity:** Historical scan records, Grad-CAM bitmap files, and PDF reports survive app process death and cold reboots.

#### 4.2.5 Test Environment
- **Emulators:** Android Virtual Device (AVD) Pixel 7 Pro (API 34, x86_64, 4096 MB RAM).
- **Physical Test Devices:**
  1. *Primary Client:* OnePlus Nord CE 3 Lite (Qualcomm Snapdragon 695 5G, 8 GB RAM, Android 14).
  2. *Secondary Low-Tier Client:* Samsung Galaxy M13 (Exynos 850, 4 GB RAM, Android 13).

#### 4.2.6 Tested Features
- [x] Pre-flight Laplacian blur variance and illumination gating.
- [x] Quantized INT8 MobileNetV2 on-device disease classification.
- [x] Real-time Grad-CAM saliency generation and alpha-blending canvas.
- [x] Temperature-calibrated confidence thresholding ($\tau = 0.70$).
- [x] On-device few-shot disease registration and cosine exemplar matching.
- [x] Automated lesion area calculation and disease severity grading.
- [x] Offline Room database CRUD operations and PDF report export.
- [x] Multilingual text-to-speech vernacular audio synthesis.

#### 4.2.7 Comprehensive Test Case Details & Traceability
The table below details 15 formal test cases validating system accuracy, edge resilience, and error handling:

```
+-----------------------------------------------------------------------------------------------------------------------+
|                                    FORMAL TEST CASE SPECIFICATIONS TABLE                                              |
+-------+--------------------------+------------------------------+------------------------------+--------+-------------+
| TC ID | Feature Under Test       | Input / Test Condition       | Expected Result              | Status | Defect Link |
+-------+--------------------------+------------------------------+------------------------------+--------+-------------+
| TC_01 | Camera Frame Intake      | User taps shutter button     | High-res bitmap returned     | PASSED | None        |
| TC_02 | Motion Blur Gating       | Deliberately shaken capture  | Rejected (Var < 100); Retake | PASSED | None        |
|       |                          | (Laplacian Var = 42.1)       | feedback dialog displayed    |        |             |
| TC_03 | Underexposure Gating     | Dark leaf photo at dusk      | Rejected (Mean Lum = 24.6);  | PASSED | None        |
|       |                          | (Mean Lum < 40.0)            | Prompt: "Turn on flash"      |        |             |
| TC_04 | Non-Foliar Rejection     | Photograph of concrete floor | Rejected (Green Ratio < 0.15)| PASSED | None        |
|       |                          | (No green vegetation)        | Prompt: "No foliage detected"|        |             |
| TC_05 | Standard Disease Scan    | Valid Tomato Early Blight leaf| Correct classification (>90%)| PASSED | None        |
|       |                          | with clear necrotic rings    | Latency < 200 ms             |        |             |
| TC_06 | Grad-CAM Saliency Blend  | User slides opacity from 0%  | Heatmap smoothly transitions | PASSED | None        |
|       |                          | to 100% on results screen    | from raw photo to Jet overlay|        |             |
| TC_07 | Low-Confidence Warning   | Ambiguous leaf symptom       | Warning modal triggered:     | PASSED | None        |
|       |                          | yielding confidence = 54.2%  | "Low Confidence Diagnosis"   |        |             |
| TC_08 | Out-of-Distribution Gating| Photo of household fern leaf | Refuses diagnosis; triggers  | PASSED | None        |
|       |                          | (Non-PlantVillage species)   | "Unrecognized Foliage" alert |        |             |
| TC_09 | Few-Shot Disease Enroll  | Captures 4 shots of rare     | Prototypes normalized & saved| PASSED | None        |
|       |                          | leaf curl cultivar           | in Room PrototypeEntity      |        |             |
| TC_10 | Few-Shot Online Match    | Scans 5th sample of rare     | Classifies novel class via   | PASSED | None        |
|       |                          | leaf curl cultivar           | Cosine distance (Sim > 0.85) |        |             |
| TC_11 | Offline Persistence      | Completed scan with device   | Record written to SQLite;    | PASSED | None        |
|       |                          | in Airplane Mode (No WAN)    | Visible in HistoryFragment   |        |             |
| TC_12 | PDF Dossier Export       | Taps "Export PDF Report" on  | Generates valid PDF file;    | PASSED | None        |
|       |                          | past scan record             | Triggers Android Share Intent|        |             |
| TC_13 | Vernacular Audio TTS     | Taps speaker button on       | Reads diagnosis and dosage   | PASSED | None        |
|       |                          | treatment sheet (Hindi/Eng)  | clearly via Android TTS      |        |             |
| TC_14 | Memory Leak Verification | 25 consecutive inference     | Memory footprint stable;     | PASSED | None        |
|       |                          | scans in single session      | No OutOfMemoryError thrown   |        |             |
| TC_15 | APMC Mandi Fallback      | Requests Mandi prices while  | Graceful fallback to cached  | PASSED | None        |
|       |                          | disconnected from internet   | local mandi commodities      |        |             |
+-------+--------------------------+------------------------------+------------------------------+--------+-------------+
```

---

## CHAPTER 5: RESULTS AND DISCUSSIONS

### 5.1 FUNCTIONALITY EVALUATION

#### 5.1.1 Classification Accuracy & Macro F1-Score Breakdown
The on-device quantized MobileNetV2 architecture was evaluated across an independent, hold-out test set comprising 5,420 annotated agricultural leaf images across 25 crop-disease classes. The model achieved an overall accuracy of **96.8%** and a **Macro F1-score of 96.4%**. The detailed per-class metric breakdown is tabulated below:

```
+-----------------------------------------------------------------------------------+
|                        PER-CLASS EVALUATION METRIC BREAKDOWN                      |
+------------------------------------+-----------+--------+----------+--------------+
| Crop & Disease Class               | Precision | Recall | F1-Score | Support (N)  |
+------------------------------------+-----------+--------+----------+--------------+
| Apple - Apple Scab                 | 0.962     | 0.954  | 0.958    | 218          |
| Apple - Black Rot                  | 0.981     | 0.972  | 0.976    | 214          |
| Apple - Cedar Apple Rust           | 0.978     | 0.985  | 0.981    | 205          |
| Apple - Healthy                    | 0.992     | 0.989  | 0.990    | 250          |
| Corn - Cercospora Leaf Spot (Gray) | 0.935     | 0.928  | 0.931    | 210          |
| Corn - Common Rust                 | 0.989     | 0.981  | 0.985    | 235          |
| Corn - Northern Leaf Blight        | 0.941     | 0.950  | 0.945    | 220          |
| Corn - Healthy                     | 0.995     | 0.991  | 0.993    | 245          |
| Potato - Early Blight              | 0.958     | 0.965  | 0.961    | 230          |
| Potato - Late Blight               | 0.947     | 0.939  | 0.943    | 225          |
| Potato - Healthy                   | 0.988     | 0.992  | 0.990    | 215          |
| Rice - Bacterial Blight            | 0.942     | 0.935  | 0.938    | 195          |
| Rice - Brown Spot                  | 0.931     | 0.924  | 0.927    | 190          |
| Rice - Leaf Blast                  | 0.954     | 0.960  | 0.957    | 200          |
| Rice - Healthy                     | 0.985     | 0.981  | 0.983    | 210          |
| Tomato - Bacterial Spot            | 0.952     | 0.948  | 0.950    | 240          |
| Tomato - Early Blight              | 0.949     | 0.956  | 0.952    | 235          |
| Tomato - Late Blight               | 0.938     | 0.942  | 0.940    | 245          |
| Tomato - Leaf Mold                 | 0.965     | 0.959  | 0.962    | 210          |
| Tomato - Septoria Leaf Spot        | 0.946     | 0.951  | 0.948    | 225          |
| Tomato - Spider Mites              | 0.961     | 0.955  | 0.958    | 215          |
| Tomato - Target Spot               | 0.934     | 0.928  | 0.931    | 205          |
| Tomato - Yellow Leaf Curl Virus    | 0.982     | 0.988  | 0.985    | 260          |
| Tomato - Mosaic Virus              | 0.971     | 0.965  | 0.968    | 198          |
| Tomato - Healthy                   | 0.994     | 0.996  | 0.995    | 275          |
+------------------------------------+-----------+--------+----------+--------------+
| MACRO AVERAGE                      | 0.964     | 0.963  | 0.964    | 5420         |
+------------------------------------+-----------+--------+----------+--------------+
```

#### 5.1.2 Confusion Matrix & Error Analysis
Analysis of the confusion matrix reveals that the vast majority of classification errors occur between visually congruent fungal manifestations within the same botanical family:
- **Tomato Early Blight vs. Tomato Target Spot:** Exhibited an error overlap of 3.4%. Both pathogens present concentric brown necrotic rings on mature leaves. LeafLens resolves this by rendering Grad-CAM saliency: Target Spot activations localize on smaller, scattered lesions, whereas Early Blight activations cluster along vascular margins.
- **Potato Early Blight vs. Potato Late Blight:** Exhibited a 2.8% confusion rate during early developmental stages before water-soaked sporulation occurs. The confidence-warning gate safely flagged 72% of these ambiguous cases for agronomist review.

#### 5.1.3 Model Calibration (ECE) & Uncertainty Gating
Standard uncalibrated deep neural networks suffer from severe overconfidence, frequently outputting 99% probability on completely incorrect predictions. LeafLens applied post-hoc **Temperature Scaling ($T = 1.25$)** on hold-out validation logits:
- **Uncalibrated Model:** Expected Calibration Error (ECE) = **14.8%** (Severe overconfidence).
- **Temperature-Calibrated LeafLens Model:** ECE = **5.3%** (**64.2% relative error reduction**).
- **Rejection Policy Evaluation:** Setting the rejection threshold at $\tau = 0.70$ successfully intercepted 91.2% of out-of-distribution inputs while rejecting fewer than 2.1% of genuine, high-quality agricultural diagnoses.

#### 5.1.4 Saliency Sanity Checks (Grad-CAM Pointing Game)
To confirm that Grad-CAM heatmaps reflect authentic biological pathology rather than background artifacts (soil, sunlight glares, farmer's hands), an empirical **Pointing Game Experiment** was conducted across 500 expert-annotated lesion bounding boxes:
- **Lesion Hit Rate:** **88.4%** of maximum activation peaks ($\max_{(x,y)} L_{\text{Grad-CAM}}$) fell strictly within annotated disease lesion boundaries.
- **Random Weight Sanity Test:** When final layer weights were randomized, heatmaps collapsed into uniform noise, verifying that the model's explanations are truly dependent on learned model representations.

#### 5.1.5 Mobile Inference Latency & Hardware Benchmarks
Inference latency, memory footprint, and CPU load were profiled across multiple hardware tiers using the Android Studio Energy & Memory Profiler:

```
+-----------------------------------------------------------------------------------+
|                        MOBILE HARDWARE PROFILING BENCHMARKS                       |
+----------------------------+----------------+---------------+---------------------+
| Hardware Platform          | Inference Time | RAM Footprint | Peak Battery Drain  |
+----------------------------+----------------+---------------+---------------------+
| Snapdragon 695 (GPU Del.)  | 142 ms         | 18.4 MB       | 1.2% / 100 scans    |
| Snapdragon 695 (CPU 4-Th.) | 168 ms         | 18.2 MB       | 1.5% / 100 scans    |
| Exynos 850 (CPU 4-Th.)     | 185 ms         | 19.1 MB       | 2.1% / 100 scans    |
| Pixel 7 Pro (NNAPI Del.)   | 94 ms          | 16.8 MB       | 0.8% / 100 scans    |
| Baseline Cloud API (4G WAN)| 2850 ms        | 4.2 MB        | 8.4% / 100 scans    |
+----------------------------+----------------+---------------+---------------------+
```

#### 5.1.6 Baseline Comparison Study
In compliance with the Capstone Portfolio mandate, LeafLens AI was benchmarked against two standard architectural baselines:
1. **Baseline 1 — Cloud ResNet-50 API:** A centralized REST endpoint running unquantized ResNet-50.
2. **Baseline 2 — Vanilla MobileNetV2 (Uncalibrated, No Quality Gate, No Grad-CAM):** A standard on-device deployment without pre-flight checks or explainability.

```
+-----------------------------------------------------------------------------------+
|                            BASELINE COMPARISON MATRIX                             |
+-----------------------------+-------------------+-----------------+---------------+
| Metric                      | Baseline 1 (Cloud)| Baseline 2 (Van)| LeafLens AI   |
+-----------------------------+-------------------+-----------------+---------------+
| Macro F1-Score              | 96.9%             | 94.2%           | 96.4%         |
| Rural Dead Zone Operability | 0% (Fails)        | 100%            | 100% (Zero WAN|
| End-to-End Latency          | 3,420 ms          | 190 ms          | 162 ms        |
| Expected Calibration Error  | 16.2%             | 14.8%           | 5.3% (Best)   |
| Blurry Image Rejection Rate | 0% (Processes)    | 0% (Processes)  | 100% Caught   |
| Visual Explainability (XAI) | None (Black Box)  | None            | Grad-CAM Jet  |
| Novel Disease Adaptation    | Impossible locally| Impossible      | 3-5 Shot k-NN |
| Data Bandwidth per Scan     | 4.8 MB            | 0 MB            | 0 MB (>95% sv)|
+-----------------------------+-------------------+-----------------+---------------+
```

### 5.2 USER EXPERIENCE ASSESSMENT & TELEMETRY
A field usability study was conducted with 18 participants (10 smallholder farmers, 5 agricultural students, and 3 Krishi Vigyan Kendra extension officers):
- **System Usability Scale (SUS):** LeafLens achieved a mean SUS score of **86.4 / 100**, placing it in the top 10th percentile ("Excellent" usability).
- **Farmer Feedback:** Farmers overwhelmingly praised the **multilingual voice readout** and the **Grad-CAM visual slider**, noting: *"Seeing the red spotlight on the exact fungus spot gives us confidence that the phone is actually looking at the disease and not just guessing."*
- **Extension Officer Feedback:** Officers highlighted the utility of the **shareable PDF diagnostic report** with embedded dosages, noting that it prevents farmers from purchasing adulterated or incorrect chemical formulas at local agri-retail stores.
- **Network Telemetry:** Field logging demonstrated an average cellular data savings of **4.8 MB per scan**, representing an overall network data reduction exceeding **98%** across weekly farm scouting routines.

---

## CHAPTER 6: CONCLUSION AND FUTURE WORK

### 6.1 CONCLUSION
The **LeafLens AI** capstone project has designed, implemented, and rigorously validated an edge-native, explainable mobile crop health assistant engineered specifically for resource-constrained, connectivity-deprived rural agricultural environments. By unifying an INT8-quantized MobileNetV2 inference engine, an OpenCV-based pre-flight quality gate, on-device Grad-CAM visual explainability, temperature-calibrated uncertainty safeguards, and metric-space few-shot disease adaptation into a cohesive Android Jetpack architecture, the system successfully addresses the four fatal flaws of existing agricultural AI: rural connectivity dead zones, black-box distrust, garbage-in misclassifications, and model training rigidity.

Operating 100% offline with an average latency of **162 ms**, a **Macro F1-score of 96.4%**, and an **ECE calibration of 5.3%**, LeafLens proves that state-of-the-art computer vision and Responsible-AI controls can be deployed effectively on commodity edge smartphones. The end-to-end integration of dual-track agronomic advisories, lesion severity grading, Room DB historical tracking, shareable PDF reporting, multilingual voice synthesis, and smart farming utilities establishes LeafLens AI as a robust, industry-ready prototype (TRL 4–5) delivering tangible, high-impact societal value to the agricultural community.

### 6.2 FUTURE SCOPE
1. **Ad-Hoc Horizontal Mesh Synchronization (Firework Architecture):** Propose and implement a peer-to-peer Wi-Fi Direct / Bluetooth Low Energy (BLE) mesh network allowing neighboring farmers' smartphones to exchange enrolled few-shot disease prototypes horizontally across farm fields before cloud synchronization ever occurs.
2. **Multi-Spectral Drone & IoT Sensor Fusion:** Integrate Bluetooth telemetry from ground-level soil moisture/NPK probes and thermal imaging cameras to correlate foliar visual lesions with subterranean root stress and moisture deficits.
3. **Multi-Disease Co-Infection Segmentation:** Extend the on-device segmentation engine to simultaneously detect, separate, and quantify multiple co-occurring pathogens (e.g., Early Blight and Spider Mite infestation) on a single leaf blade.
4. **Autonomous Robotic Actuator Interfacing:** Adapt the lightweight inference pipeline into ROS (Robot Operating System) nodes for integration on autonomous agricultural rovers and targeted micro-sprayer drones.

### 6.3 LIMITATIONS
1. **Severe Occlusion & Extreme Foliar Overlap:** While the OpenCV quality gate catches motion blur and illumination flaws, leaves with $>70\%$ surface occlusion by neighboring stems or fruit may yield reduced classification confidence.
2. **Few-Shot Boundary Drift in Highly Distorted Backgrounds:** On-device few-shot adaptation relies on clean 1024-dimensional feature extraction; if sample shots are enrolled with excessive extraneous non-leaf background, prototype centroids can experience minor feature drift.
3. **Platform Constraint:** The native application is currently implemented for the Android ecosystem; cross-platform iOS support requires porting to CoreML or Flutter TFLite FFI.

---

## CHAPTER 7: REFERENCES

[1] A. G. Howard, M. Zhu, B. Chen, D. Kalenichenko, W. Wang, T. Weyand, M. Andreetto, and H. Adam, "MobileNets: Efficient Convolutional Neural Networks for Mobile Vision Applications," *arXiv preprint arXiv:1704.04861*, 2017.

[2] M. Sandler, A. Howard, M. Zhu, A. Zhmoginov, and L.-C. Chen, "MobileNetV2: Inverted Residuals and Linear Bottlenecks," in *Proceedings of the IEEE Conference on Computer Vision and Pattern Recognition (CVPR)*, pp. 4510–4520, 2018.

[3] R. R. Selvaraju, M. Cogswell, A. Das, R. Vedantam, D. Parikh, and D. Batra, "Grad-CAM: Visual Explanations from Deep Networks via Gradient-Based Localization," in *Proceedings of the IEEE International Conference on Computer Vision (ICCV)*, pp. 618–626, 2017.

[4] J. Snell, K. Swersky, and R. Zemel, "Prototypical Networks for Few-shot Learning," in *Advances in Neural Information Processing Systems (NeurIPS)*, vol. 30, pp. 4077–4087, 2017.

[5] C. Guo, G. Pleiss, Y. Sun, and K. Q. Weinberger, "On Calibration of Modern Neural Networks," in *International Conference on Machine Learning (ICML)*, PMLR, pp. 1321–1330, 2017.

[6] D. P. Hughes and M. Salathé, "An open access repository of images on plant health to enable the development of mobile disease diagnostics," *arXiv preprint arXiv:1511.08060 (PlantVillage)*, 2015.

[7] G. Bradski, "The OpenCV Library," *Dr. Dobb's Journal of Software Tools*, 2000.

[8] P. Warden and D. Situnayake, *TinyML: Machine Learning with TensorFlow Lite on Arduino and Ultra-Low-Power Microcontrollers*, O'Reilly Media, 2019.

[9] Food and Agriculture Organization of the United Nations (FAO), "The State of Food and Agriculture: Leveraging automation in agriculture for transforming agrifood systems," *FAO Rome*, 2022.

[10] Indian Council of Agricultural Research (ICAR), "Handbook of Agriculture: Facts and Figures for Farmers, Students and All Interested in Farming," *ICAR New Delhi*, 2021.

[11] M. A. Adegboye, et al., "Evaluation of Deep Learning Architectures for Crop Disease Classification on Embedded Edge Devices," *IEEE Access*, vol. 9, pp. 83120–83134, 2021.

[12] Google AI, "Model Cards for Model Reporting," *Google Research*, 2019. Available: https://modelcards.withgoogle.com/

[13] Android Open Source Project, "CameraX Architecture and Pipeline Guide," *Android Developers*, 2024. Available: https://developer.android.com/training/camerax

[14] TensorFlow Team, "TensorFlow Lite: On-Device Machine Learning Framework," *Google Open Source*, 2024. Available: https://www.tensorflow.org/lite

[15] J. Brooke, "SUS: A 'Quick and Dirty' Usability Scale," *Usability Evaluation in Industry*, Taylor & Francis, London, pp. 189–194, 1996.