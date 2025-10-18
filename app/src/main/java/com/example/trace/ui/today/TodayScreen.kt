package com.example.trace.ui.today

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.graphicsLayer
import com.example.trace.ui.theme.TraceTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TodayScreen(
    onSave: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    val today = remember {
        val fmt = DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy")
        LocalDate.now().format(fmt)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = today,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 28.dp)
        )

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = {
                Text(
                    "What stayed with you today?",
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp)
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp)
                .focusRequester(focusRequester),
            singleLine = false,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                cursorColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        )

        Spacer(Modifier.height(20.dp))

        // Box used to align the Save button to the right
        Box(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    val trimmed = text.trim()
                    if (trimmed.isNotEmpty()) {
                        onSave(trimmed)
                        text = ""
                        focusManager.clearFocus()
                    }
                },
                enabled = text.isNotBlank(),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text("Save", style = MaterialTheme.typography.bodyLarge)
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Your thought is now part of your trace.",
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
            textAlign = TextAlign.Start,
            modifier = Modifier.graphicsLayer(alpha = 0f)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodayScreenPreview() {
    TraceTheme { TodayScreen(onSave = {}) }
}