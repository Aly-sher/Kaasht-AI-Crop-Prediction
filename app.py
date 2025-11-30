import streamlit as st
import requests
import time

# Configure Page to look like a Mobile App
st.set_page_config(page_title="Kaasht Mobile", page_icon="🌱", layout="centered")

# Custom CSS for Mobile-Like Feel
st.markdown("""
<style>
    .stApp { background-color: #f0f2f6; }
    .main-card {
        background-color: white;
        padding: 20px;
        border-radius: 15px;
        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        text-align: center;
    }
    h1 { color: #2e7d32; }
</style>
""", unsafe_allow_html=True)

st.title("🌱 Kaasht")
st.markdown("### AI-Powered Crop Recommendation")

# --- INPUT SECTION ---
with st.container():
    st.write("Enter Soil & Weather Conditions:")
    
    col1, col2 = st.columns(2)
    with col1:
        N = st.number_input("Nitrogen (N)", 0, 140, 50)
        K = st.number_input("Potassium (K)", 0, 200, 30)
        temp = st.number_input("Temperature (°C)", 0.0, 50.0, 25.0)
        ph = st.number_input("Soil pH", 0.0, 14.0, 6.5)
    
    with col2:
        P = st.number_input("Phosphorus (P)", 0, 145, 40)
        hum = st.number_input("Humidity (%)", 0.0, 100.0, 70.0)
        rain = st.number_input("Rainfall (mm)", 0.0, 300.0, 200.0)

# --- PREDICTION BUTTON ---
if st.button("🔍 Predict Best Crop", type="primary"):
    with st.spinner("Analyzing Soil Sensors..."):
        time.sleep(1) # Simulate network delay
        
        # Prepare Data for API
        payload = {
            "N": N, "P": P, "K": K,
            "temperature": temp, "humidity": hum,
            "ph": ph, "rainfall": rain
        }
        
        try:
            # Send to FastAPI (Ensure backend_api.py is running!)
            response = requests.post("[http://127.0.0.1:8000/predict](http://127.0.0.1:8000/predict)", json=payload)
            
            if response.status_code == 200:
                result = response.json()
                
                # --- RESULT DISPLAY ---
                st.markdown("---")
                st.success(f"Recommended Crop: **{result['recommended_crop']}**")
                
                col_a, col_b = st.columns(2)
                col_a.metric("Confidence", result['confidence_score'])
                col_b.metric("Soil Status", result['soil_status'])
                
                st.info("💡 Recommendation based on Punjab historical data.")
            else:
                st.error("API Error. Please check backend.")
                
        except:
            st.error("⚠️ Connection Failed. Make sure `backend_api.py` is running.")

# Footer
st.markdown("---")
st.caption("Developed by **Ali Sher Khan & Mohsin Waseem** | FYP 2025")