package edu.msrit.go.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import edu.msrit.go.data.PortalBridge
import edu.msrit.go.data.UserPreferences
import edu.msrit.go.ui.theme.*

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PortalWebViewScreen(
    userPreferences: UserPreferences,
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var currentUrl by remember { mutableStateOf(PortalBridge.PORTAL_URL) }
    var isLoading by remember { mutableStateOf(false) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var showMenuDialog by remember { mutableStateOf(false) }
    var automationStatus by remember { mutableStateOf<String?>(null) }

    // Hardware / gesture back button handler
    BackHandler(enabled = canGoBack) {
        webViewInstance?.goBack()
    }

    fun runAutoLogin(wv: WebView?) {
        if (wv == null) return
        val script = PortalBridge.getAutoSubmitScript(
            usn = userPreferences.savedUsn,
            day = userPreferences.savedDobDay,
            month = userPreferences.savedDobMonth,
            year = userPreferences.savedDobYear,
            verificationType = userPreferences.savedVerificationType,
            verificationDigits = userPreferences.savedVerificationDigits
        )
        wv.evaluateJavascript(script, null)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // 1. Truly Full-Screen WebView
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.databaseEnabled = true
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    settings.userAgentString =
                        "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36 MSRIT-GO/1.7"

                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)
                    cookieManager.setAcceptThirdPartyCookies(this, true)

                    // Download support for PDF hall tickets, marks cards, etc.
                    setDownloadListener { url, _, _, _, _ ->
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            ctx.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(ctx, "Opening download...", Toast.LENGTH_SHORT).show()
                        }
                    }

                    addJavascriptInterface(
                        PortalBridge(
                            onLoginSuccess = { _ ->
                                automationStatus = "Auto-login complete!"
                            },
                            onLoginFailure = { err ->
                                automationStatus = null
                                Toast.makeText(context, "Portal notice: $err", Toast.LENGTH_SHORT).show()
                            },
                            onVerificationRequired = {
                                automationStatus = "2-step verification challenge detected..."
                            },
                            onProgressUpdate = { msg ->
                                automationStatus = msg
                            }
                        ),
                        "MsritBridge"
                    )

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            if (newProgress >= 70 && view?.url?.contains("parents.msrit.edu") == true) {
                                if (userPreferences.savedUsn.isNotBlank()) {
                                    runAutoLogin(view)
                                }
                            }
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            isLoading = true
                            url?.let { currentUrl = it }
                            canGoBack = canGoBack()
                            canGoForward = canGoForward()
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                            url?.let { currentUrl = it }
                            canGoBack = canGoBack()
                            canGoForward = canGoForward()

                            // Auto-inject styling & run automated credentials and verification handler
                            if (url != null && url.contains("parents.msrit.edu")) {
                                view?.evaluateJavascript(PortalBridge.getStyleEnhancementScript(), null)
                                if (userPreferences.savedUsn.isNotBlank()) {
                                    runAutoLogin(view)
                                }
                            }
                        }
                    }

                    loadUrl(currentUrl)
                    webViewInstance = this
                }
            },
            update = { wv ->
                webViewInstance = wv
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Slim Top Loading Bar
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(3.dp),
                color = MsritCrimson,
                trackColor = Color.Transparent
            )
        }

        // 3. Status Notification Pill
        AnimatedVisibility(
            visible = automationStatus != null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 8.dp)
        ) {
            Surface(
                color = DarkSurfaceHigh.copy(alpha = 0.95f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MsritCrimson),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = AccentCyan
                    )
                    Text(
                        text = automationStatus ?: "",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }
        }

        // 4. Discreet Floating Quick Actions Button (Non-intrusive, bottom right)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = { showMenuDialog = true },
                containerColor = DarkSurfaceHigh.copy(alpha = 0.92f),
                contentColor = AccentCyan,
                shape = CircleShape,
                modifier = Modifier
                    .size(50.dp)
                    .border(1.dp, DarkBorder, CircleShape)
                    .shadow(8.dp, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Portal Menu",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // 5. Quick Controls Dialog / Sheet
        if (showMenuDialog) {
            Dialog(onDismissRequest = { showMenuDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = DarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Title Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "MSRIT Portal Controls",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = if (userPreferences.savedUsn.isNotBlank()) "Signed in as ${userPreferences.savedUsn}" else "Guest Session",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                            IconButton(onClick = { showMenuDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                            }
                        }

                        HorizontalDivider(color = DarkBorder)

                        // Navigation Row: Back, Forward, Reload
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    webViewInstance?.goBack()
                                    showMenuDialog = false
                                },
                                enabled = canGoBack,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White,
                                    disabledContentColor = TextMuted
                                )
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Back", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    webViewInstance?.goForward()
                                    showMenuDialog = false
                                },
                                enabled = canGoForward,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White,
                                    disabledContentColor = TextMuted
                                )
                            ) {
                                Text("Forward", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }

                            Button(
                                onClick = {
                                    webViewInstance?.reload()
                                    showMenuDialog = false
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MsritCrimson)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reload", fontSize = 12.sp)
                            }
                        }

                        // Auto-fill trigger button
                        if (userPreferences.savedUsn.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    runAutoLogin(webViewInstance)
                                    showMenuDialog = false
                                    Toast.makeText(context, "Injecting credentials...", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.Key, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Auto-fill My Credentials (${userPreferences.savedUsn})", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Portal Switcher Section
                        Text(
                            text = "Switch Portal Website",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PortalSwitchButton(
                                title = "Parents",
                                isSelected = currentUrl.contains("parents.msrit.edu"),
                                onClick = {
                                    currentUrl = PortalBridge.PORTAL_URL
                                    webViewInstance?.loadUrl(PortalBridge.PORTAL_URL)
                                    showMenuDialog = false
                                },
                                modifier = Modifier.weight(1f)
                            )
                            PortalSwitchButton(
                                title = "Exam",
                                isSelected = currentUrl.contains("exam.msrit.edu"),
                                onClick = {
                                    currentUrl = PortalBridge.EXAM_URL
                                    webViewInstance?.loadUrl(PortalBridge.EXAM_URL)
                                    showMenuDialog = false
                                },
                                modifier = Modifier.weight(1f)
                            )
                            PortalSwitchButton(
                                title = "Electives",
                                isSelected = currentUrl.contains("msrit-oe"),
                                onClick = {
                                    currentUrl = PortalBridge.OE_URL
                                    webViewInstance?.loadUrl(PortalBridge.OE_URL)
                                    showMenuDialog = false
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider(color = DarkBorder)

                        // Action Links: Open in Chrome & Change Login / Sign Out
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl))
                                    context.startActivity(intent)
                                    showMenuDialog = false
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                            ) {
                                Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open in Browser", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    showMenuDialog = false
                                    onLogout()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StatusCritical.copy(alpha = 0.85f))
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sign Out / Login", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PortalSwitchButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) DarkSurfaceHighest else DarkSurfaceLowest)
            .border(
                1.dp,
                if (isSelected) AccentCyan else DarkBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else TextMuted
        )
    }
}
