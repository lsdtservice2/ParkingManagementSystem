package com.logisparktech.parkingmanagementsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.logisparktech.parkingmanagementsystem.data.local.preferences.PreferenceManager
import com.logisparktech.parkingmanagementsystem.presentation.entry.EntryScreen
import com.logisparktech.parkingmanagementsystem.presentation.entry.EntryViewModel
import com.logisparktech.parkingmanagementsystem.presentation.exit.ExitScreen
import com.logisparktech.parkingmanagementsystem.presentation.exit.ExitViewModel
import com.logisparktech.parkingmanagementsystem.presentation.login.LoginScreen
import com.logisparktech.parkingmanagementsystem.presentation.login.LoginViewModel
import com.logisparktech.parkingmanagementsystem.presentation.rate.RateScreen
import com.logisparktech.parkingmanagementsystem.presentation.rate.RateViewModel
import com.logisparktech.parkingmanagementsystem.presentation.recent_tickets.RecentTicketsScreen
import com.logisparktech.parkingmanagementsystem.presentation.recent_tickets.RecentTicketsViewModel
import com.logisparktech.parkingmanagementsystem.ui.theme.ParkingManagementSystemTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.logisparktech.parkingmanagementsystem.core.auth.AuthEventManager
import com.logisparktech.parkingmanagementsystem.core.sync.AutoSyncManager
import com.logisparktech.parkingmanagementsystem.core.worker.SyncReminderWorker
import com.logisparktech.parkingmanagementsystem.presentation.profile.ProfileScreen
import com.logisparktech.parkingmanagementsystem.presentation.profile.ProfileViewModel
import java.util.concurrent.TimeUnit
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.collectLatest

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Login : Screen("login", "Login", Icons.AutoMirrored.Filled.Login)
    data object Entry : Screen("entry", "Entry", Icons.Default.LocalParking)
    data object Exit : Screen("exit", "Exit", Icons.Default.QrCodeScanner)
    data object Rate : Screen("rate", "Rate", Icons.Default.DirectionsCar)
    data object RecentTicket : Screen("recent_ticket", "Tickets", Icons.Default.ConfirmationNumber)
    data object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var preferenceManager: PreferenceManager

    @Inject
    lateinit var authEventManager: AuthEventManager

    @Inject
    lateinit var autoSyncManager: AutoSyncManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        scheduleSyncReminder()
        autoSyncManager.startAutoSync()
        setContent {
            ParkingManagementSystemTheme {
                ParkingApp(preferenceManager, authEventManager)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        autoSyncManager.stopAutoSync()
    }

    private fun scheduleSyncReminder() {
        val syncReminderRequest = PeriodicWorkRequestBuilder<SyncReminderWorker>(
            1, TimeUnit.HOURS
        )
            .setInitialDelay(1, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "SyncReminderWork",
            ExistingPeriodicWorkPolicy.KEEP,
            syncReminderRequest
        )
    }
}


@Composable
fun ParkingApp(preferenceManager: PreferenceManager, authEventManager: AuthEventManager) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(Unit) {
        authEventManager.authEvents.collectLatest { event ->
            when (event) {
                is AuthEventManager.AuthEvent.Unauthorized -> {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

    val startDestination = if (preferenceManager.isLoggedIn()) {
        Screen.Entry.route
    } else {
        Screen.Login.route
    }

    val showBottomBar = currentRoute in listOf(
        Screen.Entry.route,
        Screen.Exit.route,
        Screen.Rate.route,
        Screen.RecentTicket.route,
        Screen.Profile.route
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                val viewModel: LoginViewModel = hiltViewModel(it)
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = {
                        navController.navigate(Screen.Entry.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Entry.route) {
                val viewModel: EntryViewModel = hiltViewModel(it)
                EntryScreen(
                    viewModel = viewModel,
                    onTicketGenerated = {
                        viewModel.resetState()
                    }
                )
            }
            composable(Screen.Exit.route) {
                val viewModel: ExitViewModel = hiltViewModel(it)
                ExitScreen(
                    viewModel = viewModel,
                    onExitSuccess = {
                        viewModel.resetState()
                    }
                )
            }
            composable(Screen.Rate.route) {
                val viewModel: RateViewModel = hiltViewModel(it)
                RateScreen(viewModel = viewModel)
            }
            composable(Screen.RecentTicket.route) {
                val viewModel: RecentTicketsViewModel = hiltViewModel(it)
                RecentTicketsScreen(viewModel = viewModel)
            }

            composable(Screen.Profile.route) {
                val viewModel: ProfileViewModel = hiltViewModel(it)
                ProfileScreen(
                    viewModel = viewModel,
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {

    val items = listOf(
        Screen.Entry,
        Screen.Exit,
        Screen.Rate,
        Screen.RecentTicket,
        Screen.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        items.forEach { screen ->

            val selected = currentRoute == screen.route

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Entry.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },

                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title,
                        modifier = Modifier.size(if (selected) 26.dp else 22.dp)
                    )
                },

                label = {
                    Text(
                        text = screen.title,
                        style = MaterialTheme.typography.labelSmall
                    )
                },

                alwaysShowLabel = true,

                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),

                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
