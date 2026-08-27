package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ApplyDialog
import com.example.ui.components.FlagDialog
import com.example.ui.components.NotificationsDialog
import com.example.ui.components.bounceClickable
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MapExploreScreen
import com.example.ui.screens.PostGigScreen
import com.example.ui.screens.SavedGigsScreen
import com.example.ui.theme.CebuGigMapTheme
import com.example.viewmodel.AppNavDestination
import com.example.viewmodel.GigViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: GigViewModel = viewModel()
            val systemInDark = isSystemInDarkTheme()
            val userDarkModePref by viewModel.isDarkMode.collectAsState()
            val effectiveDarkMode = userDarkModePref ?: systemInDark

            CebuGigMapTheme(darkTheme = effectiveDarkMode) {
                CebuGigMapApp(
                    viewModel = viewModel,
                    isDarkTheme = effectiveDarkMode,
                    onToggleTheme = {
                        viewModel.toggleDarkMode(effectiveDarkMode)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CebuGigMapApp(
    viewModel: GigViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val currentDestination by viewModel.currentDestination.collectAsState()
    val allGigs by viewModel.allGigs.collectAsState()
    val filteredGigs by viewModel.filteredGigs.collectAsState()
    val flaggedGigs by viewModel.flaggedGigs.collectAsState()
    val savedOrAppliedGigs by viewModel.savedOrAppliedGigs.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedGig by viewModel.selectedGig.collectAsState()
    val isListView by viewModel.isMobileListView.collectAsState()
    val postFormState by viewModel.postFormState.collectAsState()

    val activeCount by viewModel.activeCount.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()
    val flaggedCount by viewModel.flaggedCount.collectAsState()

    val applyTarget by viewModel.applyGigTarget.collectAsState()
    val flagTarget by viewModel.flagGigTarget.collectAsState()
    var showNotificationsDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Audiotrack,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "CEBU GIG MAP",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = "BETA",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontSize = 9.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                actions = {
                    // Notifications Activity Icon
                    IconButton(
                        onClick = { showNotificationsDialog = true },
                        modifier = Modifier
                            .testTag("notifications_button")
                            .bounceClickable { showNotificationsDialog = true }
                    ) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Text("3")
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Dark / Light Mode Toggle Icon
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier
                            .testTag("theme_toggle_button")
                            .bounceClickable(onClick = onToggleTheme)
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkTheme) "Switch to Light Mode" else "Switch to Dark Mode",
                            tint = if (isDarkTheme) Color(0xFFFFB77F) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.border(0.dp, Color.Transparent)
            )
        },
        bottomBar = {
            // Iconic Navigation Bar (Navigation strictly using icons, no text labels)
            IconOnlyBottomNavigationBar(
                currentDestination = currentDestination,
                flaggedCount = flaggedCount,
                onNavigate = { dest ->
                    viewModel.navigateTo(dest)
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = {
                    if (targetState.ordinal > initialState.ordinal) {
                        (slideInHorizontally(animationSpec = tween(280)) { width -> width } + fadeIn(animationSpec = tween(280)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(280)) { width -> -width } + fadeOut(animationSpec = tween(280)))
                    } else {
                        (slideInHorizontally(animationSpec = tween(280)) { width -> -width } + fadeIn(animationSpec = tween(280)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(280)) { width -> width } + fadeOut(animationSpec = tween(280)))
                    }
                },
                label = "ScreenTransition",
                modifier = Modifier.fillMaxSize()
            ) { destination ->
                when (destination) {
                    AppNavDestination.HOME -> {
                        HomeScreen(
                            activeGigsCount = allGigs.size,
                            featuredGig = allGigs.firstOrNull { it.id == 3L || it.id == 1L } ?: allGigs.firstOrNull(),
                            onExploreClicked = { viewModel.navigateTo(AppNavDestination.EXPLORE_MAP) },
                            onPostGigClicked = { viewModel.navigateTo(AppNavDestination.POST_GIG) },
                            onFeaturedGigClicked = { gig ->
                                viewModel.selectGig(gig)
                                viewModel.navigateTo(AppNavDestination.EXPLORE_MAP)
                            }
                        )
                    }

                    AppNavDestination.EXPLORE_MAP -> {
                        MapExploreScreen(
                            gigs = filteredGigs,
                            selectedCategory = selectedCategory,
                            searchQuery = searchQuery,
                            selectedGig = selectedGig,
                            isListView = isListView,
                            onCategorySelected = { viewModel.selectCategory(it) },
                            onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                            onGigSelected = { viewModel.selectGig(it) },
                            onToggleViewMode = { viewModel.toggleMobileListView() },
                            onApplyClicked = { viewModel.openApplyDialog(it) },
                            onFlagClicked = { viewModel.openFlagDialog(it) },
                            onSaveToggle = { viewModel.toggleSaveGig(it) }
                        )
                    }

                    AppNavDestination.POST_GIG -> {
                        PostGigScreen(
                            formState = postFormState,
                            onFormUpdate = { title, category, dateText, locName, x, y, pay, contact, desc ->
                                viewModel.updatePostForm(
                                    title = title,
                                    category = category,
                                    dateText = dateText,
                                    locationName = locName,
                                    posX = x,
                                    posY = y,
                                    payText = pay,
                                    contactInfo = contact,
                                    description = desc
                                )
                            },
                            onSubmit = {
                                viewModel.submitNewGig {
                                    // navigated back to map in VM
                                }
                            }
                        )
                    }

                    AppNavDestination.ADMIN -> {
                        AdminDashboardScreen(
                            activeCount = activeCount,
                            pendingCount = pendingCount,
                            flaggedCount = flaggedCount,
                            flaggedGigs = flaggedGigs,
                            onApprove = { viewModel.approveGig(it) },
                            onHide = { viewModel.hideGig(it) },
                            onDelete = { viewModel.deleteGig(it) }
                        )
                    }

                    AppNavDestination.SAVED -> {
                        SavedGigsScreen(
                            gigs = savedOrAppliedGigs,
                            onGigSelected = {
                                viewModel.selectGig(it)
                                viewModel.navigateTo(AppNavDestination.EXPLORE_MAP)
                            },
                            onApplyClicked = { viewModel.openApplyDialog(it) },
                            onFlagClicked = { viewModel.openFlagDialog(it) },
                            onSaveToggle = { viewModel.toggleSaveGig(it) },
                            onExploreClicked = { viewModel.navigateTo(AppNavDestination.EXPLORE_MAP) }
                        )
                    }
                }
            }

            // Apply Dialog Modal
            applyTarget?.let { gig ->
                ApplyDialog(
                    gig = gig,
                    onDismiss = { viewModel.closeApplyDialog() },
                    onSubmit = { name, instrument, note ->
                        viewModel.submitApplication(gig, name, instrument, note)
                    }
                )
            }

            // Flag Dialog Modal
            flagTarget?.let { gig ->
                FlagDialog(
                    gig = gig,
                    onDismiss = { viewModel.closeFlagDialog() },
                    onSubmitFlag = { reason ->
                        viewModel.submitFlag(gig, reason)
                    }
                )
            }

            // Notifications Sheet Dialog
            if (showNotificationsDialog) {
                NotificationsDialog(
                    onDismiss = { showNotificationsDialog = false }
                )
            }
        }
    }
}

/**
 * Bottom Navigation Bar designed strictly with icons (no text labels)
 * with animated active indicator pill and tactile bounce haptic response.
 */
@Composable
fun IconOnlyBottomNavigationBar(
    currentDestination: AppNavDestination,
    flaggedCount: Int,
    onNavigate: (AppNavDestination) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Home / Hero
            NavIconItem(
                icon = Icons.Default.Explore,
                contentDescription = "Home",
                isSelected = currentDestination == AppNavDestination.HOME,
                onClick = { onNavigate(AppNavDestination.HOME) }
            )

            // Tab 2: Map & Explore Feed
            NavIconItem(
                icon = Icons.Default.Map,
                contentDescription = "Gig Map",
                isSelected = currentDestination == AppNavDestination.EXPLORE_MAP,
                onClick = { onNavigate(AppNavDestination.EXPLORE_MAP) }
            )

            // Tab 3: Post a Gig
            NavIconItem(
                icon = Icons.Default.AddLocationAlt,
                contentDescription = "Post a Gig",
                isSelected = currentDestination == AppNavDestination.POST_GIG,
                isHighlight = true,
                onClick = { onNavigate(AppNavDestination.POST_GIG) }
            )

            // Tab 4: Moderation Dashboard (with badge if flags exist)
            NavIconItem(
                icon = Icons.Default.AdminPanelSettings,
                contentDescription = "Moderation Dashboard",
                isSelected = currentDestination == AppNavDestination.ADMIN,
                badgeCount = if (flaggedCount > 0) flaggedCount else null,
                onClick = { onNavigate(AppNavDestination.ADMIN) }
            )

            // Tab 5: Saved / Applied Gigs
            NavIconItem(
                icon = Icons.Default.Bookmark,
                contentDescription = "Saved Gigs",
                isSelected = currentDestination == AppNavDestination.SAVED,
                onClick = { onNavigate(AppNavDestination.SAVED) }
            )
        }
    }
}

@Composable
fun NavIconItem(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    isHighlight: Boolean = false,
    badgeCount: Int? = null,
    onClick: () -> Unit
) {
    val activeBg = if (isHighlight) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }

    val activeTint = if (isHighlight) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    val inactiveTint = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(54.dp, 44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) activeBg else Color.Transparent)
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) (if (isHighlight) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .bounceClickable(onClick = onClick)
    ) {
        if (badgeCount != null && badgeCount > 0) {
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ) {
                        Text(badgeCount.toString())
                    }
                }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = if (isSelected) activeTint else inactiveTint,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (isSelected) activeTint else inactiveTint,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
