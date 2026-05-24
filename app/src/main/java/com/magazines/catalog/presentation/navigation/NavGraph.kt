package com.magazines.catalog.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.magazines.catalog.presentation.auth.LoginScreen
import com.magazines.catalog.presentation.auth.RegisterScreen
import com.magazines.catalog.presentation.catalog.CatalogScreen
import com.magazines.catalog.presentation.detail.MagazineDetailScreen
import com.magazines.catalog.presentation.favorites.FavoritesScreen
import com.magazines.catalog.presentation.mymagazines.MyMagazinesScreen
import com.magazines.catalog.presentation.profile.ProfileScreen
import com.magazines.catalog.presentation.splash.SplashScreen

private const val TAB_ANIMATION_MS = 200
private const val DETAIL_ANIMATION_MS = 300

private val tabFadeIn get() = fadeIn(animationSpec = tween(TAB_ANIMATION_MS))
private val tabFadeOut get() = fadeOut(animationSpec = tween(TAB_ANIMATION_MS))

@Composable
fun NavGraph(
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.AUTH_GRAPH,
    ) {
        navigation(
            route = Routes.AUTH_GRAPH,
            startDestination = Routes.SPLASH,
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(
                    onNavigateToMain = {
                        navController.navigate(Routes.MAIN_GRAPH) {
                            popUpTo(Routes.AUTH_GRAPH) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    },
                )
            }

            composable(Routes.LOGIN) {
                LoginScreen(
                    onNavigateToRegister = {
                        navController.navigate(Routes.REGISTER)
                    },
                    onLoginSuccess = {
                        navController.navigate(Routes.MAIN_GRAPH) {
                            popUpTo(Routes.AUTH_GRAPH) { inclusive = true }
                        }
                    },
                )
            }

            composable(Routes.REGISTER) {
                RegisterScreen(
                    onNavigateToLogin = {
                        navController.popBackStack()
                    },
                    onRegisterSuccess = {
                        navController.navigate(Routes.MAIN_GRAPH) {
                            popUpTo(Routes.AUTH_GRAPH) { inclusive = true }
                        }
                    },
                )
            }
        }

        composable(Routes.MAIN_GRAPH) {
            MainGraph()
        }
    }
}

@Composable
private fun MainGraph() {
    val mainNavController = rememberNavController()
    val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = shouldShowBottomBar(currentRoute)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController = mainNavController)
            }
        },
    ) { paddingValues ->
        NavHost(
            navController = mainNavController,
            startDestination = Routes.CATALOG,
            modifier = Modifier.padding(paddingValues),
        ) {
            composable(
                route = Routes.CATALOG,
                enterTransition = { tabFadeIn },
                exitTransition = { tabFadeOut },
                popEnterTransition = { tabFadeIn },
                popExitTransition = { tabFadeOut },
            ) {
                CatalogScreen(
                    onMagazineClick = { magazineId ->
                        mainNavController.navigate(Routes.magazineDetail(magazineId))
                    },
                )
            }

            composable(
                route = Routes.FAVORITES,
                enterTransition = { tabFadeIn },
                exitTransition = { tabFadeOut },
                popEnterTransition = { tabFadeIn },
                popExitTransition = { tabFadeOut },
            ) {
                FavoritesScreen()
            }

            composable(
                route = Routes.MY_MAGAZINES,
                enterTransition = { tabFadeIn },
                exitTransition = { tabFadeOut },
                popEnterTransition = { tabFadeIn },
                popExitTransition = { tabFadeOut },
            ) {
                MyMagazinesScreen()
            }

            composable(
                route = Routes.PROFILE,
                enterTransition = { tabFadeIn },
                exitTransition = { tabFadeOut },
                popEnterTransition = { tabFadeIn },
                popExitTransition = { tabFadeOut },
            ) {
                ProfileScreen()
            }

            composable(
                route = Routes.MAGAZINE_DETAIL,
                arguments = listOf(
                    navArgument("magazineId") { type = NavType.StringType },
                ),
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(DETAIL_ANIMATION_MS),
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(DETAIL_ANIMATION_MS),
                    )
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> -fullWidth },
                        animationSpec = tween(DETAIL_ANIMATION_MS),
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(DETAIL_ANIMATION_MS),
                    )
                },
            ) {
                MagazineDetailScreen(
                    onNavigateBack = { mainNavController.popBackStack() },
                    onUploadIssue = { id -> mainNavController.navigate(Routes.uploadIssue(id)) },
                    onIssueClick = { pdfUrl -> mainNavController.navigate(Routes.pdfViewer(pdfUrl)) },
                )
            }

            composable(
                route = Routes.PDF_VIEWER,
                arguments = listOf(
                    navArgument("pdfUrl") {
                        type = NavType.StringType
                        nullable = true
                    },
                ),
            ) { backStackEntry ->
                val pdfUrl = backStackEntry.arguments?.getString("pdfUrl").orEmpty()
                PlaceholderScreen(title = "PDF: $pdfUrl")
            }

            composable(Routes.UPLOAD_MAGAZINE) {
                PlaceholderScreen(title = "Загрузка журнала")
            }

            composable(
                route = Routes.UPLOAD_ISSUE,
                arguments = listOf(
                    navArgument("magazineId") { type = NavType.StringType },
                ),
            ) { backStackEntry ->
                val magazineId = backStackEntry.arguments?.getString("magazineId").orEmpty()
                PlaceholderScreen(title = "Загрузка выпуска: $magazineId")
            }

            composable(Routes.ADMIN) {
                PlaceholderScreen(title = "Админ-панель")
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = title)
    }
}
