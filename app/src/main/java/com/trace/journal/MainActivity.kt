package com.trace.journal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.trace.journal.data.entryRepository
import com.trace.journal.ui.theme.TraceTheme
import com.trace.journal.ui.today.TodayScreen
import com.trace.journal.ui.today.TodayViewModel
import com.trace.journal.ui.traces.TracesScreen
import com.trace.journal.ui.traces.TracesViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
class MainActivity : ComponentActivity() {

    private val repo by lazy { entryRepository(this) }
    private val todayVm by viewModels<TodayViewModel> { TodayViewModel.factory(repo) }
    private val tracesVm by viewModels<TracesViewModel> { TracesViewModel.factory(repo) }

    enum class Tab { Today, Traces }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TraceTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                // Refresh "today" when app returns to foreground (e.g., after midnight)
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                    todayVm.refreshToday()
                }

                // Pager: 0 = Today, 1 = Traces
                val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
                var currentTab by remember { mutableStateOf(Tab.Today) }

                // Settings sheet state
                var showSettings by remember { mutableStateOf(false) }
                var reminderEnabled by remember { mutableStateOf(false) } // MVP flag (non-persistent)

                // Keep tab in sync with pager swipes
                LaunchedEffect(pagerState.currentPage) {
                    currentTab = if (pagerState.currentPage == 0) Tab.Today else Tab.Traces
                }

                // Runtime permission launcher for Android 13+ notifications
                val notifPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { /* no-op */ }
                )

                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    if (currentTab == Tab.Traces) "Your Traces" else "Today",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            },
                            actions = {
                                TextButton(onClick = { showSettings = true }) {
                                    Text("Settings", style = MaterialTheme.typography.bodyLarge)
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                scrolledContainerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        BottomAppBar(containerColor = MaterialTheme.colorScheme.background) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                BottomItem(
                                    label = "Today",
                                    selected = currentTab == Tab.Today,
                                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                                    icon = { DotIcon(selected = currentTab == Tab.Today) }
                                )
                                Spacer(Modifier.width(72.dp))
                                BottomItem(
                                    label = "Traces",
                                    selected = currentTab == Tab.Traces,
                                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                                    icon = { TracesIcon(selected = currentTab == Tab.Traces) }
                                )
                            }
                        }
                    }
                ) { innerPadding ->

                    // subtle hairline under the app bar
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
                    )

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.padding(innerPadding)
                    ) { page ->
                        when (page) {
                            0 -> {
                                val initialText by todayVm.todayText.collectAsState()
                                TodayScreen(
                                    initialText = initialText,
                                    onSave = { raw ->
                                        val t = raw.trim()
                                        if (t.isEmpty()) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Take a breath. Try again.")
                                            }
                                        } else {
                                            scope.launch {
                                                todayVm.saveForToday(t)
                                                val streak = repo.consecutiveDaysUpTo(java.time.LocalDate.now())
                                                if (streak == 7) {
                                                    snackbarHostState.showSnackbar("Seven quiet days.")
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                            1 -> {
                                val list by tracesVm.entries.collectAsState()
                                TracesScreen(
                                    entries = list,
                                    onToggleHighlight = { date, flag -> tracesVm.toggleHighlight(date, flag) },
                                    onDelete = { date ->
                                        tracesVm.delete(date)
                                        scope.launch { snackbarHostState.showSnackbar("Deleted from your trace.") }
                                    }
                                )
                            }
                        }
                    }

                    // Settings bottom sheet
                    if (showSettings) {
                        SettingsSheet(
                            enabled = reminderEnabled,
                            onEnable = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val granted = ContextCompat.checkSelfPermission(
                                        this@MainActivity,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (!granted) {
                                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
                                com.trace.journal.notifications.ReminderScheduler.scheduleDaily(
                                    this@MainActivity, 21, 0
                                )
                                reminderEnabled = true
                                scope.launch { snackbarHostState.showSnackbar("Daily reminder set for 9:00 PM.") }
                            },
                            onDisable = {
                                com.trace.journal.notifications.ReminderScheduler.cancel(this@MainActivity)
                                reminderEnabled = false
                                scope.launch { snackbarHostState.showSnackbar("Daily reminder turned off.") }
                            },
                            onDismiss = { showSettings = false }
                        )
                    }
                }
            }
        }
    }
}

/* ----------------------- Settings Sheet ----------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSheet(
    enabled: Boolean,
    onEnable: () -> Unit,
    onDisable: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Settings", style = MaterialTheme.typography.titleLarge)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Daily reminder", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Time to leave today’s trace.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = { isOn -> if (isOn) onEnable() else onDisable() }
                )
            }

            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
            )

            Text(
                text = "All thoughts are stored locally. Trace never uploads or shares your data.",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

/* ----------------------- Bottom bar pieces ----------------------- */

@Composable
private fun BottomItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    val copper = Color(0xFFCBA77C)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .widthIn(min = 72.dp)
            .clickable(onClick = onClick)
    ) {
        icon()
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (selected) copper else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(tween(60)),
            exit = fadeOut(tween(120))
        ) {
            Spacer(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .width(28.dp)
                    .height(2.dp)
                    .background(copper)
            )
        }
    }
}

@Composable
private fun DotIcon(selected: Boolean) {
    val copper = Color(0xFFCBA77C)
    val color = if (selected) copper else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
    Canvas(modifier = Modifier.size(16.dp)) {
        drawCircle(color = color, radius = size.minDimension / 2f)
    }
}

@Composable
private fun TracesIcon(selected: Boolean) {
    val copper = Color(0xFFCBA77C)
    val stroke = MaterialTheme.colorScheme.onBackground.copy(alpha = if (selected) 0.9f else 0.6f)
    val dot = if (selected) copper else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)

    Canvas(modifier = Modifier.size(24.dp)) {
        val y = size.height / 2f
        val left = size.width * 0.2f
        val mid = size.width * 0.5f
        val right = size.width * 0.8f
        drawLine(color = stroke, start = Offset(left, y), end = Offset(right, y), strokeWidth = 2f)
        drawCircle(color = dot, radius = 3.5f, center = Offset(left, y))
        drawCircle(color = dot, radius = 3.5f, center = Offset(mid, y))
        drawCircle(color = dot, radius = 3.5f, center = Offset(right, y))
    }
}