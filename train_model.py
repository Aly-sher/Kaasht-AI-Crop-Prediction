import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler, LabelEncoder
from sklearn.metrics import accuracy_score
import joblib

# 1. Simulate Punjab Agricultural Data (Based on Thesis Section 4.10.2)
# Simulating N, P, K, Temp, Humidity, pH, Rainfall for crops like Rice, Wheat, Maize
def generate_data(n_samples=2200):
    np.random.seed(42)
    data = []
    crops = ['Rice', 'Wheat', 'Maize', 'Cotton', 'Sugarcane']
    
    for _ in range(n_samples):
        crop = np.random.choice(crops)
        
        if crop == 'Rice':
            N = np.random.randint(60, 90); P = np.random.randint(35, 60); K = np.random.randint(35, 45)
            temp = np.random.uniform(20, 27); hum = np.random.uniform(80, 90); ph = np.random.uniform(6.0, 7.0)
            rain = np.random.uniform(180, 300)
        elif crop == 'Wheat':
            N = np.random.randint(20, 40); P = np.random.randint(20, 40); K = np.random.randint(15, 25)
            temp = np.random.uniform(15, 25); hum = np.random.uniform(50, 65); ph = np.random.uniform(6.0, 7.0)
            rain = np.random.uniform(40, 100)
        elif crop == 'Cotton':
            N = np.random.randint(100, 140); P = np.random.randint(35, 60); K = np.random.randint(15, 25)
            temp = np.random.uniform(25, 35); hum = np.random.uniform(40, 60); ph = np.random.uniform(6.0, 8.0)
            rain = np.random.uniform(60, 110)
        else: # Maize/Sugarcane (Generalizing)
            N = np.random.randint(50, 100); P = np.random.randint(30, 60); K = np.random.randint(20, 50)
            temp = np.random.uniform(20, 30); hum = np.random.uniform(50, 70); ph = np.random.uniform(5.5, 7.5)
            rain = np.random.uniform(80, 150)

        data.append([N, P, K, temp, hum, ph, rain, crop])
    
    return pd.DataFrame(data, columns=['N', 'P', 'K', 'temperature', 'humidity', 'ph', 'rainfall', 'label'])

# 2. Preprocessing
print("🌱 Generating Dataset...")
df = generate_data()
X = df.drop('label', axis=1)
y = df['label']

# Scaling (Thesis Section 4.10.3)
print("⚙️ Scaling Features...")
scaler = StandardScaler()
X_scaled = scaler.fit_transform(X)

# 3. Training (Random Forest)
print("🧠 Training Random Forest Model...")
X_train, X_test, y_train, y_test = train_test_split(X_scaled, y, test_size=0.2, random_state=42)
model = RandomForestClassifier(n_estimators=100, random_state=42)
model.fit(X_train, y_train)

# Evaluation
acc = accuracy_score(y_test, model.predict(X_test))
print(f"✅ Model Accuracy: {acc:.2%}")

# 4. Save Artifacts
joblib.dump(model, 'kaasht_model.pkl')
joblib.dump(scaler, 'scaler.pkl')
print("💾 Model & Scaler Saved.")