package dev.whooslizi.screenposter.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    
    var expanded by remember { mutableStateOf(false) }

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
