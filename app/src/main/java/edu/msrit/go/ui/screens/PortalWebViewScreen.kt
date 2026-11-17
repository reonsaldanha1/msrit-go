package edu.msrit.go.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.net.Uri
import android.view.ViewGroup
import android.webkit.*
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import java.io.ByteArrayInputStream
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import edu.msrit.go.R
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

    // Captcha & Security Code state
    var captchaBase64 by remember { mutableStateOf<String?>(null) }
    var enteredCaptcha by remember { mutableStateOf("") }
    var showCaptchaDialog by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(showCaptchaDialog) {
        if (showCaptchaDialog) {
            kotlinx.coroutines.delay(250)
            try {
                focusRequester.requestFocus()
            } catch (e: Exception) {}
        }
    }

    // Auto-dismiss automation status badge after 3 seconds
    LaunchedEffect(automationStatus) {
        if (automationStatus != null) {
            kotlinx.coroutines.delay(3000)
            automationStatus = null
        }
    }

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
                    // Use a clean Chrome 131 UA — no custom token that flags as WebView/third-party
                    settings.userAgentString =
                        "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.135 Mobile Safari/537.36"

                    // Disable sending X-Requested-With header which signals third-party apps to servers
                    if (WebViewFeature.isFeatureSupported(WebViewFeature.REQUESTED_WITH_HEADER_ALLOW_LIST)) {
                        WebSettingsCompat.setRequestedWithHeaderOriginAllowList(
                            settings,
                            setOf()
                        )
                    }

                    // Inject cloaking script before any page script executes
                    if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
                        WebViewCompat.addDocumentStartJavaScript(
                            this,
                            PortalBridge.getWebViewCloakingScript(),
                            setOf("https://parents.msrit.edu", "http://parents.msrit.edu")
                        )
                    }

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

                    val bridge = PortalBridge(
                        onLoginSuccess = { _ ->
                            automationStatus = "Login successful!"
                        },
                        onLoginFailure = { err ->
                            automationStatus = null
                            Toast.makeText(context, "Portal: $err", Toast.LENGTH_SHORT).show()
                        },
                        onVerificationRequired = {
                            automationStatus = "2-step verification challenge detected..."
                        },
                        onProgressUpdate = { msg ->
                            automationStatus = msg
                        },
                        onSaveCredentials = { usn, day, month, year, vType, vDigits ->
                            if (usn.isNotBlank()) userPreferences.savedUsn = usn
                            if (day.isNotBlank()) userPreferences.savedDobDay = day
                            if (month.isNotBlank()) userPreferences.savedDobMonth = month
                            if (year.isNotBlank()) userPreferences.savedDobYear = year
                            if (vType.isNotBlank()) userPreferences.savedVerificationType = vType
                            if (vDigits.isNotBlank()) userPreferences.savedVerificationDigits = vDigits
                            userPreferences.rememberMe = true
                            userPreferences.isLoggedIn = true
                        },
                        onCaptchaDetected = { b64 ->
                            captchaBase64 = b64
                            showCaptchaDialog = true
                            automationStatus = "Security code required"
                        }
                    ).apply {
                        onHtmlLoaded = { url, html ->
                            currentUrl = url
                            loadDataWithBaseURL(url, html, "text/html", "UTF-8", url)
                        }
                        onNavigateUrl = { redirectUrl ->
                            currentUrl = redirectUrl
                            loadUrl(redirectUrl)
                        }
                    }

                    addJavascriptInterface(bridge, "MsritBridge")

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
                        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                            if (request == null) return null
                            val intercepted = PortalBridge.interceptPortalRequest(request)
                            if (intercepted != null) return intercepted
                            return super.shouldInterceptRequest(view, request)
                        }

                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            isLoading = true
                            url?.let { currentUrl = it }
                            canGoBack = canGoBack()
                            canGoForward = canGoForward()
                            // Cloak WebView identity as early as possible so the portal doesn't block us
                            view?.evaluateJavascript(PortalBridge.getWebViewCloakingScript(), null)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                            url?.let { currentUrl = it }
                            canGoBack = canGoBack()
                            canGoForward = canGoForward()

                            // Auto-inject styling & run automated credentials and verification handler
                            if (url != null && url.contains("parents.msrit.edu")) {
                                // Re-run cloaking at page finish to override any page-level JS detection
                                view?.evaluateJavascript(PortalBridge.getWebViewCloakingScript(), null)
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

        // 3. Status Notification Pill (Top Center)
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

        // 4. Discreet Floating Quick Actions Button (Bottom Right)
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

        // 4b. Floating Security Code Button (if detected and dialog is closed)
        if (captchaBase64 != null && !showCaptchaDialog) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                Surface(
                    onClick = { showCaptchaDialog = true },
                    shape = RoundedCornerShape(20.dp),
                    color = MsritCrimson,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text("Enter Security Code", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4c. User Input Window for Portal Security Code (CAPTCHA)
        if (showCaptchaDialog) {
            Dialog(onDismissRequest = { showCaptchaDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = DarkSurfaceHigh,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MsritCrimson),
                    shadowElevation = 16.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Title Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Security Verification",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Type the 5 characters shown below",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                            IconButton(onClick = { showCaptchaDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                            }
                        }

                        // CAPTCHA Image
                        val bmp = remember(captchaBase64) {
                            try {
                                if (captchaBase64 == null) null
                                else {
                                    val clean = if (captchaBase64!!.contains(",")) captchaBase64!!.substringAfter(",") else captchaBase64!!
                                    val bytes = Base64.decode(clean, Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                }
                            } catch (e: Exception) {
                                null
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (bmp != null) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Security Code",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(72.dp),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.height(72.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color = MsritCrimson,
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        "Extracting security code...",
                                        color = Color.DarkGray,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        // Refresh Code button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    webViewInstance?.evaluateJavascript(PortalBridge.getCaptchaRefreshScript(), null)
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New code", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Input field for 5 characters (Auto-focused with keyboard)
                        OutlinedTextField(
                            value = enteredCaptcha,
                            onValueChange = {
                                if (it.length <= 5) {
                                    enteredCaptcha = it.uppercase()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 8.sp,
                                textAlign = TextAlign.Center,
                                color = Color.White
                            ),
                            placeholder = {
                                Text(
                                    text = "5 CHARACTERS",
                                    fontSize = 14.sp,
                                    letterSpacing = 2.sp,
                                    color = TextMuted,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (enteredCaptcha.length >= 4) {
                                        showCaptchaDialog = false
                                        automationStatus = "Verifying & logging in..."
                                        val script = PortalBridge.getCaptchaSubmitScript(enteredCaptcha)
                                        webViewInstance?.evaluateJavascript(script, null)
                                    }
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MsritCrimson,
                                unfocusedBorderColor = DarkBorder,
                                focusedContainerColor = DarkSurfaceHighest,
                                unfocusedContainerColor = DarkSurfaceHighest
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Submit Button
                        Button(
                            onClick = {
                                if (enteredCaptcha.isNotBlank()) {
                                    showCaptchaDialog = false
                                    automationStatus = "Verifying & logging in..."
                                    val script = PortalBridge.getCaptchaSubmitScript(enteredCaptcha)
                                    webViewInstance?.evaluateJavascript(script, null)
                                }
                            },
                            enabled = enteredCaptcha.length >= 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MsritCrimson,
                                disabledContainerColor = DarkSurfaceHighest
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.LockOpen, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "Verify & Login",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
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

                        // Reload Action Button (Prominent & Bigger)
                        Button(
                            onClick = {
                                webViewInstance?.reload()
                                showMenuDialog = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MsritCrimson)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reload Page",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Reload Portal Page",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Auto-fill trigger button
                        if (userPreferences.savedUsn.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    automationStatus = "Auto-filling credentials..."
                                    runAutoLogin(webViewInstance)
                                    showMenuDialog = false
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
