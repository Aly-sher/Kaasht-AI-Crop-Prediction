package com.kaasht.croprecommendation.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.kaasht.croprecommendation.data.model.UserProfile
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    
    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser
    
    val isUserLoggedIn: Boolean
        get() = currentUser != null
    
    /**
     * Register user with email and password
     */
    suspend fun registerUser(
        email: String,
        password: String,
        name: String,
        district: String
    ): Result<FirebaseUser> {
        return try {
            val authResult = firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .await()
            
            val user = authResult.user ?: throw Exception("User creation failed")
            
            // Create user profile in Firestore
            val profile = UserProfile(
                userId = user.uid,
                email = email,
                name = name,
                contact = null,
                district = district,
                location = null,
                createdAt = System.currentTimeMillis()
            )
            
            saveUserProfile(profile)
            
            Timber.d("User registered: ${user.email}")
            Result.success(user)
        } catch (e: Exception) {
            Timber.e(e, "Registration failed")
            Result.failure(e)
        }
    }
    
    /**
     * Login user with email and password
     */
    suspend fun loginUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .await()
            
            val user = authResult.user ?: throw Exception("Login failed")
            
            Timber.d("User logged in: ${user.email}")
            Result.success(user)
        } catch (e: Exception) {
            Timber.e(e, "Login failed")
            Result.failure(e)
        }
    }
    
    /**
     * Login with Google
     */
    suspend fun loginWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            
            val user = authResult.user ?: throw Exception("Google login failed")
            
            // Check if profile exists, create if not
            val profileExists = checkUserProfileExists(user.uid)
            if (!profileExists) {
                val profile = UserProfile(
                    userId = user.uid,
                    email = user.email ?: "",
                    name = user.displayName ?: "",
                    contact = user.phoneNumber,
                    district = "",
                    location = null,
                    createdAt = System.currentTimeMillis()
                )
                saveUserProfile(profile)
            }
            
            Timber.d("Google login successful: ${user.email}")
            Result.success(user)
        } catch (e: Exception) {
            Timber.e(e, "Google login failed")
            Result.failure(e)
        }
    }
    
    /**
     * Logout user
     */
    fun logout() {
        firebaseAuth.signOut()
        Timber.d("User logged out")
    }
    
    /**
     * Send password reset email
     */
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Timber.d("Password reset email sent to: $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Password reset failed")
            Result.failure(e)
        }
    }
    
    /**
     * Save user profile to Firestore
     */
    private suspend fun saveUserProfile(profile: UserProfile) {
        try {
            firestore.collection("users")
                .document(profile.userId)
                .set(profile)
                .await()
            Timber.d("User profile saved")
        } catch (e: Exception) {
            Timber.e(e, "Failed to save user profile")
            throw e
        }
    }
    
    /**
     * Get user profile from Firestore
     */
    suspend fun getUserProfile(userId: String): Result<UserProfile> {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            val profile = document.toObject(UserProfile::class.java)
                ?: throw Exception("Profile not found")
            
            Result.success(profile)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get user profile")
            Result.failure(e)
        }
    }
    
    /**
     * Update user profile
     */
    suspend fun updateUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(profile.userId)
                .set(profile)
                .await()
            
            Timber.d("User profile updated")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to update profile")
            Result.failure(e)
        }
    }
    
    /**
     * Check if user profile exists
     */
    private suspend fun checkUserProfileExists(userId: String): Boolean {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            document.exists()
        } catch (e: Exception) {
            Timber.e(e, "Failed to check profile existence")
            false
        }
    }
}
