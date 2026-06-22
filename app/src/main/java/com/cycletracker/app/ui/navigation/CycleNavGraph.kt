package com.cycletracker.app.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.cycletracker.app.R
import com.cycletracker.app.core.designsystem.theme.CycleTheme
import com.cycletracker.app.core.designsystem.theme.cuteBackground
import com.cycletracker.app.core.lock.AppLockManager
import com.cycletracker.app.domain.model.AppSettings
import com.cycletracker.app.domain.model.CURRENT_DISCLAIMER_VERSION
import com.cycletracker.app.ui.RootViewModel
import com.cycletracker.app.ui.calendar.CalendarScreen
import com.cycletracker.app.ui.history.HistoryScreen
import com.cycletracker.app.ui.insights.InsightsScreen
import com.cycletracker.app.ui.lock.LockScreen
import com.cycletracker.app.ui.log.LogEntryScreen
import com.cycletracker.app.ui.onboarding.DisclaimerScreen
import com.cycletracker.app.ui.onboarding.OnboardingScreen
import com.cycletracker.app.ui.settings.AboutScreen
import com.cycletracker.app.ui.settings.SettingsScreen
import com.cycletracker.app.ui.today.TodayScreen
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

private data class TopDest(val route: Any, val labelRes: Int, val icon: ImageVector)

private fun todayEpochDay(): Long =
    Clock.System.todayIn(TimeZone.currentSystemDefault()).toEpochDays().toLong()

private fun postLockStart(s: AppSettings): Any = when {
    !s.onboardingComplete -> Onboarding
    s.disclaimerAcceptedVersion < CURRENT_DISCLAIMER_VERSION -> Disclaimer
    else -> Today
}

@Composable
fun CycleApp() {
    val rootViewModel: RootViewModel = hiltViewModel()
    val settings by rootViewModel.settings.collectAsStateWithLifecycle()
    val authenticated by rootViewModel.authenticated.collectAsStateWithLifecycle()
    val current = settings ?: return

    CycleTheme(themeMode = current.themeMode, seedColor = current.seedColor) {
        val nav = rememberNavController()
        val needsLock = current.appLockEnabled && !authenticated

        RequestNotificationPermission()

        val tops = listOf(
            TopDest(Today, R.string.tab_today, Icons.Filled.Home),
            TopDest(Calendar, R.string.tab_calendar, Icons.Filled.DateRange),
            TopDest(Insights, R.string.tab_insights, Icons.Filled.Favorite),
            TopDest(History, R.string.tab_history, Icons.Filled.List),
            TopDest(Settings, R.string.tab_settings, Icons.Filled.Settings),
        )
        val backStackEntry by nav.currentBackStackEntryAsState()
        val dest = backStackEntry?.destination
        val onTopLevel = tops.any { dest?.hasRoute(it.route::class) == true }
        val onLoggable = dest?.hasRoute(Today::class) == true || dest?.hasRoute(Calendar::class) == true

        // Force the lock screen when (re)locked, e.g. on returning from background.
        LaunchedEffect(needsLock, dest) {
            if (needsLock && dest != null && dest.hasRoute(Lock::class) != true) {
                nav.navigate(Lock) { popUpTo(0) }
            }
        }

        val start: Any = if (needsLock) Lock else postLockStart(current)

        Scaffold(
            bottomBar = {
                if (onTopLevel) {
                    NavigationBar {
                        tops.forEach { t ->
                            NavigationBarItem(
                                selected = dest?.hasRoute(t.route::class) == true,
                                onClick = {
                                    nav.navigate(t.route) {
                                        popUpTo(Today) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(t.icon, contentDescription = null) },
                                label = { Text(stringResource(t.labelRes)) },
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                if (onLoggable) {
                    FloatingActionButton(onClick = { nav.navigate(LogEntry(todayEpochDay())) }) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.action_log_today))
                    }
                }
            },
        ) { inner ->
            NavHost(navController = nav, startDestination = start, modifier = Modifier.padding(inner)) {
                composable<Lock> {
                    LockScreen(onUnlocked = {
                        AppLockManager.markAuthenticated()
                        nav.navigate(postLockStart(current)) { popUpTo(Lock) { inclusive = true } }
                    })
                }
                composable<Onboarding> {
                    OnboardingScreen(onComplete = { nav.navigate(Today) { popUpTo(Onboarding) { inclusive = true } } })
                }
                composable<Disclaimer> {
                    DisclaimerScreen(onAccept = { nav.navigate(Today) { popUpTo(Disclaimer) { inclusive = true } } })
                }
                composable<Today> { TodayScreen(onQuickLog = { nav.navigate(LogEntry(it)) }, onAddPeriod = { nav.navigate(Calendar) }) }
                composable<Calendar> { CalendarScreen(onDayClick = { nav.navigate(LogEntry(it)) }) }
                composable<Insights> { InsightsScreen() }
                composable<History> { HistoryScreen() }
                composable<Settings> { SettingsScreen(onOpenAbout = { nav.navigate(About) }) }
                composable<About> { AboutScreen(onBack = { nav.popBackStack() }) }
                composable<LogEntry> { entry ->
                    LogEntryScreen(dateEpochDay = entry.toRoute<LogEntry>().dateEpochDay, onBack = { nav.popBackStack() })
                }
            }
        }
    }
}

@Composable
private fun RequestNotificationPermission() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
