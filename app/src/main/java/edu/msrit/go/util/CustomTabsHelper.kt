package edu.msrit.go.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent

object CustomTabsHelper {
    const val PARENTS_PORTAL_URL = "https://parents.msrit.edu/newparents/index.php"
    const val EXAM_PORTAL_URL = "https://exam.msrit.edu/"
    const val OE_PORTAL_URL = "https://msrit-oe.contineo.in:5055/index.php"

    /**
     * Launches the MSRIT Portal in a Chrome Custom Tab.
     * Uses genuine device browser engine (Chrome) so that the portal detects
     * a genuine Chrome session without any WebView detection blocking.
     */
    fun launchPortal(context: Context, url: String = PARENTS_PORTAL_URL) {
        try {
            // MSRIT Brand Colors for Custom Tab Toolbar & Navigation
            val toolbarColor = 0xFFE11D48.toInt() // MsritCrimson
            val surfaceColor = 0xFF0F172A.toInt() // DarkSurfaceHigh
            val navBarColor = 0xFF0A0D14.toInt()  // DarkBackground

            val defaultColorScheme = CustomTabColorSchemeParams.Builder()
                .setToolbarColor(toolbarColor)
                .setSecondaryToolbarColor(surfaceColor)
                .setNavigationBarColor(navBarColor)
                .build()

            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setUrlBarHidingEnabled(true)
                .setDefaultColorSchemeParams(defaultColorScheme)
                .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
                .build()

            customTabsIntent.launchUrl(context, Uri.parse(url))
        } catch (e: Exception) {
            // Fallback to regular browser Intent if Custom Tabs is unavailable
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "Unable to launch browser: ${ex.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
