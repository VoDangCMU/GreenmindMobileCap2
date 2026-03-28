package com.vodang.greenmind.wastereport

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.vodang.greenmind.api.httpClient
import io.ktor.client.request.*
import io.ktor.client.statement.*

private sealed interface ImageState {
    data object Loading : ImageState
    data class Success(val bitmap: ImageBitmap) : ImageState
    data object Error : ImageState
}

@Composable
actual fun NetworkImage(url: String, modifier: Modifier) {
    var state by remember(url) { mutableStateOf<ImageState>(ImageState.Loading) }

    LaunchedEffect(url) {
        state = ImageState.Loading
        state = try {
            val bytes = httpClient.get(url).readBytes()
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bmp != null) ImageState.Success(bmp.asImageBitmap()) else ImageState.Error
        } catch (_: Throwable) {
            ImageState.Error
        }
    }

    when (val s = state) {
        ImageState.Loading -> Box(modifier.background(Color(0xFFEEEEEE)), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Color(0xFF2E7D32))
        }
        ImageState.Error -> Box(modifier.background(Color(0xFFEEEEEE)), contentAlignment = Alignment.Center) {
            // empty placeholder
        }
        is ImageState.Success -> Image(
            bitmap = s.bitmap,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    }
}
