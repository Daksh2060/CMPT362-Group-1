package com.example.cmpt362group1.navigation.explore.swipe

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.cmpt362group1.auth.AuthViewModel
import com.example.cmpt362group1.database.Event
import com.example.cmpt362group1.database.EventViewModel
import com.example.cmpt362group1.database.UserUiState
import com.example.cmpt362group1.database.UserViewModel
import com.example.cmpt362group1.navigation.explore.startDateTime
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeEventsScreen(
    eventViewModel: EventViewModel,
    userViewModel: UserViewModel,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val eventsState by eventViewModel.eventsState.collectAsState()
    val userState by userViewModel.userState.collectAsState()

    var skippedEventIds by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(Unit) {
        val uid = authViewModel.getUserId()
        if (uid != null) {
            userViewModel.loadUser(uid)
        }
    }

    val joinedIds: List<String> = (userState as? UserUiState.Success)?.user?.eventsJoined ?: emptyList()

    val allEvents: List<Event> = when (val state = eventsState) {
        is EventViewModel.EventsUiState.Success -> state.events
        else -> emptyList()
    }

    val candidateEvents by remember(allEvents, joinedIds, skippedEventIds) {
        derivedStateOf {
            val now = Date()
            allEvents.filter { event ->
                event.startDateTime() > now &&
                        !joinedIds.contains(event.id) &&
                        !skippedEventIds.contains(event.id)
            }
        }
    }

    val currentEvent = candidateEvents.firstOrNull()
    val nextEvent = candidateEvents.getOrNull(1)

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Swipe to Decide",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF5F5F5)
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(top = 16.dp, bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                eventsState is EventViewModel.EventsUiState.Loading ->
                    CircularProgressIndicator(color = Color.Black)

                currentEvent == null -> {
                    EmptyStateContent(onNavigateBack)
                }

                else -> {
                    if (nextEvent != null) {
                        StaticEventCard(
                            event = nextEvent,
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .aspectRatio(0.7f)
                                .offset(y = 12.dp)
                                .alpha(0.6f)
                        )
                    }

                    key(currentEvent.id) {
                        DraggableEventCard(
                            event = currentEvent,
                            onJoin = {
                                val uid = authViewModel.getUserId()
                                if (uid != null) {
                                    userViewModel.addJoinedEvent(uid, currentEvent.id)
                                }
                            },
                            onSkip = {
                                skippedEventIds = skippedEventIds + currentEvent.id
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DraggableEventCard(
    event: Event,
    onJoin: () -> Unit,
    onSkip: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val density = androidx.compose.ui.platform.LocalDensity.current

    val swipeThreshold = with(density) { (screenWidth * 0.15f).toPx() }
    val maxExitDistance = with(density) { (screenWidth * 1.5f).toPx() }

    val rotation by remember { derivedStateOf { offsetX.value / 20f } }

    val joinAlpha by remember { derivedStateOf { (offsetX.value / swipeThreshold).coerceIn(0f, 1f) } }
    val skipAlpha by remember { derivedStateOf { (-offsetX.value / swipeThreshold).coerceIn(0f, 1f) } }

    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .aspectRatio(0.7f)
            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
            .rotate(rotation)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                        }
                    },
                    onDragEnd = {
                        coroutineScope.launch {
                            val exitSpec = tween<Float>(
                                durationMillis = 200,
                                easing = androidx.compose.animation.core.FastOutLinearInEasing
                            )

                            val returnSpring = spring<Float>(
                                dampingRatio = 0.45f,
                                stiffness = 1200f
                            )

                            when {
                                offsetX.value > swipeThreshold -> {
                                    offsetX.animateTo(maxExitDistance, exitSpec)
                                    onJoin()
                                }
                                offsetX.value < -swipeThreshold -> {
                                    offsetX.animateTo(-maxExitDistance, exitSpec)
                                    onSkip()
                                }
                                else -> {
                                    offsetX.animateTo(0f, returnSpring)
                                }
                            }
                        }
                    }
                )
            }
    ) {
        StaticEventCard(event = event, modifier = Modifier.fillMaxSize())

        if (joinAlpha > 0) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black.copy(alpha = joinAlpha * 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Join",
                    tint = Color(0xFF4CAF50).copy(alpha = joinAlpha),
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                        .padding(20.dp)
                )
            }
        }

        if (skipAlpha > 0) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black.copy(alpha = skipAlpha * 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Skip",
                    tint = Color(0xFFE53935).copy(alpha = skipAlpha),
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                        .padding(20.dp)
                )
            }
        }
    }
}

@Composable
private fun StaticEventCard(
    event: Event,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp), spotColor = Color.Gray.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(0.65f)
                        .fillMaxWidth()
                        .background(Color(0xFFEEEEEE))
                ) {
                    if (event.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(event.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = event.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(0.35f)
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = event.location,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                maxLines = 1
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.DateRange,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "${event.startDate} • ${event.startTime}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                        }

                        if (event.description.isNotBlank()) {
                            Text(
                                text = event.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateContent(onNavigateBack: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(32.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .background(Color.Gray, CircleShape)
                .padding(16.dp),
            tint = Color.White
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "You're all caught up!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "There are no more events to swipe right now.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onNavigateBack,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            )
        ) {
            Text("Back to Explore", fontWeight = FontWeight.Bold)
        }
    }
}
