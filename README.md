🌱 Kaasht: AI-Powered Crop Prediction System

A Smart Agriculture Solution for Punjab, Pakistan using IoT & Machine Learning.

Kaasht is a mobile-based decision support system designed to assist farmers in selecting the optimal crop for their land. By integrating real-time data from Weather APIs and Soil Sensors, the system utilizes a Random Forest Classifier to predict crop suitability with 88.6% accuracy.

📄 Thesis Documentation

Click the link below to view the official thesis report submitted to Abbottabad University of Science & Technology.

📥 Download Thesis PDF

(Note: The PDF is located in the docs/ folder of this repository)

🚀 Key Features

Real-Time Environmental Analysis: Fetches live temperature, humidity, and rainfall data via OpenWeatherMap API.

IoT Sensor Integration: Accepts inputs for Soil pH, Nitrogen (N), Phosphorus (P), and Potassium (K) levels.

Machine Learning Engine: Uses a localized dataset from 22 districts in Punjab to train a Random Forest model optimized via GridSearchCV.

FastAPI Backend: A high-performance REST API handles concurrent requests and validates data using Pydantic.

Android Interface: A user-friendly mobile app with multilingual support for farmers.

🛠️ Technical Architecture

The system follows a modular architecture:

Data Layer: Aggregates data from IoT sensors and Weather APIs.

Processing Layer: Preprocesses data (StandardScaler) and encodes categorical variables (District).

Intelligence Layer: A Random Forest Classifier predicts the top N suitable crops.

Application Layer: An Android app consumes the FastAPI endpoints to display results.

📊 Model Performance

Algorithm: Random Forest Classifier (Ensemble Learning)

Accuracy: 88.6% on Test Data

Cross-Validation: 5-Fold Stratified CV

Key Predictors: Temperature and Humidity were identified as the most critical features for crop viability in Punjab.

🔧 Installation & Setup

Backend (FastAPI)

pip install -r requirements.txt
uvicorn backend_api:app --reload


ML Model Training

python train_model.py


👨‍💻 Authors

Ali Sher Khan
Bachelor of Science in Software Engineering
Abbottabad University of Science & Technology
