package com.kaasht.croprecommendation.utils

object Constants {
    
    // API Configuration
    const val BASE_URL = "https://your-api-url.com/"
    const val WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/"
    
    // Database
    const val DATABASE_NAME = "kaasht_db"
    const val DATABASE_VERSION = 1
    
    // Shared Preferences
    const val PREFS_NAME = "kaasht_prefs"
    const val KEY_USER_ID = "user_id"
    const val KEY_USER_EMAIL = "user_email"
    const val KEY_USER_DISTRICT = "user_district"
    const val KEY_FIRST_LAUNCH = "first_launch"
    
    // Request Codes
    const val REQUEST_LOCATION_PERMISSION = 1001
    const val REQUEST_BLUETOOTH_PERMISSION = 1002
    const val REQUEST_STORAGE_PERMISSION = 1003
    
    // Notification Channels
    const val CHANNEL_WEATHER_ALERTS = "weather_alerts"
    const val CHANNEL_PREDICTIONS = "predictions"
    
    // Districts in Punjab
    val PUNJAB_DISTRICTS = listOf(
        "attock", "rawalpindi", "islamabad", "jehlum", "chakwal", "sargodha",
        "khushab", "mianwali", "bhakkar", "faisalabad", "toba tek singh",
        "jhang", "gujrat", "mandi bahauddin", "sialkot", "narowal", "gujranwala",
        "hafizabad", "sheikhupura", "nankana sahib", "lahore", "kasur",
        "okara", "sahiwal", "pakpattan", "multan", "lodhran", "khanewal",
        "vehari", "muzaffargarh", "layyah", "dera ghazi khan", "rajanpur",
        "bahawalnagar", "rahim yar khan", "bahawalpur"
    )
    
    // Crop Types
    val CROP_TYPES = listOf(
        "rice", "wheat", "maize", "cotton", "sugarcane",
        "pulses", "millets", "mungbean", "blackgram", "lentil",
        "banana", "mango", "grapes"
    )
}
