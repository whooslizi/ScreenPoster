package dev.whooslizi.screenposter.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val workInfos by viewModel.workInfos.collectAsState(initial = emptyList())
    val isBatteryOptimized by viewModel.isBatteryOptimized.collectAsState()
    val hasExactAlarmPermission by viewModel.hasExactAlarmPermission.collectAsState()
    val context = LocalContext.current
    
    var expanded by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    
    // Refresh permissions/battery optimization status when returning from system settings
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissionsStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(60000L)
            currentTime = System.currentTimeMillis()
        }
    }

    val intervalOptions = listOf(
        "15 minutes" to 15,
        "30 minutes" to 30,
        "1 hour" to 60,
        "6 hours" to 360,
        "12 hours" to 720,
        "24 hours" to 1440,
        "Every Unlock" to -1,
        "On Device Boot" to -2
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                // ── Exact Alarm Permission Warning ──────────────
                if (!hasExactAlarmPermission) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Missing Alarm Permission",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "To change wallpapers automatically on a schedule, you must allow 'Alarms & Reminders' permission.",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { 
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                            data = Uri.parse("package:${context.packageName}")
                                        }
                                        context.startActivity(intent)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Grant Permission")
                            }
                        }
                    }
                }

                // ── Battery Optimization Warning ────────────────
                // Show prominently if battery optimization is ON (app will be killed)
                if (isBatteryOptimized) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = if (hasExactAlarmPermission) 16.dp else 0.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Background restrictions detected",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Your device may prevent wallpaper changes in the background. Tap below to fix this.",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.requestBatteryOptimizationExemption() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Disable battery optimization")
                            }
                            if (viewModel.isChineseOem) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = {
                                        val opened = viewModel.openAutoStartSettings()
                                        if (!opened) {
                                            Toast.makeText(
                                                context,
                                                "Could not find auto-start settings. Please enable it manually in your device settings.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                ) {
                                    Text("Enable auto-start (required for Vivo/Xiaomi/OPPO)")
                                }
                            }
                        }
                    }
                } else if (hasExactAlarmPermission) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Permissions granted & Battery optimization disabled — background changes will work",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    // Still show auto-start button for Chinese OEMs even if battery opt is off
                    if (viewModel.isChineseOem) {
                        ListItem(
                            modifier = Modifier.clickable {
                                val opened = viewModel.openAutoStartSettings()
                                if (!opened) {
                                    Toast.makeText(
                                        context,
                                        "Could not find auto-start settings. Please enable it manually.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            headlineContent = { Text("Auto-start settings") },
                            supportingContent = { Text("Make sure auto-start is enabled for this app") },
                            leadingContent = {
                                Icon(Icons.Default.BatteryAlert, contentDescription = null)
                            }
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // ── Automatic Wallpaper Change ──────────────────
                Text(
                    text = "Automatic Wallpaper Change",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp)
                )
                
                val currentLabel = intervalOptions.find { it.second == settings?.intervalMinutes }?.first ?: "Unknown"

                ListItem(
                    modifier = Modifier.clickable { expanded = true },
                    headlineContent = { Text("Changing interval") },
                    supportingContent = { Text(currentLabel) }
                )
                
                val workInfo = workInfos.firstOrNull()
                if (workInfo != null && workInfo.state == androidx.work.WorkInfo.State.ENQUEUED) {
                    val nextRun = workInfo.nextScheduleTimeMillis
                    val diff = nextRun - currentTime
                    if (diff > 0) {
                        val minutes = diff / 60000L
                        Text(
                            text = "Next wallpaper change in ~$minutes minute(s)",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }

                if (expanded) {
                    AlertDialog(
                        onDismissRequest = { expanded = false },
                        title = { Text("Select Interval") },
                        text = {
                            Column {
                                intervalOptions.forEach { (label, value) ->
                                    ListItem(
                                        modifier = Modifier.clickable {
                                            viewModel.updateInterval(value)
                                            expanded = false
                                        },
                                        headlineContent = { Text(label) },
                                        trailingContent = {
                                            if (value == settings?.intervalMinutes) {
                                                Icon(Icons.Default.Check, contentDescription = "Selected")
                                            }
                                        }
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { expanded = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // ── Home Screen Blur ────────────────────────────
                Text(
                    text = "Home Screen Blur",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp)
                )

                val blurPercent = settings?.homeScreenBlurPercent ?: 0
                var sliderValue by remember(blurPercent) { mutableStateOf(blurPercent.toFloat()) }

                ListItem(
                    headlineContent = { Text("Blur intensity") },
                    supportingContent = {
                        Text(
                            if (sliderValue.roundToInt() == 0) "Off"
                            else "${sliderValue.roundToInt()}%"
                        )
                    }
                )
                
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = {
                        viewModel.setHomeScreenBlurPercent(sliderValue.roundToInt())
                    },
                    valueRange = 0f..100f,
                    steps = 19, // 5% increments
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Text(
                    text = "Blurs the wallpaper on the home screen while keeping the lock screen wallpaper sharp",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // ── Playback Mode ───────────────────────────────
                Text(
                    text = "Playback Mode",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp)
                )

                // Random / Sequential
                ListItem(
                    modifier = Modifier.clickable { viewModel.setRandomMode(false) },
                    headlineContent = { Text("Sequential Mode") },
                    supportingContent = { Text("Wallpapers play in order") },
                    trailingContent = {
                        RadioButton(
                            selected = settings?.sequentialMode == true,
                            onClick = { viewModel.setRandomMode(false) }
                        )
                    }
                )

                ListItem(
                    modifier = Modifier.clickable { viewModel.setRandomMode(true) },
                    headlineContent = { Text("Random / Shuffle Mode") },
                    supportingContent = { Text("Wallpapers play randomly") },
                    trailingContent = {
                        RadioButton(
                            selected = settings?.randomMode == true,
                            onClick = { viewModel.setRandomMode(true) }
                        )
                    }
                )

                if (settings?.randomMode == true) {
                    ListItem(
                        modifier = Modifier.clickable { 
                            val current = settings?.noRepeatShuffle == true
                            viewModel.setNoRepeatShuffle(!current) 
                        },
                        headlineContent = { Text("No-repeat shuffle") },
                        supportingContent = { Text("Never repeat until every wallpaper has been used") },
                        trailingContent = {
                            Switch(
                                checked = settings?.noRepeatShuffle == true,
                                onCheckedChange = { viewModel.setNoRepeatShuffle(it) }
                            )
                        }
                    )
                }
            }
        }
    }
}
