package com.example.mangashelf.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mangashelf.ui.components.AnimatedActionButtons
import com.example.mangashelf.ui.components.CategoryChip
import com.example.mangashelf.ui.components.ErrorView
import com.example.mangashelf.ui.components.LoadingSpinner
import com.example.mangashelf.ui.components.MangaCoverImage
import com.example.mangashelf.ui.components.StatsSection
import com.example.mangashelf.ui.uistates.MangaDetailUiState
import com.example.mangashelf.ui.viewmodel.MangaDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaDetailScreen(
    mangaId: String,
    onNavigateBack: () -> Unit,
    viewModel: MangaDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isImageExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(mangaId) {
        viewModel.loadManga(mangaId)
    }

    Scaffold{ paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is MangaDetailUiState.Loading -> LoadingSpinner()
                is MangaDetailUiState.Error -> ErrorView(
                    message = state.message,
                    onRetry = { viewModel.retry() }
                )

                is MangaDetailUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Hero section with image
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        ) {
                            // Expandable image
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { isImageExpanded = !isImageExpanded }
                            ) {
                                MangaCoverImage(
                                    imageUrl = state.manga.image,
                                    modifier = Modifier.fillMaxSize()
                                )
                                // Gradient overlay
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                                                )
                                            )
                                        )
                                )
                            }

                            // Title and basic info overlay
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = state.manga.title,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                CategoryChip(category = state.manga.category)
                            }
                        }

                        // Stats cards
                        StatsSection(manga = state.manga)

                        // Action buttons with animation
                        AnimatedActionButtons(
                            manga = state.manga,
                            onToggleRead = { viewModel.toggleRead() },
                            onToggleFavorite = { viewModel.toggleFavorite() }
                        )
                    }
                }
            }
        }

        // Expanded image dialog
        if (isImageExpanded) {
            Dialog(
                onDismissRequest = { isImageExpanded = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.9f))
                        .clickable { isImageExpanded = false }
                ) {
                    (uiState as? MangaDetailUiState.Success)?.manga?.let { manga ->
                        MangaCoverImage(
                            imageUrl = manga.image,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(2f / 3f)
                                .align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}
