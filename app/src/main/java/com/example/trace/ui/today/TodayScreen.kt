package com.example.trace.ui.today

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trace.ui.theme.TraceTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

@Composable
fun TodayScreen(
    initialText: String = "",
    onSave: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Seed state from initialText, but react if real data arrives later.
    var hadExistingToday by rememberSaveable { mutableStateOf(initialText.isNotBlank()) }
    var text by rememberSaveable { mutableStateOf(if (initialText.isNotBlank()) initialText else "") }
    var isEditing by rememberSaveable { mutableStateOf(initialText.isBlank()) }

    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val haptics = LocalHapticFeedback.current

    // Handle flow that may emit "" first then a real value later
    LaunchedEffect(initialText) {
        val exists = initialText.isNotBlank()
        when {
            exists && isEditing && text.isBlank() -> {
                // First real load; user hasn’t typed — go to VIEW
                hadExistingToday = true
                isEditing = false
                text = initialText
            }
            exists && !isEditing -> {
                // Already in VIEW — refresh visible text if it changed
                hadExistingToday = true
                text = initialText
            }
            !exists -> {
                // No saved entry; default to EDIT if not already
                hadExistingToday = false
                if (!isEditing) isEditing = true
                if (text.isBlank()) text = ""
            }
        }
    }

    // Dark-aware copper + selection colors
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.2f
    val copper = if (isDark) MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
    else MaterialTheme.colorScheme.primary
    val selectionColors = TextSelectionColors(
        handleColor = copper,
        backgroundColor = copper.copy(alpha = if (isDark) 0.25f else 0.20f)
    )

    // Post-save animations (short + gentle)
    var showTraceLine by remember { mutableStateOf(false) }
    LaunchedEffect(showTraceLine) {
        if (showTraceLine) {
            delay(850)
            showTraceLine = false
        }
    }
    var showSavedMessage by remember { mutableStateOf(false) }
    LaunchedEffect(showSavedMessage) {
        if (showSavedMessage) {
            delay(900)
            showSavedMessage = false
        }
    }

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

        if (!isEditing && hadExistingToday) {
            // ---- VIEW MODE (entry already saved for today) ----
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        isEditing = true
                        if (text.isBlank()) text = initialText
                    }
                    .padding(4.dp)
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(copper, RoundedCornerShape(1.dp))
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Tap to edit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                )
            }
        } else {
            // ---- EDIT MODE ----
            CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = {
                        Text(
                            Prompts.forDate(),
                            color = MaterialTheme.colorScheme.onBackground.copy(
                                alpha = if (isDark) 0.55f else 0.65f
                            ),
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
                        focusedBorderColor = copper,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(
                            alpha = if (isDark) 0.18f else 0.15f
                        ),
                        cursorColor = copper
                    )
                )
            }

            Spacer(Modifier.height(20.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                val label = if (hadExistingToday) "Update" else "Save"
                Button(
                    onClick = {
                        val trimmed = text.trim()
                        if (trimmed.isNotEmpty()) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            showTraceLine = true
                            showSavedMessage = true
                            onSave(trimmed)
                            focusManager.clearFocus()
                            hadExistingToday = true
                            isEditing = false       // return to VIEW after save/update
                        }
                    },
                    enabled = text.isNotBlank(),
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Text(label, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        // Space before confirmation visuals
        Spacer(Modifier.height(24.dp))

        // Centered, edge→center fading "trace" line using outline + copper peak
        AnimatedVisibility(
            visible = showTraceLine,
            enter = fadeIn(tween(280)),
            exit = fadeOut(tween(520))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(110.dp)
                        .height(2.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0f),
                                    copper,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0f)
                                )
                            ),
                            shape = RoundedCornerShape(1.dp)
                        )
                )
            }
        }

        // Shorter confirmation message
        AnimatedVisibility(
            visible = showSavedMessage,
            enter = fadeIn(tween(220)),
            exit = fadeOut(tween(520))
        ) {
            Text(
                text = "Saved to your trace.",
                color = copper,
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Reserved copy (kept invisible for layout stability)
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
private fun TodayScreenPreviewSaved() {
    TraceTheme {
        TodayScreen(
            initialText = "Sat with my coffee. The light felt soft.",
            onSave = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodayScreenPreviewNew() {
    TraceTheme {
        TodayScreen(
            initialText = "",
            onSave = {}
        )
    }
}