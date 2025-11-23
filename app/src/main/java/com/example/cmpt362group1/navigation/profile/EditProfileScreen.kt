package com.example.cmpt362group1.navigation.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.cmpt362group1.database.ImageStoragePath
import com.example.cmpt362group1.database.ImageViewModel
import com.example.cmpt362group1.database.User
import com.example.cmpt362group1.database.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavHostController,
    userViewModel: UserViewModel,
    userProfile: User
) {
    val context = LocalContext.current
    val imageViewModel: ImageViewModel = viewModel()

    var name by remember { mutableStateOf(
        userProfile.displayName.ifEmpty {
            if (userProfile.firstName.isNotEmpty() || userProfile.lastName.isNotEmpty()) {
                "${userProfile.firstName} ${userProfile.lastName}".trim()
            } else ""
        }
    )}

    var username by remember { mutableStateOf(userProfile.username) }
    var pronouns by remember { mutableStateOf(userProfile.pronouns.ifEmpty { "None" }) }
    var bio by remember { mutableStateOf(userProfile.description) }
    var link by remember { mutableStateOf(userProfile.link) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var profilePhotoUrl by remember { mutableStateOf(userProfile.photoUrl) }
    var expanded by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            imageViewModel.uploadImage(
                uri,
                context,
                ImageStoragePath.UserProfile
            ) { downloadUrl ->
                profilePhotoUrl = downloadUrl
                userViewModel.updateUser(userProfile.id, mapOf("photoUrl" to downloadUrl))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Edit profile", textAlign = TextAlign.Center)
                    }
                },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.primary)
                    }
                },colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    TextButton(
                        onClick = {
                            val updates = mutableMapOf<String, Any>()
                            updates["displayName"] = name
                            updates["username"] = username
                            updates["pronouns"] = if (pronouns == "None") "" else pronouns
                            updates["description"] = bio
                            updates["link"] = link

                            val nameParts = name.trim().split(" ", limit = 2)
                            if (nameParts.isNotEmpty()) {
                                updates["firstName"] = nameParts[0]
                                updates["lastName"] = if (nameParts.size > 1) nameParts[1] else ""
                            }

                            userViewModel.updateUser(userProfile.id, updates)
                            navController.popBackStack()
                        }
                    ) {
                        Text("Done", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(selectedImageUri ?: profilePhotoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.LightGray, CircleShape)
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Change profile photo",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
            }

            Spacer(Modifier.height(24.dp))

            @Composable
            fun FieldCard(content: @Composable ColumnScope.() -> Unit) {
                val cardShape = RoundedCornerShape(12.dp)

                Surface(
                    color = Color.White,
                    shape = cardShape,
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(cardShape)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        content = content
                    )
                }
            }

            FieldCard {
                Text("Name", fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Name") }
                )
            }

            FieldCard {
                Text("Username", fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Username") }
                )
            }

            FieldCard {
                Text("Pronouns", fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(4.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = pronouns,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        singleLine = true
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf("None", "She/her", "He/him", "They/them").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    pronouns = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            FieldCard {
                Text("Bio", fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 5,
                    placeholder = { Text("Bio") }
                )
            }

            FieldCard {
                Text("Links", fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(8.dp))

                if (link.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLinkDialog = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Add Link", color = MaterialTheme.colorScheme.primary)
                        Text("+", color = Color.Gray, fontSize = 20.sp)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(link, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Row {
                            TextButton(onClick = { showLinkDialog = true }) {
                                Text("Edit", fontSize = 12.sp)
                            }
                            TextButton(onClick = { link = "" }) {
                                Text("Remove", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        if (showLinkDialog) {
            var tempLink by remember { mutableStateOf(link) }

            AlertDialog(
                onDismissRequest = { showLinkDialog = false },
                title = { Text("Add Link") },
                text = {
                    OutlinedTextField(
                        value = tempLink,
                        onValueChange = { tempLink = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter URL") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            link = tempLink
                            showLinkDialog = false
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text("Save", fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showLinkDialog = false },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}
