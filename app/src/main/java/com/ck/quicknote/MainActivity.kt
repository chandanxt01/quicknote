package com.ck.quicknote

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ck.quicknote.core.common.BiometricPromptManager
import com.ck.quicknote.core.common.PreferencesManager
import com.ck.quicknote.core.designsystem.theme.QuickNoteTheme
import com.ck.quicknote.feature.archive.ArchiveScreen
import com.ck.quicknote.feature.folder.FolderScreen
import com.ck.quicknote.feature.home.HomeScreen
import com.ck.quicknote.feature.note_detail.NoteDetailScreen
import com.ck.quicknote.feature.search.SearchScreen
import com.ck.quicknote.feature.settings.SettingsScreen
import com.ck.quicknote.feature.util.Screen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private var isAuthenticated by mutableStateOf(false)
    private lateinit var biometricPromptManager: BiometricPromptManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        biometricPromptManager = BiometricPromptManager(this)

        if (preferencesManager.isAppLockEnabled()) {
            authenticateUser()
        } else {
            isAuthenticated = true
        }

        setContent {
            QuickNoteTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    if (isAuthenticated) {
                        val navController = rememberNavController()

                        // Updated NavHost with Custom Animations
                        NavHost(
                            navController = navController,
                            startDestination = Screen.HomeScreen.route,
                            enterTransition = {
                                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400))
                            },
                            exitTransition = {
                                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400))
                            },
                            popEnterTransition = {
                                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400))
                            },
                            popExitTransition = {
                                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400))
                            }
                        ) {
                            composable(route = Screen.HomeScreen.route) {
                                HomeScreen(navController = navController)
                            }

                            composable(route = Screen.SearchScreen.route) {
                                SearchScreen(navController = navController)
                            }

                            composable(route = Screen.SettingsScreen.route) {
                                SettingsScreen(
                                    navController = navController,
                                    preferencesManager = preferencesManager
                                )
                            }

                            composable(route = Screen.ArchiveScreen.route) {
                                ArchiveScreen(navController = navController)
                            }

                            composable(route = Screen.FolderScreen.route) {
                                FolderScreen(navController = navController)
                            }

                            // Note Detail Screen (With Special Expansion-like Animation)
                            composable(
                                route = Screen.NoteDetailScreen.route +
                                        "?noteId={noteId}&noteColor={noteColor}",
                                arguments = listOf(
                                    navArgument(name = "noteId") {
                                        type = NavType.IntType
                                        defaultValue = -1
                                    },
                                    navArgument(name = "noteColor") {
                                        type = NavType.IntType
                                        defaultValue = -1
                                    }
                                ),
                                // Custom transition for Detail Screen to simulate "Expand" feel
                                enterTransition = {
                                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, animationSpec = tween(400))
                                },
                                exitTransition = {
                                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, animationSpec = tween(400))
                                },
                                popEnterTransition = {
                                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, animationSpec = tween(400)) // Keep consistent
                                },
                                popExitTransition = {
                                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, animationSpec = tween(400))
                                }
                            ) {
                                val color = it.arguments?.getInt("noteColor") ?: -1
                                NoteDetailScreen(
                                    navController = navController,
                                    noteColor = color
                                )
                            }
                        }
                    } else {
                        LockScreen(onRetry = { authenticateUser() })
                    }
                }
            }
        }
    }

    private fun authenticateUser() {
        biometricPromptManager.promptBiometricAuth(
            title = getString(R.string.unlock_quicknote),
            subTitle = getString(R.string.biometric_subtitle),
            negativeButtonText = getString(R.string.cancel),
            onSuccess = {
                isAuthenticated = true
            },
            onError = { error -> },
            onFailed = { }
        )
    }
}

@androidx.compose.runtime.Composable
fun LockScreen(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = stringResource(R.string.locked_icon_desc),
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.app_is_locked),
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.unlock_app_btn))
        }
    }
}