package com.ben0551.visualtimeassistant.ui.navigation

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ben0551.visualtimeassistant.domain.model.Preset
import com.ben0551.visualtimeassistant.domain.model.ThemeId
import com.ben0551.visualtimeassistant.domain.model.TimerRequest
import com.ben0551.visualtimeassistant.domain.model.WarningProfileId
import com.ben0551.visualtimeassistant.ui.components.tvFocusScale
import com.ben0551.visualtimeassistant.ui.custom.CustomTimerScreen
import com.ben0551.visualtimeassistant.ui.home.HomeScreen
import com.ben0551.visualtimeassistant.ui.presets.PresetsScreen
import com.ben0551.visualtimeassistant.ui.running.RunningTimerScreen
import com.ben0551.visualtimeassistant.ui.settings.SettingsScreen
import com.ben0551.visualtimeassistant.ui.stats.StatsScreen

object Routes {
    const val HOME = "home"
    const val CUSTOM = "custom"
    const val PRESETS = "presets"
    const val STATS = "stats"
    const val SETTINGS = "settings"

    private const val RUNNING_BASE = "running"
    const val RUNNING_PATTERN =
        "$RUNNING_BASE/{totalSec}/{presetId}/{presetName}/{theme}/{profile}"

    fun running(
        totalSec: Int,
        presetId: Long?,
        presetName: String,
        theme: ThemeId,
        profile: WarningProfileId,
    ): String = "$RUNNING_BASE/$totalSec/${presetId ?: -1L}/${Uri.encode(presetName)}/${theme.name}/${profile.name}"

    fun preset(preset: Preset): String =
        running(preset.durationSec, preset.id, preset.name, preset.theme, preset.warningProfile)

    fun duration(
        seconds: Int,
        theme: ThemeId,
        name: String = "",
        presetId: Long? = null,
    ): String = running(seconds, presetId, name, theme, WarningProfileId.DEFAULT)
}

@Composable
fun AppNavHost(
    initialTimerRequest: TimerRequest? = null,
    onTimerRequestConsumed: () -> Unit = {},
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Automation entry point: a deep link/intent launches straight into the timer.
    LaunchedEffect(initialTimerRequest) {
        initialTimerRequest?.let { request ->
            runCatching {
                // Replace an in-flight timer instead of stacking a second one.
                if (navController.currentDestination?.route?.startsWith("running/") == true) {
                    navController.popBackStack()
                }
                navController.navigate(
                    Routes.duration(
                        seconds = request.seconds,
                        theme = request.theme.resolve(),
                        name = request.presetName,
                        presetId = request.presetId,
                    ),
                )
            }.onFailure { error -> android.util.Log.e("VTALaunch", "navigate failed", error) }
            onTimerRequestConsumed()
        }
    }

    // The timer is full-screen with no chrome so the countdown owns the room.
    if (currentRoute?.startsWith("running/") == true) {
        RunningDestination(navController)
        return
    }

    AdaptiveShell(
        currentRoute = currentRoute,
        onNavigate = { route -> navController.navigateTopLevel(route) },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onStartDuration = { seconds, theme ->
                        navController.navigate(Routes.duration(seconds, theme))
                    },
                    onOpenCustom = { navController.navigate(Routes.CUSTOM) },
                    onOpenPresets = { navController.navigate(Routes.PRESETS) },
                    onStartPreset = { preset -> navController.navigate(Routes.preset(preset)) },
                )
            }
            composable(Routes.CUSTOM) {
                CustomTimerScreen(
                    onBack = { navController.popBackStack() },
                    onStart = { seconds, theme ->
                        navController.navigate(Routes.duration(seconds, theme))
                    },
                )
            }
            composable(Routes.PRESETS) {
                PresetsScreen(
                    onBack = { navController.popBackStack() },
                    onStartPreset = { preset -> navController.navigate(Routes.preset(preset)) },
                )
            }
            composable(Routes.STATS) { StatsScreen() }
            composable(Routes.SETTINGS) { SettingsScreen() }
            composable(
                route = Routes.RUNNING_PATTERN,
                arguments = listOf(
                    navArgument("totalSec") { type = NavType.StringType; defaultValue = "300" },
                    navArgument("presetId") { type = NavType.StringType; defaultValue = "-1" },
                    navArgument("presetName") { type = NavType.StringType; defaultValue = "" },
                    navArgument("theme") { type = NavType.StringType; defaultValue = "FOREST" },
                    navArgument("profile") { type = NavType.StringType; defaultValue = "DEFAULT" },
                ),
            ) {
                RunningDestination(navController)
            }
        }
    }
}

/** Tapping a shell destination returns to it without building up history. */
private fun NavHostController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun RunningDestination(navController: NavHostController) {
    val entry = navController.currentBackStackEntryAsState().value
    val args = entry?.arguments
    val totalSec = args?.getString("totalSec")?.toIntOrNull() ?: 300
    val presetId = args?.getString("presetId")?.toLongOrNull()?.takeIf { it > 0 }
    val presetName = Uri.decode(args?.getString("presetName") ?: "")
    val theme = ThemeId.fromName(args?.getString("theme"))
    val profile = WarningProfileId.fromName(args?.getString("profile"))

    RunningTimerScreen(
        totalSec = totalSec,
        presetId = presetId,
        presetName = presetName,
        theme = theme,
        warningProfile = profile,
        onExit = { navController.popBackStack() },
    )
}

@Composable
private fun AdaptiveShell(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth < 600.dp) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = { BottomBar(currentRoute, onNavigate) },
            ) { padding -> content(padding) }
        } else {
            // Wide layout (TV/tablet): tabs across the top so DPAD up/down moves between the
            // tab row and the options themselves - with a side rail, DOWN dead-ended on the
            // last tab and never reached the settings rows.
            Column(Modifier.fillMaxSize()) {
                TopTabs(currentRoute, onNavigate)
                Box(Modifier.weight(1f)) { content(PaddingValues(0.dp)) }
            }
        }
    }
}

private data class NavDestinationItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val destinations = listOf(
    NavDestinationItem(Routes.HOME, "Home", Icons.Rounded.Home),
    NavDestinationItem(Routes.PRESETS, "Presets", Icons.Rounded.FormatListBulleted),
    NavDestinationItem(Routes.STATS, "Statistics", Icons.Rounded.BarChart),
    NavDestinationItem(Routes.SETTINGS, "Settings", Icons.Rounded.Settings),
)

@Composable
private fun TopTabs(currentRoute: String?, onNavigate: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        destinations.forEach { item ->
            val selected = currentRoute == item.route
            val tabInteraction = remember { MutableInteractionSource() }
            Surface(
                selected = selected,
                onClick = { onNavigate(item.route) },
                interactionSource = tabInteraction,
                modifier = Modifier.weight(1f).height(72.dp).tvFocusScale(cornerRadius = 14.dp, interactionSource = tabInteraction),
                color = if (selected) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    Color.Transparent
                },
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        item.icon,
                        contentDescription = null,
                        tint = if (selected) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(26.dp),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        item.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    NavigationBar {
        destinations.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
            )
        }
    }
}

@Composable
private fun RailItems(currentRoute: String?, onNavigate: (String) -> Unit) {
    destinations.forEach { item ->
        NavigationRailItem(
            selected = currentRoute == item.route,
            onClick = { onNavigate(item.route) },
            icon = { Icon(item.icon, contentDescription = item.label) },
            label = { Text(item.label) },
        )
    }
}
