package com.example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import com.example.model.PaperMode
import com.example.ui.theme.PaperDark
import com.example.ui.theme.PaperLight
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

object BoardImageHelper {

    fun generateFileName(): String {
        val stamp = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US).format(Date())
        return "handwriting-$stamp.png"
    }

    fun decodeBitmapFromUri(
        context: Context,
        uri: Uri,
        maxWidth: Int,
        maxHeight: Int
    ): Bitmap? {
        val targetW = max(maxWidth, 256)
        val targetH = max(maxHeight, 256)
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    val iw = info.size.width
                    val ih = info.size.height
                    if (iw > targetW || ih > targetH) {
                        val scale = max(iw.toFloat() / targetW, ih.toFloat() / targetH)
                        val sampleW = max(1, (iw / scale).roundToInt())
                        val sampleH = max(1, (ih / scale).roundToInt())
                        decoder.setTargetSize(sampleW, sampleH)
                    }
                }
            } else {
                val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, boundsOpts)
                }
                var sampleSize = 1
                while (
                    boundsOpts.outWidth / sampleSize > targetW * 2 ||
                    boundsOpts.outHeight / sampleSize > targetH * 2
                ) {
                    sampleSize *= 2
                }
                val decodeOpts = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, decodeOpts)
                }
            }
        }.getOrNull()
    }

    fun exportBoardToBitmap(
        committedBitmap: Bitmap?,
        paperMode: PaperMode,
        width: Int,
        height: Int
    ): Bitmap {
        val outW = max(width, committedBitmap?.width ?: 1080)
        val outH = max(height, committedBitmap?.height ?: 1920)
        val result = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val bgColor = if (paperMode == PaperMode.DARK) PaperDark.toArgb() else PaperLight.toArgb()
        canvas.drawColor(bgColor)
        if (committedBitmap != null) {
            canvas.drawBitmap(committedBitmap, 0f, 0f, null)
        }
        return result
    }

    fun savePngToMediaStore(context: Context, bitmap: Bitmap): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        val name = generateFileName()
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "${Environment.DIRECTORY_PICTURES}/Handwriting Board"
            )
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
        return try {
            resolver.openOutputStream(uri)?.use { out ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                    throw IllegalStateException("PNG compression failed")
                }
            } ?: throw IllegalStateException("Failed to open MediaStore output stream")

            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            true
        } catch (_: Exception) {
            resolver.delete(uri, null, null)
            false
        }
    }

    fun writePngToUri(context: Context, bitmap: Bitmap, uri: Uri): Boolean {
        return runCatching {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            } ?: false
        }.getOrDefault(false)
    }

    fun createShareablePngUri(context: Context, bitmap: Bitmap): Uri? {
        return runCatching {
            val sharedDir = File(context.cacheDir, "shared").apply { mkdirs() }
            // Keep cache tidy by removing older shared exports
            sharedDir.listFiles()?.forEach { oldFile ->
                if (oldFile.isFile) oldFile.delete()
            }
            val file = File(sharedDir, generateFileName())
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        }.getOrNull()
    }
}
