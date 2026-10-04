"""
LeafLens AI - Backend Automated Integration Test Suite
======================================================
Tests all mandatory API endpoints:
- GET /health
- POST /predict & POST /api/v1/predict (Valid & Blurry)
- POST /api/v1/chatbot/chat (Deterministic Fallback)
- GET /api/v1/schemes/online (Govt Welfare Schemes)
"""

import io
import cv2
import numpy as np
import pytest
from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def create_sharp_leaf_image_bytes() -> bytes:
    """Generates a high-contrast 224x224 synthetic leaf with edges (Laplacian Var > 100)."""
    img = np.zeros((224, 224, 3), dtype=np.uint8)
    # Foliar green background
    img[:] = (35, 140, 45)
    # Add high-frequency lesion textures and high-contrast lines
    for i in range(10, 210, 15):
        cv2.circle(img, (i, i), 8, (15, 60, 180), -1)
        cv2.line(img, (i, 0), (i, 224), (20, 80, 20), 2)
    success, encoded = cv2.imencode(".jpg", img)
    assert success, "Failed to encode sharp test image"
    return encoded.tobytes()


def create_blurry_leaf_image_bytes() -> bytes:
    """Generates a uniform smooth green image with zero high frequencies (Laplacian Var < 10)."""
    img = np.full((224, 224, 3), (40, 160, 50), dtype=np.uint8)
    # Apply heavy blur to guarantee variance << 100.0
    img = cv2.GaussianBlur(img, (25, 25), 0)
    success, encoded = cv2.imencode(".jpg", img)
    assert success, "Failed to encode blurry test image"
    return encoded.tobytes()


# -----------------------------------------------------------------------------
# 1. Health Endpoint Tests
# -----------------------------------------------------------------------------

def test_health_check_returns_ok():
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "healthy"
    assert "LeafLens AI" in data["service"]
    assert data["version"] == "1.0.0"
    assert data["confidence_threshold"] == 0.70


# -----------------------------------------------------------------------------
# 2. Prediction Endpoint Tests (Shared Handler Verification)
# -----------------------------------------------------------------------------

@pytest.mark.parametrize("route", ["/predict", "/api/v1/predict"])
def test_predict_valid_sharp_leaf_returns_diagnosis_and_gradcam(route):
    image_bytes = create_sharp_leaf_image_bytes()
    response = client.post(
        route,
        files={"file": ("leaf.jpg", image_bytes, "image/jpeg")}
    )
    assert response.status_code == 200
    data = response.json()
    
    # Contract validation with Android CloudPredictionResponse
    assert "diseaseName" in data
    assert data["confidence"] >= 0.70
    assert data["gradCamBase64"] is not None
    assert len(data["gradCamBase64"]) > 100
    assert data["aiExplanation"] is not None
    assert data["organicCare"] is not None
    assert data["chemicalCare"] is not None
    assert "Tomato - Early blight" in data["diseaseName"]


def test_predict_supports_image_field_name():
    """Validates that multipart uploads using either 'file' or 'image' field work."""
    image_bytes = create_sharp_leaf_image_bytes()
    response = client.post(
        "/predict",
        files={"image": ("sample.png", image_bytes, "image/png")}
    )
    assert response.status_code == 200
    data = response.json()
    assert data["confidence"] >= 0.70


def test_predict_blurry_image_returns_quality_gate_warning():
    """Validates that blurry input returns low-confidence rejection without raising 500."""
    blurry_bytes = create_blurry_leaf_image_bytes()
    response = client.post(
        "/predict",
        files={"file": ("blurry_leaf.jpg", blurry_bytes, "image/jpeg")}
    )
    assert response.status_code == 200
    data = response.json()
    assert data["confidence"] < 0.70
    assert "Blurry" in data["diseaseName"]
    assert "QualityGate" in data["aiExplanation"]
    assert data["gradCamBase64"] is None


def test_predict_blurry_image_strict_mode_raises_400():
    """Validates that strict quality enforcement returns HTTP 400 Bad Request."""
    blurry_bytes = create_blurry_leaf_image_bytes()
    response = client.post(
        "/predict?strict=true",
        files={"file": ("blurry_leaf.jpg", blurry_bytes, "image/jpeg")}
    )
    assert response.status_code == 400
    data = response.json()
    assert "Blurry" in data["detail"] or "QualityGate" in data["detail"]


def test_predict_corrupted_payload_returns_400():
    response = client.post(
        "/predict",
        files={"file": ("bad.txt", b"corrupted random data not an image", "text/plain")}
    )
    assert response.status_code == 400


# -----------------------------------------------------------------------------
# 3. Chatbot Advisory Tests (Deterministic Fallback)
# -----------------------------------------------------------------------------

def test_chatbot_chat_deterministic_fallback():
    payload = {
        "message": "What is the best treatment for early blight on tomato leaves?",
        "disease_context": "Tomato - Early blight"
    }
    response = client.post("/api/v1/chatbot/chat", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert "reply" in data
    assert len(data["reply"]) > 20
    assert data["is_fallback"] is True
    assert "LeafLens Agronomic Advisor" in data["reply"]
    # Check that actionable agronomic guidance is provided
    assert "neem oil" in data["reply"].lower() or "mancozeb" in data["reply"].lower()


# -----------------------------------------------------------------------------
# 4. Government Schemes Endpoint Tests
# -----------------------------------------------------------------------------

def test_get_online_schemes():
    response = client.get("/api/v1/schemes/online")
    assert response.status_code == 200
    schemes = response.json()
    assert isinstance(schemes, list)
    assert len(schemes) >= 5

    # Validate first scheme schema matches GovtSchemeResponse in Android
    first = schemes[0]
    assert "id" in first
    assert "title" in first
    assert "description" in first
    assert "eligibility" in first
    assert "details" in first
    assert "subsidyAmount" in first
    assert "applicationUrl" in first
    assert "PM-KISAN" in first["title"]
