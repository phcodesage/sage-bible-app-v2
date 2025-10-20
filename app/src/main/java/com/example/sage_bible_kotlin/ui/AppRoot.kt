package com.example.sage_bible_kotlin.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sage_bible_kotlin.ui.home.HomeScreen
import com.example.sage_bible_kotlin.ui.placeholders.AiScreen
import com.example.sage_bible_kotlin.ui.placeholders.ProfileScreen
import com.example.sage_bible_kotlin.ui.placeholders.SearchScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.navigation.NavType
import androidx.navigation.navArgument
import java.net.URLDecoder
import java.net.URLEncoder
import com.example.sage_bible_kotlin.ui.reader.ReaderScreen

object Routes {
    const val Home = "home"
    const val Search = "search"
    const val Ai = "ai"
    const val Profile = "profile"
    const val Reader = "reader/{translation}/{book}/{chapter}"
    fun readerOf(translation: String, book: String, chapter: Int): String =
        "reader/${URLEncoder.encode(translation, Charsets.UTF_8)}/${URLEncoder.encode(book, Charsets.UTF_8)}/$chapter"
}

data class BottomItem(val route: String, val label: String, val icon: ImageVector)

@Composable
fun AppRoot() {
    val navController = rememberNavController()
    val items = listOf(
        BottomItem(Routes.Home, "Home", Icons.Filled.Home),
        BottomItem(Routes.Search, "Search", Icons.Filled.Search),
        BottomItem(Routes.Ai, "AI", Icons.Filled.SmartToy),
        BottomItem(Routes.Profile, "Profile", Icons.Filled.Person),
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentDestination.isTopLevelSelected(item.route),
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { inner ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home,
            modifier = Modifier
        ) {
            composable(Routes.Home) {
                HomeScreen(padding = inner) { translation, book, chapter ->
                    navController.navigate(Routes.readerOf(translation, book, chapter))
                }
            }
            composable(Routes.Search) { SearchScreen(padding = inner) }
            composable(Routes.Ai) { AiScreen(padding = inner) }
            composable(Routes.Profile) { ProfileScreen(padding = inner) }
            composable(
                route = Routes.Reader,
                arguments = listOf(
                    navArgument("translation") { type = NavType.StringType },
                    navArgument("book") { type = NavType.StringType },
                    navArgument("chapter") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val translation = URLDecoder.decode(backStackEntry.arguments?.getString("translation").orEmpty(), Charsets.UTF_8)
                val book = URLDecoder.decode(backStackEntry.arguments?.getString("book").orEmpty(), Charsets.UTF_8)
                val chapter = backStackEntry.arguments?.getInt("chapter") ?: 1
                ReaderScreen(
                    translation = translation,
                    book = book,
                    chapter = chapter,
                    padding = inner,
                    onBack = { navController.navigateUp() }
                )
            }
        }
    }
}

private fun NavDestination?.isTopLevelSelected(route: String): Boolean =
    this?.hierarchy?.any { it.route == route } == true
