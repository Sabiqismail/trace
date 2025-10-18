package com.example.trace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.trace.data.entryRepository
import com.example.trace.ui.theme.TraceTheme
import com.example.trace.ui.today.TodayScreen
import com.example.trace.ui.today.TodayViewModel
import com.example.trace.ui.traces.TracesScreen
import com.example.trace.ui.traces.TracesViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repo = entryRepository(this)

        val todayVm by viewModels<TodayViewModel> { TodayViewModel.factory(repo) }
        val tracesVm by viewModels<TracesViewModel> { TracesViewModel.factory(repo) }

        setContent {
            TraceTheme {
                var showTraces by remember { mutableStateOf(false) }

                // Snackbar host + coroutine scope
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                Surface(color = MaterialTheme.colorScheme.background) {
                    Scaffold(
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(
                                        if (showTraces) "Your Traces" else "Today",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                },
                                actions = {
                                    TextButton(onClick = { showTraces = !showTraces }) {
                                        Text(
                                            if (showTraces) "Today" else "Traces",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.background,
                                    scrolledContainerColor = MaterialTheme.colorScheme.background
                                )
                            )
                        },
                        snackbarHost = {
                            SnackbarHost(snackbarHostState) { data ->
                                Snackbar(
                                    snackbarData = data,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    ) { innerPadding ->
                        Divider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                        )

                        Crossfade(targetState = showTraces, label = "TraceScreens") { show ->
                            if (show) {
                                val list by tracesVm.entries.collectAsState()
                                TracesScreen(
                                    entries = list,
                                    modifier = Modifier.padding(innerPadding)
                                )
                            } else {
                                TodayScreen(
                                    onSave = { raw ->
                                        val t = raw.trim()
                                        if (t.isEmpty()) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Take a breath. Try again.")
                                            }
                                        } else {
                                            todayVm.saveForToday(t)
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Your thought is now part of your trace.")
                                            }
                                        }
                                    },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}