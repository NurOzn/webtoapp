package com.melikenurozun.webtoapp.ui.home

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.melikenurozun.webtoapp.data.remote.NewsCategories
import com.melikenurozun.webtoapp.domain.model.NewsArticle
import com.melikenurozun.webtoapp.ui.components.ErrorState
import com.melikenurozun.webtoapp.ui.components.ShimmerLoadingList
import com.melikenurozun.webtoapp.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToDetail: (NewsArticle) -> Unit,
    onMenuClick: () -> Unit,
    selectedCategoryId: String = "news"
) {
    val state by viewModel.state.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }
    
    // Pull to Refresh state
    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    // Update ViewModel when selected category changes
    LaunchedEffect(selectedCategoryId) {
        val category = NewsCategories.allCategories.find { it.id == selectedCategoryId } 
            ?: NewsCategories.allCategories.first()
        viewModel.selectCategory(category)
    }

    Scaffold(
        topBar = {
            // Modern Gradient TopBar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(GradientPurpleStart, GradientPurpleEnd)
                        )
                    )
            ) {
                if (isSearchActive) {
                    TopAppBar(
                        title = {
                            TextField(
                                value = searchQuery,
                                onValueChange = { viewModel.onSearchQueryChange(it) },
                                placeholder = { 
                                    Text(
                                        "Search news...", 
                                        color = Color.White.copy(alpha = 0.7f)
                                    ) 
                                },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    cursorColor = Color.White,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { 
                                isSearchActive = false 
                                viewModel.onSearchQueryChange("")
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Close Search",
                                    tint = Color.White
                                )
                            }
                        },
                        actions = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = Color.White
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                } else {
                    TopAppBar(
                        title = { 
                            Column {
                                Text(
                                    text = selectedCategory.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Stay updated with latest news",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onMenuClick) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tint = Color.White
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val currentState = state) {
                is NewsState.Loading -> {
                    ShimmerLoadingList()
                }
                is NewsState.Error -> {
                    ErrorState(
                        message = currentState.message,
                        onRetry = { viewModel.getNews() },
                        isNetworkError = currentState.message.contains("Network", ignoreCase = true)
                    )
                }
                is NewsState.Success -> {
                    if (currentState.news.isEmpty()) {
                        ErrorState(message = "No news articles available")
                    } else {
                        PullToRefreshBox(
                            isRefreshing = isRefreshing,
                            onRefresh = {
                                isRefreshing = true
                                viewModel.getNews()
                            },
                            state = pullToRefreshState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Reset refreshing when data loads
                            LaunchedEffect(currentState) {
                                delay(500)
                                isRefreshing = false
                            }
                            
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                itemsIndexed(
                                    items = currentState.news,
                                    key = { _, article -> article.id }
                                ) { index, article ->
                                    // Staggered Animation
                                    var visible by remember { mutableStateOf(false) }
                                    LaunchedEffect(Unit) {
                                        delay(index * 50L)
                                        visible = true
                                    }
                                    
                                    AnimatedVisibility(
                                        visible = visible,
                                        enter = fadeIn(animationSpec = tween(400)) +
                                                slideInVertically(
                                                    animationSpec = tween(400),
                                                    initialOffsetY = { it / 2 }
                                                )
                                    ) {
                                        ModernNewsCard(
                                            article = article,
                                            onClick = { onNavigateToDetail(article) },
                                            onFavoriteClick = { viewModel.toggleFavorite(article) },
                                            onReadLaterClick = { viewModel.toggleReadLater(article) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernNewsCard(
    article: NewsArticle,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onReadLaterClick: () -> Unit
) {
    val context = LocalContext.current
    
    // Press animation
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.4f)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = GradientPurpleStart.copy(alpha = 0.3f)
            )
            .clickable(
                onClick = onClick,
                onClickLabel = "Read article"
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // Large Hero Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                article.imageUrl?.let { imageUrl ->
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Gradient Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.7f)
                                    ),
                                    startY = 100f
                                )
                            )
                    )
                    
                    // Category Badge
                    Surface(
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.TopStart),
                        shape = RoundedCornerShape(20.dp),
                        color = GradientPurpleStart.copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = "📰 News",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    // Action Buttons - Glass Effect
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GlassIconButton(
                            onClick = onFavoriteClick,
                            icon = if (article.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            tint = if (article.isFavorite) SecondaryPink else Color.White
                        )
                        GlassIconButton(
                            onClick = onReadLaterClick,
                            icon = if (article.isReadLater) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            tint = if (article.isReadLater) TertiaryTeal else Color.White
                        )
                        GlassIconButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "${article.title}\n\n${article.link}")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share via"))
                            },
                            icon = Icons.Default.Share,
                            tint = Color.White
                        )
                    }
                    
                    // Title on Image
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
            
            // Content Section
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Date Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = formatDate(article.pubDate),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Description
                Text(
                    text = article.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Read More Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onClick,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Read More",
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .background(
                color = Color.Black.copy(alpha = 0.4f),
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

private fun formatDate(dateString: String): String {
    return try {
        if (dateString.contains(",")) {
            dateString.substringAfter(",").trim().substringBefore(" +").trim()
        } else {
            dateString.take(50)
        }
    } catch (e: Exception) {
        dateString
    }
}
