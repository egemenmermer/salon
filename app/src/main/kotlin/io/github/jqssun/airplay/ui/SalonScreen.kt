package io.github.jqssun.airplay.ui

import io.github.jqssun.airplay.VideoQuality
import io.github.jqssun.airplay.VideoQualityMode

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jqssun.airplay.*
import io.github.jqssun.airplay.R
import io.github.jqssun.airplay.service.AirPlayService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
private fun SalonButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Button(onClick, modifier.onFocusChanged { focused = it.isFocused }
        .border(if (focused) 3.dp else 1.dp, if (focused) Color(0xFFA7C7FF) else Color(0xFF424852), RoundedCornerShape(12.dp))
        .defaultMinSize(minHeight = 52.dp), shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (focused) Color(0xFFA7C7FF) else Color(0xFF1B1E24),
            contentColor = if (focused) Color(0xFF111315) else Color(0xFFF4F4F5))) {
        Text(text, fontSize = 18.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun SalonScreen(service: AirPlayService?, openSystemSettings: (Intent) -> Unit) {
    val state by (service?.salonState ?: remember { kotlinx.coroutines.flow.MutableStateFlow(SalonState()) }).collectAsState()
    val mirrorResolution by (service?.videoResolution ?: remember { kotlinx.coroutines.flow.MutableStateFlow("") }).collectAsState()
    val mirrorAspect by (service?.videoAspect ?: remember { kotlinx.coroutines.flow.MutableStateFlow(16f/9f) }).collectAsState()
    val hlsAspect by (service?.videoPlaybackAspect ?: remember { kotlinx.coroutines.flow.MutableStateFlow(16f/9f) }).collectAsState()
    val hls by (service?.videoPlaybackActive ?: remember { kotlinx.coroutines.flow.MutableStateFlow(false) }).collectAsState()
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(Prefs.NAME, android.content.Context.MODE_PRIVATE) }
    var returnToSettings by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var page by remember { mutableStateOf("home") }
    var panel by remember { mutableStateOf(false) }
    var interaction by remember { mutableLongStateOf(0) }
    var name by remember { mutableStateOf(prefs.getString(Prefs.SERVER_NAME, Prefs.DEF_SERVER_NAME)!!) }
    var pinEnabled by remember { mutableStateOf(prefs.getBoolean(Prefs.REQUIRE_PIN, true)) }
    var quality by remember { mutableStateOf(VideoQualityMode.stored(prefs.getString(VideoQuality.PREF,null),prefs.getString(Prefs.RESOLUTION,Prefs.DEF_RESOLUTION))) }
    var nameInvalid by remember { mutableStateOf(false) }
    var deferred by remember { mutableStateOf(false) }
    val first = remember { FocusRequester() }
    val videoFocus = remember { FocusRequester() }
    val playing = state.stage == SalonStage.VIDEO
    LaunchedEffect(state.stage) {
        if (state.stage in listOf(SalonStage.PIN, SalonStage.CONNECTING, SalonStage.AUDIO)) page = "home"
    }
    LaunchedEffect(page, panel, playing, state.stage) {
        if (playing && !panel) { page = "home"; videoFocus.requestFocus() }
        else { delay(50); runCatching { first.requestFocus() } }
    }
    LaunchedEffect(panel, interaction) { if (panel) { delay(5000); panel = false } }
    BackHandler(enabled = playing || page != "home" || panel) {
        when { panel -> panel = false; page != "home" -> page = when(page) { "more", "details" -> "help"; "about" -> "settings"; "licenses" -> "about"; else -> "home" }
            playing -> { panel = true; interaction++ } }
    }
    fun disconnect() { panel = false; service?.disconnectSession() }
    fun save() {
        nameInvalid = !SalonSettings.validName(name)
        if (nameInvalid) return
        prefs.edit().putString(Prefs.SERVER_NAME,name.trim()).putBoolean(Prefs.REQUIRE_PIN,pinEnabled)
            .putString(VideoQuality.PREF,quality.name)
            .putString(Prefs.RESOLUTION,when(quality) { VideoQualityMode.UHD -> "3840x2160"; VideoQualityMode.COMPATIBLE -> "1280x720"; else -> "1920x1080" })
            .putInt(Prefs.MAX_FPS,if(quality == VideoQualityMode.SMOOTH) 60 else 30).apply()
        deferred = state.stage in listOf(SalonStage.VIDEO, SalonStage.AUDIO, SalonStage.PIN, SalonStage.CONNECTING)
        service?.applySettings()
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(Color(0xFF111315)).onPreviewKeyEvent {
        if (panel && it.type == KeyEventType.KeyDown) interaction++
        if (playing && !panel && it.type == KeyEventType.KeyUp && (it.key == Key.DirectionCenter || it.key == Key.Enter || it.key == Key.NumPadEnter)) {
            panel = true; interaction++; true
        } else false
    }) {
        val ratio = (if (hls) hlsAspect else mirrorAspect).takeIf { it.isFinite() && it > 0 } ?: (16f/9f)
        val width = minOf(maxWidth, maxHeight * ratio)
        val height = width / ratio
        // Keep Surface attached while preparing; first visible renderer frame drives VIDEO.
        Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            VideoSurfaceView(
                onSurfaceAvailable = { if(hls) service?.setVideoPlaybackSurface(it) else service?.setVideoSurface(it) },
                onSurfaceDestroyed = { service?.clearVideoSurface(it); service?.clearVideoPlaybackSurface(it) },
                bufferSize = if(hls) null else VideoQuality.surfaceSize(mirrorResolution),
                applyAspectRatio = false, modifier = Modifier.width(width).height(height))
        }
        if (playing && !panel) Box(Modifier.fillMaxSize().focusRequester(videoFocus).focusable())
        if (!playing || page != "home") {
            Column(Modifier.fillMaxSize().background(Color(0xFF111315)).padding(horizontal = maxWidth * 0.05f, vertical = maxHeight * 0.05f)
                .then(if (page == "licenses") Modifier else Modifier.verticalScroll(rememberScrollState())), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)) {
                when(page) {
                    "settings" -> {
                        Text(stringResource(R.string.salon_settings),fontSize=28.sp,fontWeight=FontWeight.SemiBold)
                        OutlinedTextField(name,{name=it; nameInvalid=false},label={Text(stringResource(R.string.salon_name))},
                            singleLine=true,isError=nameInvalid,modifier=Modifier.widthIn(max=480.dp).fillMaxWidth().focusRequester(first))
                        if(nameInvalid) Text(stringResource(R.string.salon_invalid_name),color=Color(0xFFE8B4AD))
                        SalonButton(stringResource(if(pinEnabled) R.string.salon_pin_on else R.string.salon_pin_off)) { pinEnabled=!pinEnabled }
                        if(!pinEnabled) Text(stringResource(R.string.salon_open_access),textAlign=TextAlign.Center)
                        val qualityLabel = when(quality) {
                            VideoQualityMode.AUTOMATIC -> R.string.salon_auto
                            VideoQualityMode.SMOOTH -> R.string.salon_smooth
                            VideoQualityMode.UHD -> R.string.salon_uhd
                            VideoQualityMode.COMPATIBLE -> R.string.salon_compatible
                        }
                        SalonButton(stringResource(qualityLabel)) { quality=VideoQualityMode.entries[(quality.ordinal+1)%VideoQualityMode.entries.size] }
                        Text(stringResource(R.string.salon_quality_hint),fontSize=18.sp,textAlign=TextAlign.Center)
                        SalonButton(stringResource(R.string.salon_save)) { save() }
                        if(deferred) Text(stringResource(R.string.salon_deferred),textAlign=TextAlign.Center)
                        SalonButton(stringResource(R.string.salon_about_help)) { page="about" }
                        SalonButton(stringResource(R.string.salon_back)) { page="home" }
                    }
                    "licenses" -> {
                        Text(stringResource(R.string.salon_licenses), fontSize=28.sp)
                        SalonButton(stringResource(R.string.salon_back), Modifier.focusRequester(first)) { page="about" }
                        val licenseText=remember { context.assets.open("OPEN_SOURCE_LICENSES.txt").bufferedReader().use { it.readText() } }
                        val scroll=rememberScrollState()
                        Box(Modifier.weight(1f).fillMaxWidth().onPreviewKeyEvent {
                            if(it.type != KeyEventType.KeyDown) false
                            else when(it.key) {
                                Key.DirectionDown -> { scope.launch { scroll.animateScrollBy(200f) }; true }
                                Key.DirectionUp -> if(scroll.value > 0) { scope.launch { scroll.animateScrollBy(-200f) }; true } else false
                                else -> false
                            }
                        }.focusable().verticalScroll(scroll)) {
                            Text(licenseText,fontSize=18.sp,color=Color(0xFFB9C0CA))
                        }
                    }
                    "help", "more", "about", "details" -> {
                        val title=when(page){ "more"->R.string.salon_help_more_title; "about"->R.string.salon_about; "details"->R.string.salon_details; "licenses"->R.string.salon_licenses; else->R.string.salon_help_title }
                        val body=when(page){ "more"->R.string.salon_help_more; "about"->R.string.salon_about_body; "details"->R.string.salon_details_body; else->R.string.salon_help_body }
                        Text(stringResource(title),fontSize=28.sp,fontWeight=FontWeight.SemiBold)
                        val licenseText = remember(page) { if(page=="licenses") context.assets.open("OPEN_SOURCE_LICENSES.txt").bufferedReader().use { it.readText() } else "" }
                        Text(if(page=="licenses") licenseText else stringResource(body),fontSize=20.sp,textAlign=TextAlign.Center,modifier=Modifier.widthIn(max=760.dp))
                        if(page=="help") {
                            SalonButton(stringResource(R.string.salon_retry),Modifier.focusRequester(first)) { service?.retry(); page="home" }
                            SalonButton(stringResource(R.string.salon_more)) { page="more" }
                            SalonButton(stringResource(R.string.salon_details)) { page="details" }
                        } else {
                            if(page=="about") {
                                Text(stringResource(R.string.salon_version),fontSize=20.sp)
                                SalonButton(stringResource(R.string.salon_licenses),Modifier.focusRequester(first)) { page="licenses" }
                            }
                            if(page=="details") {
                                Text(state.detail.ifEmpty { stringResource(R.string.salon_no_error) },fontSize=18.sp)
                                val logs by (service?.diagnostics ?: remember { kotlinx.coroutines.flow.MutableStateFlow(emptyList<String>()) }).collectAsState()
                                Text(logs.takeLast(12).joinToString("\n"),fontSize=14.sp)
                                SalonButton(stringResource(R.string.salon_clear_logs),Modifier.focusRequester(first)) { service?.clearDiagnostics() }
                            }
                        }
                        Text(stringResource(R.string.salon_drm),fontSize=18.sp,color=Color(0xFFB9C0CA),textAlign=TextAlign.Center)
                        SalonButton(stringResource(R.string.salon_back),if(page!="help" && page!="details" && page!="about") Modifier.focusRequester(first) else Modifier) { page=when(page) { "more", "details" -> "help"; "about" -> "settings"; "licenses" -> "about"; else -> "home" } }
                    }
                    else -> {
                        val title=when(state.stage){
                            SalonStage.READY->R.string.salon_ready; SalonStage.NO_NETWORK->R.string.salon_network_title
                            SalonStage.PERMISSION->R.string.salon_permission_title; SalonStage.START_ERROR->R.string.salon_start_error
                            SalonStage.VIDEO_ERROR->R.string.salon_video_error; SalonStage.PIN->R.string.salon_pin_title
                            SalonStage.CONNECTING->R.string.salon_connecting; SalonStage.AUDIO->R.string.salon_audio
                            else->R.string.salon_preparing }
                        val body=when(state.stage){
                            SalonStage.NO_NETWORK->R.string.salon_network_body; SalonStage.PERMISSION->R.string.salon_permission_body
                            SalonStage.VIDEO_ERROR->R.string.salon_video_error_body; SalonStage.CONNECTING->R.string.salon_connecting_body
                            SalonStage.AUDIO->R.string.salon_audio_body; SalonStage.READY->R.string.salon_intro
                            SalonStage.PIN->R.string.salon_pin_body; else->R.string.salon_preparing_body }
                        Text(stringResource(title),fontSize=24.sp,color=when(state.stage){SalonStage.READY->Color(0xFF9BD0B0);SalonStage.START_ERROR,SalonStage.VIDEO_ERROR->Color(0xFFE8B4AD);else->Color(0xFFB9C0CA)})
                        Text(if(state.stage==SalonStage.PIN) state.pin.orEmpty() else state.tvName,fontSize=48.sp,fontWeight=FontWeight.SemiBold,textAlign=TextAlign.Center)
                        val licenseText = remember(page) { if(page=="licenses") context.assets.open("OPEN_SOURCE_LICENSES.txt").bufferedReader().use { it.readText() } else "" }
                        Text(if(page=="licenses") licenseText else stringResource(body),fontSize=20.sp,textAlign=TextAlign.Center,modifier=Modifier.widthIn(max=760.dp))
                        if(state.stage==SalonStage.READY) {
                            Text(stringResource(R.string.salon_steps,state.tvName),fontSize=22.sp,textAlign=TextAlign.Center)
                            Text(stringResource(R.string.salon_same_network),fontSize=18.sp,color=Color(0xFFB9C0CA),textAlign=TextAlign.Center)
                        }
                        Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp)) {
                            when(state.stage) {
                                SalonStage.NO_NETWORK -> {
                                    SalonButton(stringResource(R.string.salon_network_settings),Modifier.focusRequester(first)) { openSystemSettings(Intent(Settings.ACTION_WIFI_SETTINGS)) }
                                    SalonButton(stringResource(R.string.salon_retry)) { service?.retry() }
                                }
                                SalonStage.PERMISSION -> SalonButton(stringResource(R.string.salon_app_settings),Modifier.focusRequester(first)) { openSystemSettings(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+context.packageName))) }
                                SalonStage.START_ERROR -> {
                                    SalonButton(stringResource(R.string.salon_retry),Modifier.focusRequester(first)) { service?.retry() }
                                    SalonButton(stringResource(R.string.salon_details)) { page="details" }
                                }
                                SalonStage.AUDIO, SalonStage.PIN, SalonStage.CONNECTING -> SalonButton(stringResource(R.string.salon_disconnect),Modifier.focusRequester(first)) { disconnect() }
                                SalonStage.VIDEO_ERROR -> {
                                    SalonButton(stringResource(R.string.salon_help),Modifier.focusRequester(first)) { page="help" }
                                    SalonButton(stringResource(R.string.salon_disconnect)) { disconnect() }
                                }
                                else -> Row(horizontalArrangement=Arrangement.spacedBy(20.dp)) {
                                    SalonButton(stringResource(R.string.salon_help),if(!returnToSettings) Modifier.focusRequester(first) else Modifier) { returnToSettings=false; page="help" }
                                    SalonButton(stringResource(R.string.salon_settings),if(returnToSettings) Modifier.focusRequester(first) else Modifier) { returnToSettings=true; page="settings" }
                                }
                            }
                        }
                    }
                }
            }
        }
        if(panel && playing) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha=0.65f)),contentAlignment=Alignment.Center) {
                Column(Modifier.widthIn(max=560.dp).background(Color(0xFF1B1E24),RoundedCornerShape(16.dp)).padding(32.dp),
                    horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(20.dp)) {
                    Text(stringResource(R.string.salon_mirroring),fontSize=26.sp)
                    SalonButton(stringResource(R.string.salon_continue),Modifier.focusRequester(first)) { panel=false }
                    SalonButton(stringResource(R.string.salon_disconnect)) { disconnect() }
                }
            }
        }
    }
}
