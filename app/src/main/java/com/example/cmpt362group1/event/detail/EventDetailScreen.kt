package com.example.cmpt362group1.event.detail

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.cmpt362group1.auth.AuthViewModel
import com.example.cmpt362group1.database.Comment
import com.example.cmpt362group1.database.Event
import com.example.cmpt362group1.database.EventViewModel
import com.example.cmpt362group1.database.UserUiState
import com.example.cmpt362group1.database.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    eventViewModel: EventViewModel,
    userViewModel: UserViewModel,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onEditEvent: (String) -> Unit = {}
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
        topBar = {
            TopAppBar(
                title = { Text(text = "Event Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
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
                title = { Text("Delete event?") },
                text = {
                    Text("This will remove the event for all participants. This action cannot be undone.")
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
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("Cancel")
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
            EventHeaderImagePlaceholder(
                imageUrl = event.imageUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = event.title,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(8.dp))

            if (isHost) {
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
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (event.dressCode.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Dress Code: ${event.dressCode}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onToggleJoin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isJoined) "Remove from Planner" else "Add to Planner")
            }

            if (isJoined) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onToggleManualCheckIn(isCurrentUserCheckedIn) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (isCurrentUserCheckedIn)
                            "Cancel check-in"
                        else
                            "Manual check-in"
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Divider()
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
private fun HostSummaryPanel(
    participantsCount: Int,
    arrivedCount: Int,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val notArrived = (participantsCount - arrivedCount).coerceAtLeast(0)

    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Host view",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = "Total participants: $participantsCount",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Arrived: $arrivedCount   Not arrived: $notArrived",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Automatic GPS check-in can share this count.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onEditClick) {
                    Text("Edit event")
                }
                TextButton(
                    onClick = onDeleteClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
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
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = commentText,
            onValueChange = onCommentTextChange,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            placeholder = { Text("Write a comment...") },
            singleLine = false,
            maxLines = 3
        )

        Button(
            onClick = onSend,
            enabled = enabled
        ) {
            Text("Post")
        }
    }
}

@Composable
private fun CommentRow(
    comment: Comment,
    parent: Comment?,
    isHostComment: Boolean,
    onReplyClick: (Comment) -> Unit
) {
    val containerModifier = if (isHostComment) {
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(8.dp)
    } else {
        Modifier.fillMaxWidth()
    }

    val nameColor =
        if (isHostComment) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.primary

    val textColor =
        if (isHostComment) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = containerModifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (parent != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Reply to ${parent.userName.ifBlank { "Anonymous" }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = parent.text.take(60) +
                                    if (parent.text.length > 60) "..." else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            val nameLabel = buildString {
                append(comment.userName.ifBlank { "Anonymous" })
                if (isHostComment) {
                    append(" (Host)")
                }
            }

            Text(
                text = nameLabel,
                style = MaterialTheme.typography.labelMedium,
                color = nameColor
            )
            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
        }

        TextButton(
            onClick = { onReplyClick(comment) },
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = "Reply",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun EventHeaderImagePlaceholder(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Event image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = "No image",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
