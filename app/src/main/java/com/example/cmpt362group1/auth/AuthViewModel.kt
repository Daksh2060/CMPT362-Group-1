package com.example.cmpt362group1.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.cmpt362group1.database.User
import com.example.cmpt362group1.database.UserViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel(
    private val userViewModel: UserViewModel = UserViewModel()
): ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        Log.d("INFO", "Signing in with Google")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                Log.d("INFO", "Sign in OK")
                val user = authResult.user!!
                if (authResult.additionalUserInfo?.isNewUser == true) {
                    createUserInFirestore(user, onSuccess, onError)
                }

                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e("AuthViewModel", "Sign in failed", exception)
                onError(exception.message ?: "Unknown error")
            }
    }

    fun signOut() { auth.signOut() }

    fun deleteUser(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .delete()
                .addOnSuccessListener {
                    signOut()
                    userViewModel.deleteUser(uid)
                }
        }
    }

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

    private fun deleteUserInProject(
        user: FirebaseUser,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = user.uid

        // firestore
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .delete()
            .addOnSuccessListener {
                // auth
                user.delete()
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onError(e.message ?: "Failed to delete user") }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to delete user data")
            }
    }

    private fun createUserInFirestore(
        user: FirebaseUser,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        userViewModel.createNewUser(user)
    }
}

sealed class AuthState {
    data object Loading : AuthState()
    data object Unauthenticated : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
}