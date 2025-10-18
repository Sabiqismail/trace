# Trace — Journal Beyond Noise (v0.0)

A minimalist Android app for one quiet thought a day. This is a personal MVP to “feel it in hand.”

---

## What’s in v0.0

- **Today**: one serene input for a single daily thought
- **Traces**: a calm, chronological list with an empty state
- **Gentle motion**: crossfade between screens
- **Tone**: on-brand microcopy and snackbar confirmations
- **Persistence**: offline, local via **DataStore + JSON** (no accounts, no network)
- **Icon**: minimal fading line

---

## Tech & Versions

- **Kotlin**: 2.0.21
- **AGP**: 8.7.2
- **Compose**: BOM `2024.09.00` (Material3, runtime, foundation, ui-text)
- **Data**: DataStore (Preferences) + Kotlinx Serialization (JSON)
- **Min SDK**: 24
- **JVM / Desugaring**: Java 17, `desugar_jdk_libs:2.1.2`

> Room was intentionally removed for v0.0 to avoid kapt/ksp toolchain friction. Reintroduce later if needed.

---

## Structure (packages)
com.example.trace ├─ data/ │  ├─ Entry.kt                  # model │  ├─ Serialization.kt          # DTO + Json instance │  ├─ EntryRepository.kt        # DataStore-backed repository │  └─ TraceStores.kt            # DataStore handle ├─ ui/ │  ├─ theme/                    # TraceTheme (colors/typography) │  ├─ today/ │  │  ├─ TodayScreen.kt │  │  └─ TodayViewModel.kt │  └─ traces/ │     ├─ TracesScreen.kt │     └─ TracesViewModel.kt └─ MainActivity.kt

---

## Build & Run

1. Open in Android Studio (Ladybug or newer), let Gradle sync.
2. Run on an emulator or device (API 24+).
3. You should see **Today** → type → **Save** (snackbar).
4. Tap **Traces** → your entry appears with a gentle crossfade.

---

## Brand Notes

- **Colors**: Sand `#F9F6F3`, Linen `#EFECE9`, Charcoal `#3D3C3A`, Copper `#CBA77C`
- **Typography**: DM Serif Display (titles), Inter (body)
- **Language**: Calm, human, reflective. No exclamation marks.
- **Motion**: Soft fades only. No bounce/pop.

---

## Troubleshooting

- **Compose “plugin required”**: ensure `org.jetbrains.kotlin.plugin.compose` is applied in `plugins` and pinned in `settings.gradle.kts`.
- **Keyboard imports missing**: include `implementation("androidx.compose.ui:ui-text")` and imports:
  `ImeAction`, `KeyboardOptions`, `KeyboardActions`.
- **Old Room errors**: delete any leftover `@Dao`, `@Database`, `Converters.kt`, or Room imports.

---

## v0.1 Ideas (still quiet)

- Autosave draft (DataStore key) while typing
- “Highlight” toggle in list (soft accent line)
- Dark theme
- Reminder (one local notification): “Time to leave today’s trace.”
- Simple export (TXT/JSON) to device storage

> Rule of growth: **presence over productivity**. Add slowly, keep silence intact.

---

## License

Personal project (MVP). Distribution not intended for this build.