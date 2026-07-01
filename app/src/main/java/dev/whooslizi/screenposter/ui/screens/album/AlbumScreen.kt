package dev.whooslizi.screenposter.ui.screens.album

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import dev.whooslizi.screenposter.data.local.entity.WallpaperEntity
import dev.whooslizi.screenposter.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AlbumScreen(
    navController: NavController,
    albumId: Long,
    viewModel: AlbumViewModel = hiltViewModel()
) {
    val album by viewModel.album.collectAsState()
    val wallpapers by viewModel.wallpapers.collectAsState()
    val context = LocalContext.current
    
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    val inSelectionMode = selectedIds.isNotEmpty()
    var showSetWallpaperDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            val stringUris = uris.map { it.toString() }
            viewModel.addImages(stringUris)
        }
    }

    Scaffold(
        topBar = {
            if (inSelectionMode) {
                TopAppBar(
                    title = { Text("${selectedIds.size} selected") },
                    navigationIcon = {
                        IconButton(onClick = { selectedIds = emptySet() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel selection")
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text(album?.name ?: "Album") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Images")
                        }
                    }
                )
            }
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Set Wallpaper
                    IconButton(
                        onClick = {
                            if (inSelectionMode) {
                                showSetWallpaperDialog = true
                            } else {
                                Toast.makeText(context, "Long-press to select wallpapers first", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Wallpaper,
                            contentDescription = "Set Wallpaper",
                            tint = if (inSelectionMode) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Edit
                    IconButton(
                        onClick = {
                            if (inSelectionMode && selectedIds.size == 1) {
                                val wallpaperId = selectedIds.first()
                                selectedIds = emptySet()
                                navController.navigate(Screen.Editor.createRoute(wallpaperId))
                            } else if (inSelectionMode) {
                                Toast.makeText(context, "Select only one wallpaper to edit", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Long-press to select a wallpaper first", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = if (inSelectionMode && selectedIds.size == 1) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Share
                    IconButton(
                        onClick = {
                            if (inSelectionMode) {
                                val shareIntent = viewModel.shareWallpapers(selectedIds)
                                if (shareIntent != null) {
                                    context.startActivity(Intent.createChooser(shareIntent, "Share wallpaper(s)"))
                                    selectedIds = emptySet()
                                }
                            } else {
                                Toast.makeText(context, "Long-press to select wallpapers first", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share",
                            tint = if (inSelectionMode) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Favorite
                    IconButton(
                        onClick = {
                            if (inSelectionMode) {
                                viewModel.toggleFavorite(selectedIds)
                                selectedIds = emptySet()
                                Toast.makeText(context, "Favorites updated", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Long-press to select wallpapers first", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        val anySelected = inSelectionMode && wallpapers.any { it.id in selectedIds && it.favorite }
                        Icon(
                            if (anySelected) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (inSelectionMode) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Delete
                    IconButton(
                        onClick = {
                            if (inSelectionMode) {
                                showDeleteDialog = true
                            } else {
                                Toast.makeText(context, "Long-press to select wallpapers first", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = if (inSelectionMode) MaterialTheme.colorScheme.error
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(wallpapers) { wallpaper ->
                WallpaperItem(
                    wallpaper = wallpaper,
                    isSelected = wallpaper.id in selectedIds,
                    inSelectionMode = inSelectionMode,
                    onClick = {
                        if (inSelectionMode) {
                            selectedIds = if (wallpaper.id in selectedIds) {
                                selectedIds - wallpaper.id
                            } else {
                                selectedIds + wallpaper.id
                            }
                        } else {
                            navController.navigate(Screen.Preview.createRoute(wallpaper.id))
                        }
                    },
                    onLongClick = {
                        selectedIds = if (wallpaper.id in selectedIds) {
                            selectedIds - wallpaper.id
                        } else {
                            selectedIds + wallpaper.id
                        }
                    }
                )
            }
        }
    }

    // Set Wallpaper Dialog
    if (showSetWallpaperDialog) {
        val targetWallpaperId = selectedIds.firstOrNull()
        AlertDialog(
            onDismissRequest = { showSetWallpaperDialog = false },
            title = { Text("Set Wallpaper") },
            text = { Text("Where would you like to set this wallpaper?") },
            confirmButton = {
                TextButton(onClick = {
                    if (targetWallpaperId != null) {
                        viewModel.setWallpaper(targetWallpaperId, 3)
                        Toast.makeText(context, "Wallpaper set (Home + Lock)", Toast.LENGTH_SHORT).show()
                    }
                    selectedIds = emptySet()
                    showSetWallpaperDialog = false
                }) {
                    Text("Both")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        if (targetWallpaperId != null) {
                            viewModel.setWallpaper(targetWallpaperId, 1)
                            Toast.makeText(context, "Wallpaper set (Home Screen)", Toast.LENGTH_SHORT).show()
                        }
                        selectedIds = emptySet()
                        showSetWallpaperDialog = false
                    }) {
                        Text("Home Screen")
                    }
                    TextButton(onClick = {
                        if (targetWallpaperId != null) {
                            viewModel.setWallpaper(targetWallpaperId, 2)
                            Toast.makeText(context, "Wallpaper set (Lock Screen)", Toast.LENGTH_SHORT).show()
                        }
                        selectedIds = emptySet()
                        showSetWallpaperDialog = false
                    }) {
                        Text("Lock Screen")
                    }
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Wallpapers") },
            text = { Text("Delete ${selectedIds.size} selected wallpaper(s)? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteWallpapers(selectedIds)
                        Toast.makeText(context, "${selectedIds.size} wallpaper(s) deleted", Toast.LENGTH_SHORT).show()
                        selectedIds = emptySet()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WallpaperItem(
    wallpaper: WallpaperEntity,
    isSelected: Boolean,
    inSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(9f / 16f)
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (isSelected) {
                    Modifier.border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                } else {
                    Modifier
                }
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .background(Color.DarkGray)
    ) {
        AsyncImage(
            model = wallpaper.editedUri ?: wallpaper.uri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Selection indicator
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            )
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(24.dp)
                    .background(Color.White, CircleShape)
            )
        }

        // Favorite indicator (always visible when favorited)
        if (wallpaper.favorite && !isSelected) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = "Favorited",
                tint = Color.Red,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(20.dp)
            )
        }
    }
}
