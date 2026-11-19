package com.example.ui

import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.components.ColorPickerDialog
import com.example.ui.components.DrawingCanvasCard
import com.example.ui.components.ToolsDrawerOverlay
import com.example.ui.components.TopToolbar
import com.example.ui.theme.BgBlue
import com.example.ui.theme.BgMint
import com.example.ui.theme.BgPink
import com.example.ui.theme.BrandPurple
import com.example.ui.viewmodel.HandwritingViewModel
import com.example.util.BoardImageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HandwritingBoardScreen(
    viewModel: HandwritingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var toastMessage by remember { mutableStateOf<String?>(null) }

    val msgPictureAdded = stringResource(R.string.msg_picture_added)
    val msgPictureFailed = stringResource(R.string.msg_picture_failed)
    val msgSavedPng = stringResource(R.string.msg_saved_png)
    val msgSaveFailed = stringResource(R.string.msg_save_failed)
    val msgShareFailed = stringResource(R.string.msg_share_failed)
    val shareTitle = stringResource(R.string.share_chooser_title)

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2100)
            toastMessage = null
        }
    }

    // Zero-permission Android Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                BoardImageHelper.decodeBitmapFromUri(
                    context = context,
                    uri = uri,
                    maxWidth = viewModel.canvasWidthPx,
                    maxHeight = viewModel.canvasHeightPx
                )
            }
            if (bitmap != null) {
                viewModel.addImage(bitmap)
                viewModel.closeDrawer()
                toastMessage = msgPictureAdded
            } else {
                toastMessage = msgPictureFailed
            }
        }
    }

    // Fallback document saver for API < 29 (Android 7/8)
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/png")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val exportBitmap = BoardImageHelper.exportBoardToBitmap(
            committedBitmap = viewModel.getCommittedBitmap(),
            paperMode = uiState.paperMode,
            width = viewModel.canvasWidthPx,
            height = viewModel.canvasHeightPx
        )
        scope.launch {
            val ok = withContext(Dispatchers.IO) {
                BoardImageHelper.writePngToUri(context, exportBitmap, uri)
            }
            viewModel.closeDrawer()
            toastMessage = if (ok) msgSavedPng else msgSaveFailed
        }
    }

    fun handleSavePng() {
        val exportBitmap = BoardImageHelper.exportBoardToBitmap(
            committedBitmap = viewModel.getCommittedBitmap(),
            paperMode = uiState.paperMode,
            width = viewModel.canvasWidthPx,
            height = viewModel.canvasHeightPx
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            scope.launch {
                val ok = withContext(Dispatchers.IO) {
                    BoardImageHelper.savePngToMediaStore(context, exportBitmap)
                }
                viewModel.closeDrawer()
                toastMessage = if (ok) msgSavedPng else msgSaveFailed
            }
        } else {
            createDocumentLauncher.launch(BoardImageHelper.generateFileName())
        }
    }

    fun handleShare() {
        val exportBitmap = BoardImageHelper.exportBoardToBitmap(
            committedBitmap = viewModel.getCommittedBitmap(),
            paperMode = uiState.paperMode,
            width = viewModel.canvasWidthPx,
            height = viewModel.canvasHeightPx
        )
        scope.launch {
            val shareUri = withContext(Dispatchers.IO) {
                BoardImageHelper.createShareablePngUri(context, exportBitmap)
            }
            if (shareUri != null) {
                viewModel.closeDrawer()
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, shareUri)
                    putExtra(Intent.EXTRA_SUBJECT, shareTitle)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(sendIntent, shareTitle))
            } else {
                toastMessage = msgShareFailed
            }
        }
    }

    // Playful diagonal pastel background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(BgPink, BgBlue, BgMint),
                    start = Offset.Zero,
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            TopToolbar(
                inkColor = uiState.inkColor,
                isErasing = uiState.isErasing,
                canUndo = uiState.canUndo,
                onMenuClick = viewModel::openDrawer,
                onColorClick = viewModel::openColorPicker,
                onEraserClick = viewModel::toggleEraser,
                onUndoClick = viewModel::undo,
                onClearClick = viewModel::clear
            )

            DrawingCanvasCard(
                committedBitmap = viewModel.getCommittedBitmap(),
                canvasRevision = uiState.canvasRevision,
                paperMode = uiState.paperMode,
                inkColor = uiState.inkColor,
                pencilSizeDp = uiState.pencilSize,
                isErasing = uiState.isErasing,
                onCanvasSizeChanged = viewModel::onCanvasSizeChanged,
                onStrokeCommitted = viewModel::commitStroke,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }

        ToolsDrawerOverlay(
            isOpen = uiState.isDrawerOpen,
            pencilSize = uiState.pencilSize,
            inkColor = uiState.inkColor,
            isErasing = uiState.isErasing,
            paperMode = uiState.paperMode,
            onDismiss = viewModel::closeDrawer,
            onPencilSizeChange = viewModel::setPencilSize,
            onPaperModeChange = viewModel::setPaperMode,
            onAddPictureClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onSavePngClick = ::handleSavePng,
            onShareClick = ::handleShare
        )

        if (uiState.isColorPickerOpen) {
            ColorPickerDialog(
                initialColor = uiState.inkColor,
                onColorSelected = viewModel::setColor,
                onDismiss = viewModel::closeColorPicker
            )
        }

        // Floating playful pill toast
        AnimatedVisibility(
            visible = toastMessage != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 26.dp)
        ) {
            Box(
                modifier = Modifier
                    .shadow(
                        elevation = 10.dp,
                        shape = CircleShape,
                        ambientColor = BrandPurple.copy(alpha = 0.4f),
                        spotColor = BrandPurple.copy(alpha = 0.4f)
                    )
                    .clip(CircleShape)
                    .background(BrandPurple)
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = toastMessage.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
        }
    }
}
