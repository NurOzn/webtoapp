package com.melikenurozun.webtoapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface AuthRepository {
    val currentUser: FirebaseUser?
    val isLoggedIn: Boolean
    val userEmail: String?
    val userName: String?
    fun authStateFlow(): Flow<FirebaseUser?>
    suspend fun login(email: String, password: String): Result<FirebaseUser>
    suspend fun register(email: String, password: String): Result<FirebaseUser>
    suspend fun deleteAccount(): Result<Unit>
    fun logout()
}

@Singleton
class AuthRepositoryImpl @Inject constructor() : AuthRepository {
    
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    override val currentUser: FirebaseUser?
        get() = auth.currentUser
    
    override val isLoggedIn: Boolean
        get() = auth.currentUser != null
    
    override val userEmail: String?
        get() = auth.currentUser?.email
    
    override val userName: String?
        get() = auth.currentUser?.displayName ?: auth.currentUser?.email?.substringBefore("@")
    
    override fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }
    
    override suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Login failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun register(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Registration failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            auth.currentUser?.delete()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun logout() {
        auth.signOut()
    }
}
