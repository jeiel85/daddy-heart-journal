package com.jeiel.daddyheartjournal.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.jeiel.daddyheartjournal.ui.theme.PrimaryEspresso
import com.jeiel.daddyheartjournal.ui.theme.SecondaryTerracotta
import com.jeiel.daddyheartjournal.ui.theme.SoftCreamBg
import com.jeiel.daddyheartjournal.ui.theme.WarmDarkText
import com.jeiel.daddyheartjournal.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(
    viewModel: MainViewModel
) {
    val navController = rememberNavController()

    val isUnlocked by viewModel.isUnlocked.collectAsState()
    val lockSetting by viewModel.lockSetting.collectAsState()
    val isPlayingNow by viewModel.isPlayingNow.collectAsState()

    // Trigger initial locks audit
    LaunchedEffect(Unit) {
        viewModel.checkLockRequirement()
    }

    if (!isUnlocked && lockSetting != null && lockSetting!!.isEnabled) {
        PinLockScreen(
            onUnlockSuccess = { viewModel.verifyPin(lockSetting!!.pinHash) },
            correctPin = lockSetting!!.pinHash
        )
    } else {
        Scaffold(
            containerColor = SoftCreamBg,
            bottomBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    val navItems = listOf(
                        Quadruple("home", "홈", Icons.Default.Home, "홈 버튼"),
                        Quadruple("timeline", "기록장", Icons.Default.Menu, "전체 목록"),
                        Quadruple("write", "기록하기", Icons.Default.Add, "기록 시작"),
                        Quadruple("capsule", "타임캡슐", Icons.Default.Lock, "타임캡슐 목록"),
                        Quadruple("settings", "설정", Icons.Default.Settings, "앱 설정")
                    )

                    navItems.forEach { (route, label, icon, contentDesc) ->
                        val isSelected = currentRoute?.startsWith(route) == true

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (isPlayingNow) {
                                    viewModel.stopAudioPlayback()
                                }

                                if (currentRoute != route) {
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = contentDesc,
                                    modifier = Modifier.size(28.dp),
                                    tint = if (isSelected) PrimaryEspresso else WarmDarkText.copy(alpha = 0.5f)
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) PrimaryEspresso else WarmDarkText.copy(alpha = 0.6f)
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = PrimaryEspresso.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                composable(route = "home") {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToWrite = { targetId, targetRelation ->
                            val route = if (targetId != null) {
                                "write?targetId=$targetId&targetRelation=$targetRelation"
                            } else {
                                "write"
                            }
                            navController.navigate(route)
                        },
                        onNavigateToTimeline = {
                            navController.navigate("timeline")
                        },
                        onPlayAudio = { path -> viewModel.playAudio(path) },
                        isPlaying = isPlayingNow,
                        onStopAudio = { viewModel.stopAudioPlayback() }
                    )
                }

                composable(
                    route = "write?targetId={targetId}&targetRelation={targetRelation}",
                    arguments = listOf(
                        navArgument("targetId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        },
                        navArgument("targetRelation") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val argumentTargetIdStr = backStackEntry.arguments?.getString("targetId")
                    val argumentTargetId = argumentTargetIdStr?.toIntOrNull()
                    val argumentTargetRelation = backStackEntry.arguments?.getString("targetRelation")

                    WriteScreen(
                        viewModel = viewModel,
                        initialTargetMemberId = argumentTargetId,
                        initialTargetRelationName = argumentTargetRelation,
                        onSaveSuccess = {
                            navController.navigate("timeline") {
                                popUpTo("home")
                            }
                        }
                    )
                }

                // fallback direct write navigations
                composable(route = "write") {
                    WriteScreen(
                        viewModel = viewModel,
                        initialTargetMemberId = null,
                        initialTargetRelationName = null,
                        onSaveSuccess = {
                            navController.navigate("timeline") {
                                popUpTo("home")
                            }
                        }
                    )
                }

                composable(route = "timeline") {
                    TimelineScreen(
                        viewModel = viewModel,
                        onPlayAudio = { path -> viewModel.playAudio(path) },
                        isPlaying = isPlayingNow,
                        onStopAudio = { viewModel.stopAudioPlayback() }
                    )
                }

                composable(route = "capsule") {
                    TimeCapsuleScreen(
                        viewModel = viewModel,
                        onPlayAudio = { path -> viewModel.playAudio(path) },
                        isPlaying = isPlayingNow,
                        onStopAudio = { viewModel.stopAudioPlayback() }
                    )
                }

                composable(route = "settings") {
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}

// Simple Quadruple structural helper
data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

