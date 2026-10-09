package io.github.jqssun.airplay.ui

import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

// shared surface for mirroring (VideoRenderer) and airplay video (AirPlayVideoPlayer)
@Composable
fun VideoSurfaceView(
    onSurfaceAvailable: (Surface) -> Unit,
    onSurfaceDestroyed: (Surface) -> Unit,
    aspectRatio: Float = 16f / 9f,
    bufferSize: Pair<Int,Int>? = null,
    // false = caller sizes the surface (content-scale modes)
    applyAspectRatio: Boolean = true,
    modifier: Modifier = Modifier
) {
    val available = rememberUpdatedState(onSurfaceAvailable)
    val destroyed = rememberUpdatedState(onSurfaceDestroyed)
    val callbacks = remember {
        object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                available.value(holder.surface)
            }
            override fun surfaceChanged(holder: SurfaceHolder, fmt: Int, w: Int, h: Int) {
                available.value(holder.surface)
            }
            override fun surfaceDestroyed(holder: SurfaceHolder) {
                destroyed.value(holder.surface)
            }
        }
    }

    val appliedBuffer = remember { arrayOfNulls<Pair<Int,Int>>(1) }
    AndroidView(
        factory = { ctx ->
            SurfaceView(ctx).also {
                it.holder.addCallback(callbacks)
            }
        },
        update = { view ->
            if(appliedBuffer[0] != bufferSize) {
                if(bufferSize == null) view.holder.setSizeFromLayout()
                else view.holder.setFixedSize(bufferSize.first,bufferSize.second)
                appliedBuffer[0] = bufferSize
            }
            if(view.holder.surface.isValid) available.value(view.holder.surface)
        },
        modifier = if (applyAspectRatio) {
            modifier
                .aspectRatio(aspectRatio, matchHeightConstraintsFirst = aspectRatio < 1f)
                .fillMaxSize()
        } else {
            modifier
        }
    )
}
