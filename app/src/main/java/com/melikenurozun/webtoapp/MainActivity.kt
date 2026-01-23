package com.melikenurozun.webtoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.melikenurozun.webtoapp.data.remote.NewsCategories
import com.melikenurozun.webtoapp.ui.detail.DetailScreen
import com.melikenurozun.webtoapp.ui.favorites.FavoritesScreen
import com.melikenurozun.webtoapp.ui.home.HomeScreen
import com.melikenurozun.webtoapp.ui.readlater.ReadLaterScreen
import com.melikenurozun.webtoapp.ui.settings.SettingsScreen
import com.melikenurozun.webtoapp.ui.navigation.Screen
import com.melikenurozun.webtoapp.ui.theme.WebtoAppTheme
import com.melikenurozun.webtoapp.worker.NotificationWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install Splash Screen
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // Schedule Notification Worker
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "DailyNewsNotification",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }
            var isAmoledMode by remember { mutableStateOf(false) }
            val systemDarkTheme = isSystemInDarkTheme()
            
            LaunchedEffect(Unit) {
                isDarkTheme = systemDarkTheme
            }

            WebtoAppTheme(
                darkTheme = isDarkTheme,
                amoledMode = isAmoledMode
            ) {
                MainScreen(
                    isDarkTheme = isDarkTheme,
                    isAmoledMode = isAmoledMode,
                    onThemeChanged = { isDarkTheme = it },
                    onAmoledChanged = { isAmoledMode = it }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isDarkTheme: Boolean,
    isAmoledMode: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    onAmoledChanged: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // *** Kategori state'i burada tutulacak ***
    var currentCategoryId by remember { mutableStateOf("news") }
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                
                // User Profile Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = androidx.compose.foundation.shape.CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "WebtoApp News",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Guest User",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                HorizontalDivider()
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // Kategoriler için özel işlem
                    NewsCategories.allCategories.forEach { category ->
                        NavigationDrawerItem(
                            label = { Text(text = category.name) },
                            selected = currentCategoryId == category.id && currentRoute?.startsWith("home") == true,
                            onClick = {
                                scope.launch { drawerState.close() }
                                // Kategoriyi güncelle
                                currentCategoryId = category.id
                                // Home'a git (eğer değilse)
                                if (currentRoute?.startsWith("home") != true) {
                                    navController.navigate("home_screen") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Text(text = getCategoryEmoji(category.name)) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Text(
                        text = "Library",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // Favorites
                    NavigationDrawerItem(
                        label = { Text(text = "Favorites") },
                        selected = currentRoute == Screen.Favorites.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Screen.Favorites.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Default.Favorite, contentDescription = "Favorites") },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    
                    // Read Later
                    NavigationDrawerItem(
                        label = { Text(text = "Read Later") },
                        selected = currentRoute == Screen.ReadLater.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Screen.ReadLater.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Default.Bookmark, contentDescription = "Read Later") },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    
                    // Settings
                    NavigationDrawerItem(
                        label = { Text(text = "Settings") },
                        selected = currentRoute == Screen.Settings.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Screen.Settings.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings") },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route
        ) {
            // Auth Screens
            composable(route = Screen.Login.route) {
                com.melikenurozun.webtoapp.ui.auth.LoginScreen(
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onLoginSuccess = {
                        navController.navigate("home_screen") {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onContinueAsGuest = {
                        navController.navigate("home_screen") {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(route = Screen.Register.route) {
                com.melikenurozun.webtoapp.ui.auth.RegisterScreen(
                    onNavigateToLogin = {
                        navController.popBackStack()
                    },
                    onRegisterSuccess = {
                        navController.navigate("home_screen") {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(route = "home_screen") {
                // currentCategoryId doğrudan kullanılıyor, navigation argümanı değil
                HomeScreen(
                    selectedCategoryId = currentCategoryId,
                    onNavigateToDetail = { article ->
                        navController.navigate(
                            Screen.Detail.createRoute(
                                title = article.title,
                                description = article.description,
                                imageUrl = article.imageUrl,
                                articleUrl = article.link,
                                pubDate = article.pubDate
                            )
                        )
                    },
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    }
                )
            }
            
            composable(route = Screen.Favorites.route) {
                FavoritesScreen(
                    onNavigateToDetail = { article ->
                        navController.navigate(
                            Screen.Detail.createRoute(
                                title = article.title,
                                description = article.description,
                                imageUrl = article.imageUrl,
                                articleUrl = article.link,
                                pubDate = article.pubDate
                            )
                        )
                    },
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    }
                )
            }
            
            composable(route = Screen.ReadLater.route) {
                ReadLaterScreen(
                    onNavigateToDetail = { article ->
                        navController.navigate(
                            Screen.Detail.createRoute(
                                title = article.title,
                                description = article.description,
                                imageUrl = article.imageUrl,
                                articleUrl = article.link,
                                pubDate = article.pubDate
                            )
                        )
                    },
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    }
                )
            }
            
            composable(route = Screen.Settings.route) {
                SettingsScreen(
                    isDarkMode = isDarkTheme,
                    onDarkModeChanged = onThemeChanged,
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(
                route = Screen.Detail.route,
                arguments = listOf(
                    navArgument("title") { type = NavType.StringType },
                    navArgument("description") { type = NavType.StringType },
                    navArgument("imageUrl") { type = NavType.StringType },
                    navArgument("articleUrl") { type = NavType.StringType },
                    navArgument("pubDate") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val title = backStackEntry.arguments?.getString("title")?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) } ?: ""
                val description = backStackEntry.arguments?.getString("description")?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) } ?: ""
                val imageUrlArg = backStackEntry.arguments?.getString("imageUrl")
                val imageUrl = if (imageUrlArg == "null") null else imageUrlArg?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }
                val articleUrl = backStackEntry.arguments?.getString("articleUrl")?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) } ?: ""
                val pubDate = backStackEntry.arguments?.getString("pubDate")?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) } ?: ""

                DetailScreen(
                    title = title,
                    description = description,
                    imageUrl = imageUrl,
                    articleUrl = articleUrl,
                    pubDate = pubDate,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

fun getCategoryEmoji(title: String): String {
    return when(title.lowercase()) {
        "news" -> "📰"
        "world" -> "🌍"
        "business" -> "💼"
        "technology" -> "💻"
        "science" -> "🔬"
        "entertainment" -> "🎬"
        "sport" -> "⚽"
        else -> "📰"
    }
}