package com.example.trace.ui.traces

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.trace.data.Entry
import java.time.format.DateTimeFormatter
import androidx.compose.ui.tooling.preview.Preview
import com.example.trace.ui.theme.TraceTheme
import java.time.LocalDate

@Composable
fun TracesScreen(
    entries: List<Entry>,
    modifier: Modifier = Modifier
) {
    val dateFmt = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) {
        if (entries.isEmpty()) {
            // Calm empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "Every day leaves a trace.",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Yours can start today.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(entries, key = { it.date }) { entry ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = entry.date.format(dateFmt),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = entry.text.trim(),
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Divider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TracesScreenEmptyPreview() {
    TraceTheme { TracesScreen(entries = emptyList()) }
}

@Preview(showBackground = true)
@Composable
private fun TracesScreenListPreview() {
    val sample = listOf(
        Entry(LocalDate.now(), "Sat with my coffee. The light felt soft."),
        Entry(LocalDate.now().minusDays(1), "A sentence from a book stayed with me.", isHighlighted = true),
        Entry(LocalDate.now().minusDays(2), "Silence in the elevator. A kind of comfort.")
    )
    TraceTheme { TracesScreen(entries = sample) }
}