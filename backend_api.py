from fastapi import FastAPI
from pydantic import BaseModel
import joblib
import numpy as np
import uvicorn

# Initialize App
app = FastAPI(title="Kaasht API", description="AI Crop Prediction Backend")

# Load AI Models
model = joblib.load('kaasht_model.pkl')
scaler = joblib.load('scaler.pkl')

# Define Input Structure (Pydantic)
class SoilData(BaseModel):
    N: int
    P: int
    K: int
    temperature: float
    humidity: float
    ph: float
    rainfall: float

@app.get("/")
def home():
    return {"status": "Kaasht API is Running"}

@app.post("/predict")
def predict_crop(data: SoilData):
    # 1. Prepare Input
    features = np.array([[
        data.N, data.P, data.K, 
        data.temperature, data.humidity, 
        data.ph, data.rainfall
    ]])
    
    # 2. Scale Data
    scaled_features = scaler.transform(features)
    
    # 3. Predict
    prediction = model.predict(scaled_features)[0]
    probabilities = model.predict_proba(scaled_features)[0]
    confidence = np.max(probabilities) * 100
    
    return {
        "recommended_crop": prediction,
        "confidence_score": f"{confidence:.2f}%",
        "soil_status": "Optimal" if 6.0 <= data.ph <= 7.5 else "Needs Adjustment"
    }

if __name__ == "__main__":
    uvicorn.run(app, host="127.0.0.1", port=8000)