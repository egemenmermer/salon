package io.github.jqssun.airplay

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.github.jqssun.airplay.service.AirPlayService
import io.github.jqssun.airplay.ui.SalonScreen
import io.github.jqssun.airplay.ui.theme.AirPlayTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var receiver by mutableStateOf<AirPlayService?>(null)
    private var bound = false
    private var visible = false
    private var externalSettings = false
    private var screenJob: Job? = null
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val svc = (binder as? AirPlayService.LocalBinder)?.service ?: return
            receiver = svc
            svc.setUiVisible(visible)
            screenJob = lifecycleScope.launch {
                svc.salonState.collect {
                    if (visible && it.stage == SalonStage.VIDEO) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
        }
        override fun onServiceDisconnected(name: ComponentName?) { receiver = null }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        bound = bindService(Intent(this, AirPlayService::class.java), connection, BIND_AUTO_CREATE)
        setContent {
            AirPlayTheme {
                SalonScreen(receiver) { intent ->
                    runCatching { externalSettings = true; startActivity(intent) }
                        .onFailure { externalSettings = false }
                }
            }
        }
    }
    override fun onStart() {
        super.onStart(); visible = true; externalSettings = false
        receiver?.setUiVisible(true)
    }
    override fun onStop() {
        visible = false
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (!isChangingConfigurations && !externalSettings) receiver?.setUiVisible(false)
        super.onStop()
    }
    override fun onDestroy() {
        screenJob?.cancel()
        if (bound) unbindService(connection)
        super.onDestroy()
    }
}
