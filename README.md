# 🌱 Kaasht: AI-Powered Smart Agriculture System

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Python 3.9+](https://img.shields.io/badge/python-3.9+-blue.svg)](https://www.python.org/downloads/)
[![Status: Completed](https://img.shields.io/badge/Status-Completed-success.svg)]()

> **Final Year Thesis Project** | Bachelor of Science in Software Engineering  
> **Author:** Ali Sher Khan Tareen

---

## 📖 Overview

**Kaasht** is an end-to-end IoT and Artificial Intelligence solution designed to modernize traditional farming practices. By bridging the gap between hardware sensors and machine learning algorithms, the system provides farmers with real-time soil analytics and accurate crop recommendations to maximize yield and resource efficiency.

This project addresses the critical issue of lack of data-driven decision-making in agriculture, offering a scalable solution for precision farming.

---

## ✨ Key Features

* **🤖 AI Crop Recommendation:** Utilizes Machine Learning algorithms to analyze soil parameters (N, P, K, pH) and environmental factors (Temperature, Humidity, Rainfall) to predict the optimal crop for cultivation.
* **📡 IoT Integration:** Real-time data acquisition using IoT sensors (Soil Moisture, DHT11/22) transmitted via microcontrollers (ESP32/Arduino).
* **📊 Interactive Dashboard:** A user-friendly web interface for monitoring live sensor data and viewing prediction results.
* **📉 Data Visualization:** Historical data tracking and graphical analysis of soil health trends.

---

## 🛠️ Tech Stack

### **Hardware (IoT)**
* **Microcontroller:** ESP32 / Arduino Uno
* **Sensors:** Capacitive Soil Moisture Sensor, DHT11 (Temperature & Humidity), NPK Sensor
* **Connectivity:** Wi-Fi / MQTT Protocol

### **Software & AI**
* **Language:** Python
* **Machine Learning:** Scikit-Learn, Pandas, NumPy
* **Algorithms:** Random Forest Classifier / XGBoost (Achieved 90%+ Accuracy)
* **Backend:** Flask / FastAPI
* **Frontend:** Streamlit / React / HTML5
* **Database:** Firebase / MySQL / SQLite

---

## ⚙️ System Architecture

The system follows a 3-tier architecture:
1.  **Perception Layer:** Sensors collect environmental data.
2.  **Network Layer:** Data is transmitted to the cloud server via Wi-Fi.
3.  **Application Layer:** The ML model processes data to generate insights displayed on the user dashboard.

*(Optional: Add a diagram of your system architecture here)*

---

## 🚀 Installation & Setup

### Prerequisites
* Python 3.8+
* Arduino IDE (for hardware code)

### 1. Clone the Repository
```bash
git clone [https://github.com/yourusername/kaasht-smart-agri.git](https://github.com/yourusername/kaasht-smart-agri.git)
cd kaasht-smart-agri