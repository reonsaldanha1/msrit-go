package edu.msrit.go.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import edu.msrit.go.data.PortalBridge
import edu.msrit.go.ui.theme.*

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PortalSyncModal(
    usn: String,
    day: String,
    month: String,
    year: String,
    verificationType: String = "Father Mobile Last 4 Digits",
    verificationDigits: String = "",
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit,
    onDismiss: () -> Unit,
    startVisible: Boolean = false
) {
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var currentUrl by remember { mutableStateOf(PortalBridge.PORTAL_URL) }
    var isLoading by remember { mutableStateOf(true) }
    var statusText by remember { mutableStateOf("Connecting to Ramaiah Institute of Technology...") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var stepIndex by remember { mutableIntStateOf(1) }

    // Pulsing rotation animation for loading spinner
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val bridge = remember {
        PortalBridge(
            onLoginSuccess = { jsonPayload ->
                statusText = "Sync complete! Loading dashboard..."
                onSuccess(jsonPayload)
            },
            onLoginFailure = { err ->
                errorMessage = err
                isLoading = false
                onError(err)
            },
            onVerificationRequired = {
                statusText = "Completing 2-step verification..."
                stepIndex = 2
            },
            onProgressUpdate = { progressMsg ->
                statusText = progressMsg
                if (progressMsg.contains("2-Step", ignoreCase = true) || progressMsg.contains("Verification", ignoreCase = true)) {
                    stepIndex = 2
                } else if (progressMsg.contains("Extracting", ignoreCase = true) || progressMsg.contains("records", ignoreCase = true)) {
                    stepIndex = 3
                }
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground.copy(alpha = 0.96f))
                .statusBarsPadding()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Hidden background WebView: performs automated login & scraping without showing the raw website
            Box(
                modifier = Modifier
                    .size(0.dp)
                    .clip(RoundedCornerShape(0.dp))
            ) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(1, 1)

                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.useWideViewPort = true
                            settings.loadWithOverviewMode = true
                            settings.userAgentString =
                                "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36 MSRIT-GO/1.2"

                            addJavascriptInterface(bridge, "MsritBridge")

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    isLoading = true
                                    url?.let { currentUrl = it }
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    isLoading = false
                                    url?.let { currentUrl = it }

                                    if (url != null && url.contains("parents.msrit.edu")) {
                                        // Execute automated login and verification handler
                                        view?.evaluateJavascript(
                                            PortalBridge.getAutoSubmitScript(
                                                usn = usn,
                                                day = day,
                                                month = month,
                                                year = year,
                                                verificationType = verificationType,
                                                verificationDigits = verificationDigits
                                            ),
                                            null
                                        )
                                    }
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?
                                ) {
                                    if (request?.isForMainFrame == true) {
                                        errorMessage = "Network connection failed. Check your internet connection."
                                        isLoading = false
                                    }
                                }
                            }

                            loadUrl(PortalBridge.PORTAL_URL)
                            webViewInstance = this
                        }
                    },
                    update = { wv ->
                        webViewInstance = wv
                    }
                )
            }

            // Beautiful Stitch Loading Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(28.dp)),
                color = DarkSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Glowing Animated Brand Badge
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        // Outer rotating glow border
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .rotate(rotation)
                                .clip(CircleShape)
                                .background(
                                    Brush.sweepGradient(
                                        listOf(
                                            MsritCrimson,
                                            AccentCyan,
                                            Color.Transparent,
                                            MsritCrimson
                                        )
                                    )
                                )
                        )
                        // Inner Badge
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(DarkSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(66.dp)
                                    .clip(CircleShape)
                                    .background(MsritCrimson),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }

                    // Title & Subtitle
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Syncing Academic Records",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.3.sp
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = "Ramaiah Institute of Technology • Contineo SIS",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }

                    // Progress Track Bar
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = MsritCrimson,
                            trackColor = DarkSurfaceHighest
                        )

                        // Current status message with pulsing indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (errorMessage != null) StatusCritical else StatusSafe)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = statusText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Step Pills Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SyncStepPill(number = 1, label = "Credentials", isActive = stepIndex >= 1)
                        SyncStepPill(number = 2, label = "Verification", isActive = stepIndex >= 2)
                        SyncStepPill(number = 3, label = "Data Sync", isActive = stepIndex >= 3)
                    }

                    // Error Alert Box if any
                    if (errorMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(StatusCriticalBg)
                                .border(1.dp, StatusCritical, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = StatusCritical, modifier = Modifier.size(20.dp))
                                Text(
                                    text = errorMessage ?: "Portal connection notice",
                                    fontSize = 12.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Retry Button
                        Button(
                            onClick = {
                                errorMessage = null
                                isLoading = true
                                statusText = "Retrying connection..."
                                webViewInstance?.reload()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MsritCrimson),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retry Connection", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Bottom Action: Skip / Offline Fallback
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = if (errorMessage != null) "Close & Use Cached Records" else "Skip to Offline Dashboard",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncStepPill(
    number: Int,
    label: String,
    isActive: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(if (isActive) MsritCrimson else DarkSurfaceHigh),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) Color.White else TextMuted
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isActive) TextPrimary else TextMuted
        )
    }
}
