package edu.msrit.go.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import edu.msrit.go.data.PortalBridge
import edu.msrit.go.data.UserPreferences
import edu.msrit.go.ui.theme.*

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PortalWebViewScreen(
    userPreferences: UserPreferences,
    onDataExtracted: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var currentUrl by remember { mutableStateOf(PortalBridge.PORTAL_URL) }
    var isLoading by remember { mutableStateOf(false) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }

    var automationStatus by remember { mutableStateOf<String?>(null) }

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // WebView Control Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Navigation controls (Back, Forward, Refresh)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { webViewInstance?.goBack() },
                            enabled = canGoBack,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = if (canGoBack) Color.White else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { webViewInstance?.goForward() },
                            enabled = canGoForward,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = "Forward",
                                tint = if (canGoForward) Color.White else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { webViewInstance?.reload() },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Reload",
                                tint = AccentCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Scrape & Sync Current Page button
                    Button(
                        onClick = {
                            Toast.makeText(context, "Extracting records from portal...", Toast.LENGTH_SHORT).show()
                            webViewInstance?.evaluateJavascript(PortalBridge.getScraperScript(), null)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MsritCrimson),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Extract Data",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Auto-Login Credentials & Verification Action
                    OutlinedButton(
                        onClick = {
                            runAutoLogin(webViewInstance)
                            Toast.makeText(context, "Executing auto-login & verification...", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = "Auto Login",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Open in Chrome/External browser
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Default.OpenInBrowser,
                            contentDescription = "Open in browser",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Quick Portal Switching Pills (Parents, Exam, Open Elective)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PortalSwitchPill(
                        title = "Parents Portal",
                        isSelected = currentUrl.contains("parents.msrit.edu"),
                        onClick = {
                            currentUrl = PortalBridge.PORTAL_URL
                            webViewInstance?.loadUrl(PortalBridge.PORTAL_URL)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    PortalSwitchPill(
                        title = "Exam Portal",
                        isSelected = currentUrl.contains("exam.msrit.edu"),
                        onClick = {
                            currentUrl = PortalBridge.EXAM_URL
                            webViewInstance?.loadUrl(PortalBridge.EXAM_URL)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    PortalSwitchPill(
                        title = "Open Electives",
                        isSelected = currentUrl.contains("msrit-oe"),
                        onClick = {
                            currentUrl = PortalBridge.OE_URL
                            webViewInstance?.loadUrl(PortalBridge.OE_URL)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Live Automation Progress Banner
        AnimatedVisibility(visible = automationStatus != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                color = DarkSurfaceHigh,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MsritCrimson)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
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
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Loading Indicator Bar
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = MsritCrimson,
                trackColor = DarkSurfaceHigh
            )
        }

        // Web Content View
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
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
                        settings.databaseEnabled = true
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = true
                        settings.builtInZoomControls = true
                        settings.displayZoomControls = false
                        settings.userAgentString =
                            "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36 MSRIT-GO/1.4"

                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)

                        addJavascriptInterface(
                            PortalBridge(
                                onLoginSuccess = { json ->
                                    automationStatus = "Auto-login complete! Academic records synced."
                                    onDataExtracted(json)
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
                                    runAutoLogin(view)
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
                                    runAutoLogin(view)
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
        }
    }
}

@Composable
private fun PortalSwitchPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) DarkSurfaceHighest else DarkSurfaceLowest)
            .border(
                1.dp,
                if (isSelected) AccentCyan else DarkBorder,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else TextMuted
        )
    }
}
