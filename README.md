# 🌾 Kaasht - AI-Powered Crop Prediction System

<div align="center">

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat&logo=android)
![Backend](https://img.shields.io/badge/Backend-FastAPI-009688?style=flat&logo=fastapi)
![ML](https://img.shields.io/badge/AI-Random%20Forest-orange?style=flat)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

*Empowering farmers in Punjab, Pakistan with intelligent crop recommendations using AI and real-time environmental data.*

</div>

---

## 📱 Overview

**Kaasht** is a smart agriculture mobile application designed to assist farmers. By analyzing soil nutrients (N, P, K, pH) and real-time weather conditions, it uses Machine Learning to recommend the most optimal crops to maximize yield.

### 🎯 Problem Statement
Traditional farming relies on guesswork. Kaasht solves this by providing:
- ✅ **Data-Driven Decisions:** Based on 2,200+ agricultural records.
- ✅ **Real-Time Analysis:** Integration with live weather APIs.
- ✅ **Precision:** Soil nutrient analysis for accurate predictions.

---

## 🚀 Features

### 🧠 **AI-Powered Prediction**
- Uses a **Random Forest Classifier**.
- Analyzes **8 parameters**: Nitrogen, Phosphorus, Potassium, Temperature, Humidity, pH, Rainfall, and District.
- Provides top 5 crop recommendations with confidence scores.

### 🌦️ **Weather Integration**
- Fetches live data via **OpenWeatherMap API**.
- Displays 7-day forecasts and real-time alerts.

### 📊 **History & Analytics**
- Saves predictions locally using **Room Database**.
- Visualizes crop trends using dynamic **Bar Charts**.
- **Cloud Sync** with Firebase Firestore.

### 📤 **Reporting**
- Export prediction reports as **PDF** (Professional Format).
- Export data as **CSV** for external analysis.

### 📡 **IoT Sensor Support**
- Bluetooth integration for **ESP32/Arduino Soil Sensors**.
- Live data streaming directly into the app.

---

## 🛠️ Tech Stack

### **Android (Client)**

| Component | Technology |
|-----------|-----------|
| **Language** | Kotlin |
| **Architecture** | MVVM (Model-View-ViewModel) |
| **DI** | Dagger Hilt |
| **Networking** | Retrofit + OkHttp |
| **Local DB** | Room Database |
| **UI** | Material Design 3, ViewBinding |
| **Charts** | MPAndroidChart |

### **Backend (Server)**

| Component | Technology |
|-----------|-----------|
| **Framework** | FastAPI (Python) |
| **ML Model** | Random Forest (Scikit-Learn) |
| **Server** | Uvicorn |
| **Data** | Pandas, NumPy |

---

## 📂 Project Structure

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

## 👥 Team

Ali Sher Khan Tareen   - Lead Developer & ML Engineer



Mohsin Waseem          - Team Member

Supervisor: Mr. Mir Jamal-ud-din

Abbottabad University of Science & Technology

---

## 📄 License
This project is licensed under the MIT License.

<div align="center"> <sub>Made with ❤️ in Pakistan</sub> </div>


