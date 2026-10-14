package edu.msrit.go

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import edu.msrit.go.data.UserPreferences
import edu.msrit.go.ui.screens.LoginScreen
import edu.msrit.go.ui.screens.PortalWebViewScreen
import edu.msrit.go.ui.theme.DarkBackground
import edu.msrit.go.ui.theme.MsritGoTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MsritGoTheme {
                val userPreferences = remember { UserPreferences(this@MainActivity) }
                var isLoggedIn by remember { mutableStateOf(userPreferences.isLoggedIn) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkBackground)
                ) {
                    AnimatedContent(
                        targetState = isLoggedIn,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "authTransition"
                    ) { loggedIn ->
                        if (!loggedIn) {
                            // First-time or logged-out experience: Show Login Screen
                            LoginScreen(
                                userPreferences = userPreferences,
                                onLoginWithPortal = { usn, day, month, year, verificationType, verificationDigits, rememberMe ->
                                    userPreferences.savedUsn = usn
                                    userPreferences.savedDobDay = day
                                    userPreferences.savedDobMonth = month
                                    userPreferences.savedDobYear = year
                                    userPreferences.savedVerificationType = verificationType
                                    userPreferences.savedVerificationDigits = verificationDigits
                                    userPreferences.rememberMe = rememberMe
                                    userPreferences.isLoggedIn = true
                                    isLoggedIn = true
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Launching Full-Screen Portal for $usn...",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onOpenPortalDirectly = {
                                    userPreferences.isLoggedIn = true
                                    isLoggedIn = true
                                }
                            )
                        } else {
                            // Full-screen MSRIT Portal View
                            PortalWebViewScreen(
                                userPreferences = userPreferences,
                                onLogout = {
                                    userPreferences.logout()
                                    isLoggedIn = false
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Signed out from MSRIT GO",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
