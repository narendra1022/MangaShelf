package com.example.mangashelf.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mangashelf.ui.screens.FavoriteMangaScreen
import com.example.mangashelf.ui.screens.MangaDetailScreen
import com.example.mangashelf.ui.screens.MangaListScreen

sealed class Screen(val route: String) {
    data object MangaList : Screen("manga_list")
    data object FavoriteManga : Screen("favorite_manga")
    data object MangaDetail : Screen("manga_detail/{mangaId}") {
        fun createRoute(mangaId: String) = "manga_detail/$mangaId"
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    currentRoute: String?
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Menu, contentDescription = "Manga List") },
            label = { Text("Manga List") },
            selected = currentRoute == Screen.MangaList.route,
            onClick = {
                if (currentRoute != Screen.MangaList.route) {
                    navController.navigate(Screen.MangaList.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Favorite, contentDescription = "Favorites") },
            label = { Text("Favorites") },
            selected = currentRoute == Screen.FavoriteManga.route,
            onClick = {
                if (currentRoute != Screen.FavoriteManga.route) {
                    navController.navigate(Screen.FavoriteManga.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            }
        )
    }
}

@Composable
fun MangaShelfApp() {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    Scaffold(
        bottomBar = {
            // Only show bottom navigation if we're on a root screen
            if (currentRoute == Screen.MangaList.route ||
                currentRoute == Screen.FavoriteManga.route
            ) {
                BottomNavigationBar(navController, currentRoute)
            }
        }
    ) { padding ->
        MangaShelfNavGraph(
            navController = navController,
            modifier = Modifier.padding(1.dp)
        )
    }
}

@Composable
fun MangaShelfNavGraph(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.MangaList.route,
        modifier = modifier
    ) {
        composable(Screen.MangaList.route) {
            MangaListScreen(
                onMangaClick = { mangaId ->
                    navController.navigate(Screen.MangaDetail.createRoute(mangaId))
                }
            )
        }

        composable(Screen.FavoriteManga.route) {
            FavoriteMangaScreen(
                onNavigateBack = { navController.navigateUp() },
                onMangaClick = { mangaId ->
                    navController.navigate(Screen.MangaDetail.createRoute(mangaId))
                }
            )
        }

        composable(
            route = Screen.MangaDetail.route,
            arguments = listOf(
                navArgument("mangaId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val mangaId = backStackEntry.arguments?.getString("mangaId")
                ?: return@composable
            MangaDetailScreen(
                mangaId = mangaId,
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}