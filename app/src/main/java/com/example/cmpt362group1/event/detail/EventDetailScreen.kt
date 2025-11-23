package com.example.cmpt362group1.event.detail

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.cmpt362group1.auth.AuthViewModel
import com.example.cmpt362group1.database.Comment
import com.example.cmpt362group1.database.Event
import com.example.cmpt362group1.database.EventViewModel
import com.example.cmpt362group1.database.UserUiState
import com.example.cmpt362group1.database.UserViewModel
import com.example.cmpt362group1.navigation.explore.weather.WeatherHelper
import com.example.cmpt362group1.navigation.explore.weather.WeatherRepository
import com.example.cmpt362group1.navigation.explore.weather.WeatherResult
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    eventViewModel: EventViewModel,
    userViewModel: UserViewModel,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onEditEvent: (String) -> Unit = {},
    allowEditDelete: Boolean = true
) {
    val eventState by eventViewModel.eventState.collectAsState()
    val userState by userViewModel.userState.collectAsState()
    val commentsState by eventViewModel.commentsState.collectAsState()
    val participantsCount by eventViewModel.participantsCount.collectAsState()
    val arrivedCount by eventViewModel.arrivedCount.collectAsState()
    val isCurrentUserCheckedIn by eventViewModel.isCurrentUserCheckedIn.collectAsState()

    LaunchedEffect(eventId) {
        eventViewModel.loadEvent(eventId)
        eventViewModel.startComments(eventId)
        eventViewModel.startParticipants(eventId)
    }

    var currentUserId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val uid = authViewModel.getUserId()
        currentUserId = uid
        if (uid != null) {
            userViewModel.loadUser(uid)
        }
    }

    LaunchedEffect(eventId, currentUserId) {
        eventViewModel.startCheckIns(eventId, currentUserId)
    }

    var isJoined by remember { mutableStateOf(false) }
    var currentUserName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userState, eventId) {
        val uid = authViewModel.getUserId()
        if (uid != null && userState is UserUiState.Success) {
            val user = (userState as UserUiState.Success).user
            isJoined = user.eventsJoined.contains(eventId)
            currentUserName = user.displayName.ifBlank { null }
        }
    }

    val comments: List<Comment> =
        when (commentsState) {
            is EventViewModel.CommentsUiState.Success ->
                (commentsState as EventViewModel.CommentsUiState.Success).comments
            else -> emptyList()
        }
    val commentsLoading = commentsState is EventViewModel.CommentsUiState.Loading
    val commentsError = (commentsState as? EventViewModel.CommentsUiState.Error)?.message

    var commentText by remember { mutableStateOf("") }
    var replyTo by remember { mutableStateOf<Comment?>(null) }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var eventIdPendingDelete by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(

                title = {
                    Text(
                        text = "Event Detail",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )

        }
    ) { innerPadding ->
        when (eventState) {
            is EventViewModel.EventUiState.Idle,
            is EventViewModel.EventUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is EventViewModel.EventUiState.Error -> {
                val message = (eventState as EventViewModel.EventUiState.Error).message
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            is EventViewModel.EventUiState.Success -> {
                val event = (eventState as EventViewModel.EventUiState.Success).event
                val isHost = currentUserId != null && event.createdBy == currentUserId
                val listState = rememberLazyListState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        EventDetailScrollableContent(
                            event = event,
                            isHost = isHost,
                            allowEditDelete = allowEditDelete,
                            isJoined = isJoined,
                            onToggleJoin = {
                                val uid = authViewModel.getUserId()
                                if (uid != null) {
                                    if (isJoined) {
                                        userViewModel.removeJoinedEvent(uid, event.id)
                                    } else {
                                        userViewModel.addJoinedEvent(uid, event.id)
                                    }
                                    isJoined = !isJoined
                                }
                            },
                            comments = comments,
                            commentsLoading = commentsLoading,
                            commentsError = commentsError,
                            participantsCount = participantsCount,
                            arrivedCount = arrivedCount,
                            isCurrentUserCheckedIn = isCurrentUserCheckedIn,
                            onToggleManualCheckIn = { currentlyCheckedIn ->
                                val uid = authViewModel.getUserId()
                                if (uid != null) {
                                    if (currentlyCheckedIn) {
                                        eventViewModel.cancelManualCheckIn(event.id, uid)
                                    } else {
                                        eventViewModel.manualCheckIn(event.id, uid)
                                    }
                                }
                            },
                            onReplyClick = { target ->
                                replyTo = target
                            },
                            listState = listState,
                            onEditEvent = { onEditEvent(event.id) },
                            onDeleteEvent = {
                                eventIdPendingDelete = event.id
                                showDeleteConfirm = true
                            }
                        )
                    }
                    val showScrollHint by remember {
                        derivedStateOf { listState.canScrollForward }
                    }
                    if (showScrollHint) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "▼",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Scroll for more",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (replyTo != null) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Replying to ${replyTo!!.userName.ifBlank { "Anonymous" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            TextButton(onClick = { replyTo = null }) {
                                Text("Cancel")
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    CommentInputBar(
                        commentText = commentText,
                        onCommentTextChange = { commentText = it },
                        enabled = currentUserName != null && commentText.isNotBlank(),
                        onSend = {
                            val text = commentText.trim()
                            if (text.isNotEmpty()) {
                                val uid = authViewModel.getUserId()
                                if (uid != null) {
                                    val name = currentUserName ?: "Anonymous"
                                    eventViewModel.postComment(
                                        event.id,
                                        uid,
                                        name,
                                        text,
                                        replyTo?.id
                                    )
                                }
                                commentText = ""
                                replyTo = null
                            }
                        }
                    )
                }
            }
        }

        if (showDeleteConfirm && eventIdPendingDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                containerColor = Color(0xFFF5F5F5),
                shape = RoundedCornerShape(16.dp),
                title = {
                    Text(
                        "Delete event?",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                },
                text = {
                    Text(
                        "This will remove the event for all participants. This action cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val id = eventIdPendingDelete!!
                            showDeleteConfirm = false
                            eventViewModel.deleteEvent(id) { success ->
                                if (success) {
                                    onNavigateBack()
                                }
                            }
                        },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(
                            "Delete",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteConfirm = false },
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            "Cancel",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun EventDetailScrollableContent(
    event: Event,
    isHost: Boolean,
    allowEditDelete: Boolean,
    isJoined: Boolean,
    onToggleJoin: () -> Unit,
    comments: List<Comment>,
    commentsLoading: Boolean,
    commentsError: String?,
    participantsCount: Int,
    arrivedCount: Int,
    isCurrentUserCheckedIn: Boolean,
    onToggleManualCheckIn: (Boolean) -> Unit,
    onReplyClick: (Comment) -> Unit,
    listState: LazyListState,
    onEditEvent: () -> Unit,
    onDeleteEvent: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Text(
                text = event.title,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(12.dp))

            EventHeaderImagePlaceholder(
                imageUrl = event.imageUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )

            Spacer(Modifier.height(16.dp))

            if (isHost && allowEditDelete) {
                HostSummaryPanel(
                    participantsCount = participantsCount,
                    arrivedCount = arrivedCount,
                    onEditClick = onEditEvent,
                    onDeleteClick = onDeleteEvent
                )
            } else {
                Text(
                    text = "Participants: $participantsCount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = event.location,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "${event.startDate} ${event.startTime}  -  ${event.endDate} ${event.endTime}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (event.description.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text("Description", style = MaterialTheme.typography.titleMedium)
                Text(event.description, style = MaterialTheme.typography.bodyMedium)
            }

            if (event.dressCode.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text("Dress Code: ${event.dressCode}", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(16.dp))
            event.latitude?.let { lat ->
                event.longitude?.let { lon ->
                    WeatherInfoPanel(
                        latitude = lat,
                        longitude = lon,
                        eventDate = event.startDate,
                        eventTime = event.startTime
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onToggleJoin,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isJoined) "Remove from Planner" else "Add to Planner")
            }
            if (isJoined) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onToggleManualCheckIn(isCurrentUserCheckedIn) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Text(
                        if (isCurrentUserCheckedIn) "Cancel check-in" else "Manual check-in"
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Comments",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(8.dp))
        }
        when {
            commentsLoading -> {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Loading comments...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            commentsError != null -> {
                item {
                    Text(
                        text = commentsError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            comments.isEmpty() -> {
                item {
                    Text(
                        text = "No comments yet. Be the first to comment.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                val commentMap = comments.associateBy { it.id }
                items(
                    items = comments,
                    key = { it.id }
                ) { c ->
                    val parent = c.parentId?.let { pid -> commentMap[pid] }
                    val isHostComment = c.userId == event.createdBy

                    CommentRow(
                        comment = c,
                        parent = parent,
                        isHostComment = isHostComment,
                        onReplyClick = onReplyClick
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun WeatherInfoPanel(
    latitude: Double,
    longitude: Double,
    eventDate: String,
    eventTime: String
) {
    var weatherData by remember { mutableStateOf<WeatherResult?>(null) }
    var weatherError by remember { mutableStateOf<String?>(null) }
    val repository = remember { WeatherRepository() }

    LaunchedEffect(latitude, longitude, eventDate, eventTime) {
        try {
            val inputFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")
            val eventDateTime = LocalDateTime.parse("$eventDate $eventTime", inputFormatter)

            repository.getWeatherForDateTime(
                latitude,
                longitude,
                dateTime = eventDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:00")),
                onSuccess = { weatherData = it },
                onError = { weatherError = it }
            )
        } catch (e: Exception) {
            weatherError = "Check back closer to the event date."
            Log.e("WeatherDebug", "Date parsing error", e)
        }
    }
    Column {
        Text(
            text = "Expected Weather",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        when {
            weatherData != null -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${weatherData!!.temperature.toInt()}°C",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val symbol = WeatherHelper.getWeatherSymbol(weatherData!!.condition.lowercase())
                    Text(symbol, fontSize = 24.sp)
                    Text(
                        text = weatherData!!.condition,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            weatherError != null -> {
                Text(
                    text = "Weather data unavailable",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = weatherError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text(
                        text = "Loading weather...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun HostSummaryPanel(
    participantsCount: Int,
    arrivedCount: Int,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val notArrived = (participantsCount - arrivedCount).coerceAtLeast(0)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF5F5F5),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Host view",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Total Participants",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$participantsCount",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = Color.LightGray
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Arrived",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$arrivedCount",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Not Arrived",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$notArrived",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = "* Automatic GPS check-in can share this count.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "EDIT Event",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                TextButton(
                    onClick = onDeleteClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "DELETE Event",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentInputBar(
    commentText: String,
    onCommentTextChange: (String) -> Unit,
    enabled: Boolean,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = commentText,
            onValueChange = onCommentTextChange,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
                .heightIn(min = 48.dp),
            placeholder = { Text("Write a comment...") },
            singleLine = false,
            maxLines = 3,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        )
        Button(
            onClick = onSend,
            enabled = enabled,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("Post", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun CommentRow(
    comment: Comment,
    parent: Comment? = null,
    isHostComment: Boolean,
    onReplyClick: (Comment) -> Unit
) {
    val commentBackgroundColor = Color(0xFFF5F5F5)
    val commentTextColor = MaterialTheme.colorScheme.onSurface

    val containerModifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(commentBackgroundColor)
        .padding(8.dp)

    Column(modifier = containerModifier) {
        parent?.let {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFE0E0E0),
                tonalElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = it.userName.ifBlank { "Anonymous" },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = it.text.take(60) + if (it.text.length > 60) "..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        val nameText = comment.userName.ifBlank { "Anonymous" }
        val displayName = if (isHostComment) "$nameText (Host) ⭐" else nameText

        Text(
            text = displayName,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 16.sp),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(4.dp))
        Text(
            text = comment.text,
            style = MaterialTheme.typography.bodyMedium,
            color = commentTextColor
        )
        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = { onReplyClick(comment) },
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = "Reply",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun EventHeaderImagePlaceholder(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    if (imageUrl.isNotBlank()) {
        val context = LocalContext.current
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Event image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}
