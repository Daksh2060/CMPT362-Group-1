package com.example.cmpt362group1.auth

import android.app.Activity
import com.example.cmpt362group1.R
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException


@Composable
fun BubbleText(
    text: String,
    fontSize: TextUnit = MaterialTheme.typography.headlineMedium.fontSize
) {
    Box {
        Text(
            text = text,
            fontSize = fontSize,
            color = Color.Black,
            style = LocalTextStyle.current.copy(
                shadow = Shadow(
                    color = Color.Black,
                    blurRadius = 2f,
                    offset = Offset(2f, 2f)
                )
            )
        )
        Text(
            text = text,
            fontSize = fontSize,
            color = Color(0x00000000)
        )
    }
}

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("657057707753-8k8h7mt0muj5a6b5kj4hsjsslef83djr.apps.googleusercontent.com")
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken

            if (idToken != null) {
                isLoading = true
                authViewModel.signInWithGoogle(
                    idToken = idToken,
                    onSuccess = {
                        isLoading = false
                        onLoginSuccess()
                    },
                    onError = { error ->
                        isLoading = false
                    }
                )
            }
        }
        isLoading = false
    }

    Scaffold(
        containerColor = Color.White
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.sfu_logo),
                        contentDescription = "App Logo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(90.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    BubbleText(
                        text = "Campus Event Explorer",
                        fontSize = 36.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .clickable(enabled = !isLoading) {
                            isLoading = true
                            launcher.launch(googleSignInClient.signInIntent)
                        }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(26.dp),
                                color = Color.Black
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.google_logo),
                                    contentDescription = "Google Logo",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = "Sign in with Google",
                                    color = Color.Black,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }

            }
        }
    }
}

