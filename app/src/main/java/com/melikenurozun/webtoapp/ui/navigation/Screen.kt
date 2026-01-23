package com.melikenurozun.webtoapp.ui.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object Register : Screen("register_screen")
    object Home : Screen("home_screen?category={category}") {
        fun createRoute(category: String = "news"): String {
            return "home_screen?category=$category"
        }
    }
    object Favorites : Screen("favorites_screen")
    object ReadLater : Screen("read_later_screen")
    object Settings : Screen("settings_screen")
    
    object Detail : Screen("detail_screen/{title}/{description}/{imageUrl}/{articleUrl}/{pubDate}") {
        fun createRoute(title: String, description: String, imageUrl: String?, articleUrl: String, pubDate: String): String {
            val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
            val encodedDesc = URLEncoder.encode(description, StandardCharsets.UTF_8.toString())
            val encodedImg = if (imageUrl != null) URLEncoder.encode(imageUrl, StandardCharsets.UTF_8.toString()) else "null"
            val encodedUrl = URLEncoder.encode(articleUrl, StandardCharsets.UTF_8.toString())
            val encodedDate = URLEncoder.encode(pubDate, StandardCharsets.UTF_8.toString())
            return "detail_screen/$encodedTitle/$encodedDesc/$encodedImg/$encodedUrl/$encodedDate"
        }
    }
}
