package com.example.trace.ui.traces

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.trace.data.Entry
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import androidx.compose.ui.tooling.preview.Preview
import com.example.trace.ui.theme.TraceTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TracesScreen(
    entries: List<Entry>,
    onToggleHighlight: (LocalDate, Boolean) -> Unit = { _, _ -> },
    onDelete: (LocalDate) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val monthFmt = DateTimeFormatter.ofPattern("MMMM yyyy")
    val dayFmt = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")
    val grouped = entries.sortedByDescending { it.date }.groupBy { YearMonth.from(it.date) }

    var selected by remember { mutableStateOf<Entry?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) {
        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text("Every day leaves a trace.", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("Yours can start today.", style = MaterialTheme.typography.bodyLarge)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                grouped.forEach { (ym, list) ->
                    item("header-$ym") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize(animationSpec = tween(220))
                        ) {
                            Text(
                                text = ym.format(monthFmt),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, bottom = 2.dp)
                            )
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                            )
                            Spacer(Modifier.height(4.dp))
                        }
                    }

                    items(list, key = { it.date }) { entry ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize(animationSpec = tween(220))
                                .pointerInput(entry) {
                                    detectTapGestures(
                                        onLongPress = {
                                            selected = entry
                                            scope.launch { sheetState.show() }
                                        }
                                    )
                                }
                        ) {
                            Text(
                                text = entry.date.format(dayFmt),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = entry.text.trim(),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = if (entry.isHighlighted)
                                        Color(0xFFA48358) // soft gold
                                    else MaterialTheme.colorScheme.onBackground
                                ),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        }
    }

    if (selected != null) {
        ModalBottomSheet(
            onDismissRequest = { selected = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background,
            dragHandle = null
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val e = selected!!
                TextButton(
                    onClick = {
                        onToggleHighlight(e.date, !e.isHighlighted)
                        scope.launch { sheetState.hide() }
                        selected = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (e.isHighlighted) "Remove highlight" else "Highlight this trace",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                TextButton(
                    onClick = {
                        onDelete(e.date)
                        scope.launch { sheetState.hide() }
                        selected = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete this trace", style = MaterialTheme.typography.bodyLarge)
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TracesScreenPreview() {
    val sample = listOf(
        Entry(LocalDate.now(), "Sat with my coffee. The light felt soft."),
        Entry(LocalDate.now().minusDays(1), "A sentence from a book stayed with me.", isHighlighted = true),
        Entry(LocalDate.now().minusDays(35), "Silence in the elevator. A kind of comfort.")
    )
    TraceTheme { TracesScreen(entries = sample) }
}