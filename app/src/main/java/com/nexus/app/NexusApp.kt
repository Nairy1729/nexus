package com.nexus.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.nexus.core.animation.LocalReducedMotion
import com.nexus.core.animation.rememberReducedMotion
import com.nexus.core.design.NexusDock
import com.nexus.core.design.NexusArea
import com.nexus.core.di.AppContainer
import com.nexus.core.haptics.LocalNexusHaptics
import com.nexus.core.haptics.NexusHaptics
import com.nexus.core.theme.NexusTheme
import com.nexus.core.theme.NexusThemeMode
import com.nexus.data.model.Contact
import com.nexus.data.model.ResolvedCall
import com.nexus.feature.activity.ActivityScreen
import com.nexus.feature.call.ActiveCallScreen
import com.nexus.feature.call.IncomingCallScreen
import com.nexus.feature.contact.ContactProfileScreen
import com.nexus.feature.dialer.DialerScreen
import com.nexus.feature.home.HomeScreen
import com.nexus.feature.people.PeopleScreen
import com.nexus.telephony.CallCommand
import kotlinx.coroutines.flow.collect

/**
 * NEXUS app shell: theme, haptics, reduced motion, navigation graph, dock, and the
 * call-session router.
 *
 * All navigation decisions live here — screens emit intents (onCallBack, onAnswer)
 * and the call session emits [CallCommand]s; no screen ever pushes routes itself,
 * which is what lets Phase 3 swap the mock telephony without touching feature UI.
 */
object Routes {
    const val Home = "home"
    const val People = "people"
    const val Dial = "dial"
    const val Activity = "activity"
    const val Contact = "contact"
    const val Incoming = "incoming"
    const val Active = "active"

    fun contact(id: String) = "$Contact/$id"
}

