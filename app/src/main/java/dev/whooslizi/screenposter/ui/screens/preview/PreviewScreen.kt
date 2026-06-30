package dev.whooslizi.screenposter.ui.screens.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    navController: NavController,
    wallpaperId: Long,
    viewModel: PreviewViewModel = hiltViewModel()
) {
    val wallpaper by viewModel.wallpaper.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0 = Lock, 1 = Home
    var showSetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preview") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(16.dp)
            ) {
                TabRow(selectedTabIndex = selectedTab, containerColor = Color.Transparent) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Lock Screen", color = Color.White) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Home Screen", color = Color.White) }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showSetDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Set Wallpaper", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (wallpaper != null) {
                AsyncImage(
                    model = wallpaper!!.editedUri ?: wallpaper!!.uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Overlays simulating UI elements
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (selectedTab == 0) {
                    // Lock Screen Overlay (Clock)
                    Text(
                        text = "10:00",
                        color = Color.White,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Light,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 48.dp)
                    )
                } else {
                    // Home Screen Overlay (Icons placeholder)
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(5) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.5f))
                            )
                        }
                    }
                }
            }
        }

        if (showSetDialog) {
            AlertDialog(
                onDismissRequest = { showSetDialog = false },
                title = { Text("Set Wallpaper") },
                text = { Text("Where would you like to set this wallpaper?") },
                confirmButton = {
                    TextButton(onClick = { 
                        viewModel.setWallpaper(3)
                        showSetDialog = false 
                    }) {
                        Text("Both")
                    }
                },
                dismissButton = {
                    Row {
                        TextButton(onClick = {
                            viewModel.setWallpaper(1)
                            showSetDialog = false
                        }) {
                            Text("Home Screen")
                        }
                        TextButton(onClick = {
                            viewModel.setWallpaper(2)
                            showSetDialog = false
                        }) {
                            Text("Lock Screen")
                        }
                    }
                }
            )
        }
    }
}
