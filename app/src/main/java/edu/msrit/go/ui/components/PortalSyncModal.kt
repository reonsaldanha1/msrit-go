package edu.msrit.go.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.*
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    var statusText by remember { mutableStateOf("Connecting to parents.msrit.edu...") }
    var isManualInteractionRequired by remember { mutableStateOf(startVisible) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isManualInteractionRequired) 12.dp else 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(24.dp)),
            color = DarkSurface
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Modal Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceHigh)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MsritCrimson),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Sync,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "MSRIT Portal Bridge",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = "parents.msrit.edu • Contineo",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = { webViewInstance?.reload() },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = AccentCyan, modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        color = MsritCrimson,
                        trackColor = DarkSurfaceHighest
                    )
                }

                // Status Banner
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = DarkSurfaceLowest
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = AccentCyan,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = statusText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        if (!isManualInteractionRequired) {
                            TextButton(
                                onClick = { isManualInteractionRequired = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Show Portal", fontSize = 11.sp, color = AccentCyan)
                            }
                        }
                    }
                }

                // Error alert if any
                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(StatusCriticalBg)
                            .border(1.dp, StatusCritical, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = StatusCritical, modifier = Modifier.size(20.dp))
                            Text(
                                text = errorMessage ?: "Login failed",
                                fontSize = 12.sp,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Web Content Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )

                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.useWideViewPort = true
                                settings.loadWithOverviewMode = true
                                settings.userAgentString =
                                    "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36 MSRIT-GO/1.1"

                                val cookieManager = CookieManager.getInstance()
                                cookieManager.setAcceptCookie(true)
                                cookieManager.setAcceptThirdPartyCookies(this, true)

                                addJavascriptInterface(
                                    PortalBridge(
                                        onLoginSuccess = { jsonPayload ->
                                            statusText = "Sync successful! Updating records..."
                                            onSuccess(jsonPayload)
                                        },
                                        onLoginFailure = { err ->
                                            errorMessage = err
                                            statusText = "Notice: $err"
                                            isManualInteractionRequired = true
                                            onError(err)
                                        },
                                        onVerificationRequired = {
                                            statusText = "Verification required on screen"
                                            isManualInteractionRequired = true
                                        },
                                        onProgressUpdate = { msg ->
                                            statusText = msg
                                        }
                                    ),
                                    "MsritBridge"
                                )

                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        isLoading = true
                                        url?.let { currentUrl = it }
                                        statusText = "Loading ${url?.take(35)}..."
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        isLoading = false
                                        url?.let { currentUrl = it }

                                        // Apply styling
                                        view?.evaluateJavascript(PortalBridge.getStyleEnhancementScript(), null)

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
                                }

                                loadUrl(PortalBridge.PORTAL_URL)
                                webViewInstance = this
                            }
                        },
                        update = { wv ->
                            webViewInstance = wv
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Footer Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceHigh)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextMuted)
                    }

                    Button(
                        onClick = {
                            statusText = "Extracting current page records..."
                            webViewInstance?.evaluateJavascript(PortalBridge.getScraperScript(), null)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MsritCrimson),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Extract Current Page", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
