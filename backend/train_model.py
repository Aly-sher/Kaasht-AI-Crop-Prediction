import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split, GridSearchCV
from sklearn.ensemble import RandomForestClassifier
from sklearn.preprocessing import LabelEncoder, StandardScaler
from sklearn.metrics import classification_report, accuracy_score
import joblib
import json

def main():
    print("Loading dataset...")
    # Make sure 'Dataset kaasht.csv' is in the same folder!
    try:
        df = pd.read_csv('Dataset kaasht.csv')
    except FileNotFoundError:
        print("Error: 'Dataset kaasht.csv' not found. Please upload it to the backend folder.")
        return

    # Preprocessing
    le_district = LabelEncoder()
    le_label = LabelEncoder()
    
    df['district_encoded'] = le_district.fit_transform(df['district'].astype(str).str.strip().str.lower())
    df['label_encoded'] = le_label.fit_transform(df['label'].astype(str).str.strip().str.lower())
    
    feature_cols = ['N', 'P', 'K', 'temperature', 'humidity', 'ph', 'rainfall', 'district_encoded']
    X = df[feature_cols]
    y = df['label_encoded']
    
    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)
    
    # Train/Test Split
    X_train, X_test, y_train, y_test = train_test_split(X_scaled, y, test_size=0.2, random_state=42, stratify=y)
    
    # Training
    print("Training Random Forest Model...")
    rf = RandomForestClassifier(n_estimators=100, random_state=42, n_jobs=-1)
    rf.fit(X_train, y_train)
    
    # Evaluation
    print(f"Accuracy: {accuracy_score(y_test, rf.predict(X_test))*100:.2f}%")
    
    # Save Artifacts
    joblib.dump(rf, 'crop_prediction_model.pkl')
    joblib.dump(scaler, 'scaler.pkl')
    joblib.dump(le_district, 'label_encoder_district.pkl')
    joblib.dump(le_label, 'label_encoder_crop.pkl')
    
    # Save Mappings for reference
    crop_mapping = dict(zip(le_label.transform(le_label.classes_), le_label.classes_))
    with open('crop_mapping.json', 'w') as f:
        json.dump({int(k): v for k, v in crop_mapping.items()}, f, indent=2)

    district_mapping = dict(zip(le_district.transform(le_district.classes_), le_district.classes_))
    with open('district_mapping.json', 'w') as f:
        json.dump({int(k): v for k, v in district_mapping.items()}, f, indent=2)
        
    print("Model artifacts saved successfully.")

if __name__ == "__main__":
    main()