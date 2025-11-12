package com.example.cmpt362group1.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel: ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        Log.d("INFO", "Signing in with Google")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                Log.d("INFO", "Sign in OK")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e("AuthViewModel", "Sign in failed", exception)
                onError(exception.message ?: "Unknown error")
            }
    }

    fun signOut() { auth.signOut() }

    fun getUser(): FirebaseUser? = auth.currentUser

    init {
        checkAuthState(auth)

        auth.addAuthStateListener { firebaseAuth ->
            checkAuthState(firebaseAuth)
        }
    }

    private fun checkAuthState(firebaseAuth: FirebaseAuth) {
        _authState.value = if (firebaseAuth.currentUser != null) {
            AuthState.Authenticated(firebaseAuth.currentUser!!)
        } else {
            AuthState.Unauthenticated
        }
    }
}

sealed class AuthState {
    data object Loading : AuthState()
    data object Unauthenticated : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
}