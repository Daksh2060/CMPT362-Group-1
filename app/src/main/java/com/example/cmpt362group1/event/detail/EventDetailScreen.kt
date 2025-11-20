package com.example.cmpt362group1.event.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
    onNavigateBack: () -> Unit
) {
    val eventState by eventViewModel.eventState.collectAsState()
    val userState by userViewModel.userState.collectAsState()
    val commentsState by eventViewModel.commentsState.collectAsState()
    val participantsCount by eventViewModel.participantsCount.collectAsState()

    LaunchedEffect(eventId) {
        eventViewModel.loadEvent(eventId)
        eventViewModel.startComments(eventId)
        eventViewModel.startParticipants(eventId)
    }

    LaunchedEffect(Unit) {
        val uid = authViewModel.getUserId()
        if (uid != null) {
            userViewModel.loadUser(uid)
        }
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
                            onReplyClick = { target ->
                                replyTo = target
                            }
                        )
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
                                    eventViewModel.postComment(event.id, uid, name, text, replyTo?.id)
                                }
                                commentText = ""
                                replyTo = null
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EventDetailScrollableContent(
    event: Event,
    isJoined: Boolean,
    onToggleJoin: () -> Unit,
    comments: List<Comment>,
    commentsLoading: Boolean,
    commentsError: String?,
    participantsCount: Int,
    onReplyClick: (Comment) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

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

            Text(
                text = "Participants: $participantsCount",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

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
                    CommentRow(
                        comment = c,
                        parent = parent,
                        onReplyClick = onReplyClick
                    )
                    Spacer(Modifier.height(8.dp))
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
    onReplyClick: (Comment) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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

            Text(
                text = comment.userName.ifBlank { "Anonymous" },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
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
