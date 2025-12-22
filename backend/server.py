from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import joblib
import numpy as np
import json
import os

app = FastAPI(title="Kaasht API")

# Load Artifacts
try:
    model = joblib.load('crop_prediction_model.pkl')
    scaler = joblib.load('scaler.pkl')
    le_district = joblib.load('label_encoder_district.pkl')
    le_crop = joblib.load('label_encoder_crop.pkl')
    print("Model loaded successfully.")
except Exception as e:
    print(f"Error loading model: {e}")
    model = None

class CropInput(BaseModel):
    N: float
    P: float
    K: float
    temperature: float
    humidity: float
    ph: float
    rainfall: float
    district: str

@app.get("/")
def home():
    return {"status": "Kaasht API Running"}

@app.post("/predict")
def predict(data: CropInput):
    if not model:
        raise HTTPException(status_code=500, detail="Model not loaded")
    
    try:
        # 1. Encode District
        clean_dist = data.district.strip().lower()
        if clean_dist in le_district.classes_:
            dist_val = le_district.transform([clean_dist])[0]
        else:
            dist_val = 0 # Fallback
            
        # 2. Scale Features
        features = np.array([[
            data.N, data.P, data.K, 
            data.temperature, data.humidity, 
            data.ph, data.rainfall, 
            dist_val
        ]])
        features_scaled = scaler.transform(features)
        
        # 3. Predict
        probs = model.predict_proba(features_scaled)[0]
        top5_idx = np.argsort(probs)[::-1][:5]
        
        recommendations = []
        for rank, idx in enumerate(top5_idx, 1):
            recommendations.append({
                "crop": le_crop.inverse_transform([idx])[0],
                "confidence": round(probs[idx] * 100, 2),
                "rank": rank
            })
            
        return {
            "success": True,
            "recommendations": recommendations,
            "input_summary": data.dict()
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)