@Composable
fun NexusApp(
    container: AppContainer,
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var themeMode by remember {
        mutableStateOf(com.nexus.core.theme.NexusThemePreferences.getThemeMode(context))
    }
    var showThemeSelector by remember { mutableStateOf(false) }

    fun openThemeSelector() {
        showThemeSelector = true
    }

    fun selectTheme(mode: NexusThemeMode) {
        themeMode = mode
        com.nexus.core.theme.NexusThemePreferences.setThemeMode(context, mode)
    }

    NexusTheme(mode = themeMode) {
        val view = LocalView.current
        val haptics = remember { NexusHaptics(view) }
        val reduced = rememberReducedMotion()
        CompositionLocalProvider(
            LocalNexusHaptics provides haptics,
            LocalReducedMotion provides reduced,
        ) {
            // The canvas every screen paints on — inside the theme so it follows the
            // active theme identity (Obsidian Aurora, Graphite Lime, Midnight Burgundy).
            androidx.compose.material3.Surface(
                modifier = Modifier.fillMaxSize(),
                color = androidx.compose.material3.MaterialTheme.colorScheme.background,
            ) {
            val navController = rememberNavController()
            val session by container.callSessionController.session
                .collectAsStateWithLifecycle()

            // ---- Call session → navigation ---------------------------------
            LaunchedEffect(navController, container) {
                container.callSessionController.commands.collect { command ->
                    when (command) {
                        CallCommand.ShowIncoming -> navController.navigateSingleTop(Routes.Incoming)
                        CallCommand.ShowActive -> navController.navigateSingleTop(Routes.Active)
                        CallCommand.Dismiss -> navController.dismissIfTop(Routes.Incoming)
                        is CallCommand.Finish -> {
                            navController.dismissCallScreens()
                            // Land on the person's history — unless their profile is
                            // already what you're looking at.
                            val contactId = command.contactId
                            if (contactId != null) {
                                val top = navController.currentBackStackEntry?.destination?.route
                                if (top != Routes.contact(contactId)) {
                                    navController.navigate(Routes.contact(contactId)) {
                                        launchSingleTop = true
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Box(modifier = modifier.fillMaxSize()) {
                AppNavGraph(
                    navController = navController,
                    container = container,
                    session = session,
                    onToggleTheme = ::openThemeSelector,
                )

                // Dock: present on the four areas, gone on contact and call screens.
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route
                val area = NexusArea.from(currentRoute)
                AnimatedVisibility(
                    visible = area != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter),
                ) {
                    NexusDock(
                        selectedRoute = currentRoute,
                        onSelect = { picked ->
                            navController.navigate(picked.route) {
                                popUpTo(Routes.Home) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                // Spatial Identity / Multi-Theme Selector Sheet
                com.nexus.core.design.theme.NexusThemeSelectorSheet(
                    visible = showThemeSelector,
                    currentMode = themeMode,
                    onSelectMode = { selectedMode ->
                        selectTheme(selectedMode)
                    },
                    onDismiss = { showThemeSelector = false },
                )
            }
        }
    }
    }
}

@Composable
private fun AppNavGraph(
    navController: NavHostController,
    container: AppContainer,
    session: com.nexus.telephony.CallSession?,
    onToggleTheme: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Home,
        modifier = Modifier.fillMaxSize(),
    ) {
        composable(Routes.Home) {
            HomeScreen(
                onOpenContact = { id -> navController.navigate(Routes.contact(id)) },
                onOpenActivity = { navController.gotoArea(NexusArea.Activity) },
                onOpenPeople = { navController.gotoArea(NexusArea.People) },
                onOpenDial = { navController.gotoArea(NexusArea.Dial) },
                onCallBack = { call -> container.callFrom(call) },
                onSimulateIncoming = {
                    container.callSessionController.simulateIncoming("rahul")
                },
                container = container,
                onToggleTheme = onToggleTheme,
            )
        }

        composable(Routes.People) {
            PeopleScreen(
                onOpenContact = { id -> navController.navigate(Routes.contact(id)) },
                onCallBack = { contact -> container.callContact(contact) },
                container = container,
            )
        }

        composable(Routes.Dial) {
            DialerScreen(
                onCallBack = { digits, match -> container.callDialString(digits, match) },
                onOpenContact = { id -> navController.navigate(Routes.contact(id)) },
                container = container,
            )
        }

        composable(Routes.Activity) {
            ActivityScreen(
                onOpenContact = { id -> navController.navigate(Routes.contact(id)) },
                onCallBack = { call -> container.callFrom(call) },
                container = container,
            )
        }

        composable(
            route = "${Routes.Contact}/{contactId}",
            arguments = listOf(navArgument("contactId") { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString("contactId") ?: return@composable
            ContactProfileScreen(
                contactId = id,
                onBack = { navController.popBackStack() },
                onCallBack = { contact -> container.callContact(contact) },
                container = container,
            )
        }

        composable(Routes.Incoming) {
            val current = session ?: return@composable
            if (current.direction != com.nexus.telephony.CallDirection.Incoming) {
                return@composable
            }
            IncomingCallScreen(
                session = current,
                container = container,
                onAnswer = { container.callSessionController.accept() },
                onDecline = { container.callSessionController.decline() },
            )
        }

        composable(Routes.Active) {
            val current = session ?: return@composable
            ActiveCallScreen(
                session = current,
                elapsedSeconds = container.callSessionController.elapsedSeconds,
                onEnd = { container.callSessionController.end() },
                onOpenProfile = current.contactId?.let { id ->
                    { navController.navigate(Routes.contact(id)) { launchSingleTop = true } }
                },
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Call helpers — one place that turns UI intents into session commands.
// ---------------------------------------------------------------------------

private fun AppContainer.callContact(contact: Contact) {
    callSessionController.startOutgoing(
        contactId = contact.id,
        number = null,
        displayName = null,
    )
}

private fun AppContainer.callFrom(call: ResolvedCall) {
    callSessionController.startOutgoing(
        contactId = call.contactId,
        number = call.number,
        displayName = if (call.isUnknown) null else call.displayName,
    )
}

private fun AppContainer.callDialString(digits: String, match: Contact?) {
    callSessionController.startOutgoing(
        contactId = match?.id,
        number = digits,
        displayName = match?.name,
    )
}

// ---------------------------------------------------------------------------
// Navigation helpers
// ---------------------------------------------------------------------------

private fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) { launchSingleTop = true }
}

private fun NavHostController.gotoArea(area: NexusArea) {
    navigate(area.route) {
        popUpTo(Routes.Home) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavHostController.dismissIfTop(route: String) {
    val top = currentBackStackEntry?.destination?.route
    if (top == route) popBackStack()
}

private fun NavHostController.dismissCallScreens() {
    while (true) {
        val top = currentBackStackEntry?.destination?.route
        if (top == Routes.Incoming || top == Routes.Active) {
            if (!popBackStack()) return
        } else {
            return
        }
    }
}
