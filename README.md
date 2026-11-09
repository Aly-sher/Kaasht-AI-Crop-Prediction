##          🌾 Kaasht - AI-Powered Crop Prediction System

<div align="center">

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat&logo=android)
![Backend](https://img.shields.io/badge/Backend-FastAPI-009688?style=flat&logo=fastapi)
![ML](https://img.shields.io/badge/AI-Random%20Forest-orange?style=flat)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

*A research prototype that recommends crops from soil nutrients and weather data. Built as a BS Software Engineering thesis project at AUST, Pakistan.*

</div>

---

## 📱 Overview

Kaasht is a crop recommendation system with an Android app, a FastAPI prediction server and a Random Forest model. By analyzing soil nutrients (N, P, K, pH) and weather conditions, it recommends the crops most suited to those conditions. It was built by two students as a BS Software Engineering thesis project at Abbottabad University of Science and Technology (AUST), Pakistan.
</div>

## 🧪 Project Status

This is a research prototype.

⚠️ It has not been deployed with farmers or tested in the field.
⚠️ The results below come from 2,202 labeled samples. They do not show how the model would perform on real farms.
⚠️ The Android app is a thesis-era prototype and is not actively maintained. The model training code and the prediction server are the parts most useful to reuse.

## 🎯 Problem Statement

Many farmers choose crops from habit and experience. This project asks whether soil and weather measurements can support that decision with a simple model that can be inspected and explained. Kaasht explores this by providing:

✅ Data-Driven Recommendations: Trained on 2,202 labeled samples.
✅ Real-Time Weather: Integration with a live weather API.
✅ Soil Nutrient Input: N, P, K and pH from Bluetooth sensors or manual entry.

## 🚀 Features

# 🧠 AI-Powered Prediction
Uses a Random Forest Classifier.
Analyzes 8 parameters: Nitrogen, Phosphorus, Potassium, Temperature, Humidity, pH, Rainfall, and District.
Returns the top 5 crops, each with the model's predicted probability. The app labels this a "success rate" (see Limitations).

# 🌦️ Weather Integration
Fetches live data via OpenWeatherMap API.
Displays 7-day forecasts.

# 📊 History & Analytics
Saves predictions locally using Room Database.
Visualizes trends with dynamic Charts.
Cloud Sync with Firebase Firestore.

# 📤 Reporting
Export prediction reports as PDF.
Export data as CSV for external analysis.

# 📡 IoT Sensor Support
Bluetooth integration for ESP32/Arduino Soil Sensors.
Manual input as a fallback.

## 🗂️ Data

The training data is in backend/Dataset kaasht.csv: 2,202 labeled samples with the eight input features above and a crop label.

The dataset was downloaded from an online source and prepared for this project (cleaning, encoding and scaling). The original source was not recorded, so it is not cited here. The project team did not collect it. The MIT license below covers the code; the dataset comes from a third party and may have its own terms.

## 🔬 Model and Evaluation

Preprocessing. Missing values and outliers were handled with mean or median imputation. The district column was label-encoded, and the numeric features were standardized with StandardScaler. The data was split 80:20 into training and test sets.

Model. A Random Forest classifier (scikit-learn), trained by backend/train_model.py. Hyperparameters were tuned with GridSearchCV using 5-fold stratified cross-validation, over n_estimators, max_depth, min_samples_leaf and min_samples_split.

How it was chosen. Several other algorithms were tried first, and some overfit: they scored well on training data and poorly on unseen data. The candidates were then compared against each other in pairs, under the guidance of the project supervisor, and Random Forest performed best.

Result. About 88.6% accuracy on the held-out 20% test set, as reported in the project thesis.

## 🔌 API

The server exposes a /predict endpoint. It accepts a POST request with N, P, K, temperature, humidity, pH, rainfall and district, validates the input, applies the saved encoder and scaler, and returns the top crops by predicted probability. Example of the response format:

json
```
{"recommendations": [{"crop": "rice", "success_rate": 0.8542}, {"crop": "maize", "success_rate": 0.1234}]}
```

## ⚠️ Limitations
The dataset is small (2,202 samples) and was not collected by the project team, so it may not reflect the soils, weather and districts of real farms.
The system has not been validated on real farms.
The "success rate" shown to users is the Random Forest's predicted class probability. It says how strongly the model favors a crop given its training data. It is not a measured chance that the crop will succeed on a farm, and it has not been checked for calibration.
It is a prototype built for a thesis, not a production system.

# ❓ Open Questions

These are directions the work could take. They are not done in this repository.

Trust on unseen conditions. How reliable is a model trained on a small dataset when soil and weather conditions differ from the training data?
Uncertainty. Are the confidence scores well calibrated? How should the system tell a user when a recommendation is weak?
Learning across farms. Can models learn from data held on many farms or districts without collecting it all in one place?


# 🛠️ Tech Stack
```
Android (Client)
Component	Technology
Language	Kotlin
Architecture	MVVM (Model-View-ViewModel)
DI	Dagger Hilt
Networking	Retrofit + OkHttp
Local DB	Room Database
UI	Material Design 3, ViewBinding
Charts	MPAndroidChart
Backend (Server)
Component	Technology
Framework	FastAPI (Python)
ML Model	Random Forest (Scikit-Learn)
Server	Uvicorn
Data	Pandas, NumPy
```

# 📂 Project Structure
```text
Kaasht-AI-Crop-Prediction/
├── android/               # Android Application Source Code
│   ├── app/src/main/java  # Kotlin Code (MVVM Architecture)
│   └── app/src/main/res   # UI Layouts & Resources
│
├── backend/               # Python ML Server
│   ├── train_model.py     # Script to train the ML model
│   ├── server.py          # FastAPI server for predictions
│   └── Dataset kaasht.csv # Training Dataset
```
---
# ▶️ Running the Backend
```bash
cd backend
pip install fastapi uvicorn scikit-learn pandas numpy
python train_model.py
uvicorn server:app --reload
```
---
# 👥 Team
Ali Sher Khan Tareen: data preprocessing, feature engineering, model training and evaluation, and the FastAPI prediction server
Mohsin Waseem: system design and the Android app

Supervisor: Mr. Mir Jamal-ud-din

Abbottabad University of Science & Technology
---

# 📄 License

This project is licensed under the MIT License.
<div align="center"> <sub>Made with ❤️ in Pakistan</sub> </div>


