package edu.msrit.go

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import edu.msrit.go.data.MockDataProvider
import edu.msrit.go.data.UserPreferences
import edu.msrit.go.ui.components.AppTab
import edu.msrit.go.ui.components.BottomNavBar
import edu.msrit.go.ui.components.TopHeaderBar
import edu.msrit.go.ui.screens.*
import edu.msrit.go.ui.theme.DarkBackground
import edu.msrit.go.ui.theme.MsritGoTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MsritGoTheme {
                val userPreferences = remember { UserPreferences(this@MainActivity) }
                var selectedTab by remember { mutableStateOf(AppTab.DASHBOARD) }
                var showLoginDialog by remember { mutableStateOf(false) }

                var currentUsn by remember { mutableStateOf(userPreferences.savedUsn) }
                var targetAttendance by remember { mutableStateOf(userPreferences.targetAttendance) }
                var isDemoMode by remember { mutableStateOf(userPreferences.isDemoMode) }

                val studentProfile = remember(currentUsn) {
                    MockDataProvider.getStudentProfile(currentUsn)
                }

                val attendanceList = remember { MockDataProvider.sampleAttendance }
                val cieMarksList = remember { MockDataProvider.sampleCieMarks }
                val circulars = remember { MockDataProvider.sampleCirculars }
                val campusServices = remember { MockDataProvider.campusServices }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkBackground),
                    containerColor = DarkBackground,
                    topBar = {
                        TopHeaderBar(
                            usn = currentUsn,
                            isDemoMode = isDemoMode,
                            onUsnClick = { showLoginDialog = true },
                            onSyncClick = {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Connected to MSRIT Portal: Academic records up to date!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    },
                    bottomBar = {
                        BottomNavBar(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = selectedTab,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "tabTransition"
                        ) { tab ->
                            when (tab) {
                                AppTab.DASHBOARD -> DashboardScreen(
                                    profile = studentProfile,
                                    attendanceList = attendanceList,
                                    cieMarksList = cieMarksList,
                                    circulars = circulars,
                                    onNavigateTab = { selectedTab = it },
                                    onOpenPortal = { selectedTab = AppTab.PORTAL }
                                )
                                AppTab.ATTENDANCE -> AttendanceScreen(
                                    attendanceList = attendanceList,
                                    targetAttendance = targetAttendance,
                                    onTargetChanged = { newTarget ->
                                        targetAttendance = newTarget
                                        userPreferences.targetAttendance = newTarget
                                    }
                                )
                                AppTab.MARKS -> MarksScreen(
                                    cieMarksList = cieMarksList,
                                    sgpa = studentProfile.sgpa,
                                    cgpa = studentProfile.cgpa
                                )
                                AppTab.PORTAL -> PortalWebViewScreen(
                                    userPreferences = userPreferences
                                )
                                AppTab.HUB -> CampusHubScreen(
                                    services = campusServices
                                )
                            }
                        }

                        if (showLoginDialog) {
                            LoginDialog(
                                userPreferences = userPreferences,
                                onDismiss = { showLoginDialog = false },
                                onSaved = {
                                    currentUsn = userPreferences.savedUsn
                                    isDemoMode = false
                                    showLoginDialog = false
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Credentials saved for $currentUsn",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
