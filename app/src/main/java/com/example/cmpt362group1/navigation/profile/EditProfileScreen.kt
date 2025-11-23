package com.example.cmpt362group1.navigation.profile

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavHostController,
    userViewModel: UserViewModel,
    userProfile: User
) {
    val context=LocalContext.current
    val imageViewModel: ImageViewModel = viewModel()

    var name by remember { mutableStateOf(
        userProfile.displayName.ifEmpty {
            if (userProfile.firstName.isNotEmpty() || userProfile.lastName.isNotEmpty()) {
                "${userProfile.firstName} ${userProfile.lastName}".trim()

            } else {
                ""
            }
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

    // Photo choice
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            // Upload image
            imageViewModel.uploadImage(
                uri,
                context,
                ImageStoragePath.UserProfile,
                { downloadUrl ->
                    profilePhotoUrl = downloadUrl
                    userViewModel.updateUser(userProfile.id,mapOf("photoUrl" to downloadUrl))
                }
            )
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
                        Text(
                            text = "Edit profile",
                            textAlign = TextAlign.Center
                        )
                    }
                },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val updates = mutableMapOf<String, Any>()
                            updates["displayName"] = name
                            updates["username"] = username
                            updates["pronouns"] = if (pronouns == "None") "" else pronouns
                            updates["description"] = bio
                            updates["link"] = link

                            // Parse first and last name
                            val nameParts = name.trim().split(" ", limit = 2)
                            if (nameParts.isNotEmpty()) {
                                updates["firstName"] = nameParts[0]
                                updates["lastName"] = if (nameParts.size > 1) nameParts[1] else ""
                            }

                            userViewModel.updateUser(userProfile.id, updates)
                            navController.popBackStack()
                        }
                    ) {
                        Text("Done", color= MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile photo
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(selectedImageUri ?: profilePhotoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.LightGray, CircleShape)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Change profile photo",
                    color= MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        photoPicker.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }
                )
            }

            HorizontalDivider()

            // Name
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Name",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value =name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Name") }
                )
            }

            HorizontalDivider()

            // Username
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Username",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Username") }
                )
            }

            HorizontalDivider()

            // Pronouns
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Pronouns",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = pronouns,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = "Dropdown"
                            )
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

            HorizontalDivider()

            // Bio
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Bio",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio=it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 5,
                    placeholder = { Text("Bio") }
                )
            }

            HorizontalDivider()

            // Links
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Links",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (link.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLinkDialog= true }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Add Link",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "+",
                            color = Color.Gray,
                            fontSize = 20.sp
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = link,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row {
                            TextButton(onClick = { showLinkDialog =true }) {
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

        // Link
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
                confirmButton= {
                    TextButton(
                        onClick = {
                            link = tempLink
                            showLinkDialog = false
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton= {
                    TextButton(onClick = { showLinkDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}