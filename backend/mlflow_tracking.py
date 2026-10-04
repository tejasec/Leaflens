"""
LeafLens AI - MLflow MLOps Experiment Tracking Script
=====================================================
Logs the certified empirical evaluation runs, hyperparameters, quantization profiles,
and calibration metrics from the Capstone Portfolio (BAI-03).
"""

import json
import os
import mlflow

# Metrics certified in Capstone Black Book
EXPERIMENT_NAME = "LeafLens_AI_Edge_Optimization"
TRACKING_URI = "sqlite:///backend/mlflow.db"


def log_experiment_run():
    print(f"[MLFLOW] Setting tracking URI to: {TRACKING_URI}")
    mlflow.set_tracking_uri(TRACKING_URI)
    mlflow.set_experiment(EXPERIMENT_NAME)

    run_name = "mobilenetv2_int8_temperature_calibrated"
    print(f"[MLFLOW] Starting active run: {run_name}")

    with mlflow.start_run(run_name=run_name) as run:
        run_id = run.info.run_id
        print(f"[MLFLOW] Active Run ID: {run_id}")

        # 1. Log Architecture & Hyperparameters
        params = {
            "model_architecture": "MobileNetV2-InvertedResiduals",
            "precision": "INT8-PostTrainingQuantization",
            "runtime_engine": "TensorFlow-Lite-2.16",
            "input_resolution": "224x224x3",
            "target_disease_classes": 25,
            "temperature_parameter_T": 1.35,
            "confidence_threshold_tau": 0.70,
            "quality_gate_blur_min_var": 100.0,
            "few_shot_metric_space": "CosineDistance-kNN-1024d",
            "academic_code": "BAI-03",
            "institution": "KES Shroff College / University of Mumbai"
        }
        for k, v in params.items():
            mlflow.log_param(k, v)
        print(f"[MLFLOW] Logged {len(params)} parameters.")

        # 2. Log Empirical Performance Metrics (Certified from Black Book)
        metrics = {
            "macro_f1": 0.964,
            "overall_accuracy": 0.968,
            "uncalibrated_ece": 0.148,
            "calibrated_ece": 0.053,
            "ece_relative_reduction_pct": 64.2,
            "mean_edge_latency_ms": 162.0,
            "snapdragon695_gpu_latency_ms": 142.0,
            "exynos850_cpu_latency_ms": 185.0,
            "pointing_game_saliency_hit_rate": 0.884,
            "model_storage_size_mb": 2.5,
            "cellular_bandwidth_savings_pct": 98.2,
            "system_usability_scale_sus": 86.4
        }
        for k, v in metrics.items():
            mlflow.log_metric(k, v)
        print(f"[MLFLOW] Logged {len(metrics)} metrics.")

        # 3. Log Artifacts
        artifacts_dir = "backend/mlruns_temp_artifacts"
        os.makedirs(artifacts_dir, exist_ok=True)
        summary_path = os.path.join(artifacts_dir, "run_summary.json")
        with open(summary_path, "w") as f:
            json.dump({"params": params, "metrics": metrics, "run_id": run_id}, f, indent=2)

        mlflow.log_artifact(summary_path, artifact_path="provenance")
        print(f"[MLFLOW] Logged artifact '{summary_path}' to run.")

        print("\n" + "=" * 60)
        print(f"MLflow Run Successfully Completed!")
        print(f"Experiment Name : {EXPERIMENT_NAME}")
        print(f"Run ID          : {run_id}")
        print(f"Macro F1 Logged : {metrics['macro_f1'] * 100:.1f}%")
        print(f"Calibrated ECE  : {metrics['calibrated_ece'] * 100:.1f}%")
        print(f"Edge Latency    : {metrics['mean_edge_latency_ms']:.0f} ms")
        print("=" * 60)


if __name__ == "__main__":
    log_experiment_run()
