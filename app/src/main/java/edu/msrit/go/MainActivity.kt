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
import edu.msrit.go.data.AcademicDataRepository
import edu.msrit.go.data.UserPreferences
import edu.msrit.go.ui.components.AppTab
import edu.msrit.go.ui.components.BottomNavBar
import edu.msrit.go.ui.components.PortalSyncModal
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
                val repository = remember { AcademicDataRepository(userPreferences) }

                var isLoggedIn by remember { mutableStateOf(userPreferences.isLoggedIn) }
                var selectedTab by remember { mutableStateOf(AppTab.DASHBOARD) }
                var showAccountDialog by remember { mutableStateOf(false) }
                var showPortalSyncModal by remember { mutableStateOf(false) }

                // Sync target credentials
                var syncUsn by remember { mutableStateOf(userPreferences.savedUsn) }
                var syncDay by remember { mutableStateOf(userPreferences.savedDobDay) }
                var syncMonth by remember { mutableStateOf(userPreferences.savedDobMonth) }
                var syncYear by remember { mutableStateOf(userPreferences.savedDobYear) }

                val studentProfile by repository.studentProfile.collectAsState()
                val attendanceList by repository.attendanceList.collectAsState()
                val cieMarksList by repository.cieMarksList.collectAsState()
                val circulars by repository.circulars.collectAsState()
                val isSyncing by repository.isSyncing.collectAsState()
                val lastSyncDisplay by repository.lastSyncDisplay.collectAsState()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkBackground)
                ) {
                    if (!isLoggedIn) {
                        // First-screen experience: Must ask to login using credentials on website login page
                        LoginScreen(
                            userPreferences = userPreferences,
                            onLoginWithPortal = { usn, day, month, year, rememberMe ->
                                syncUsn = usn
                                syncDay = day
                                syncMonth = month
                                syncYear = year
                                userPreferences.savedUsn = usn
                                userPreferences.savedDobDay = day
                                userPreferences.savedDobMonth = month
                                userPreferences.savedDobYear = year
                                userPreferences.rememberMe = rememberMe
                                showPortalSyncModal = true
                            },
                            onExploreDemoMode = {
                                repository.setDemoData("1MS22CS042")
                                userPreferences.savedUsn = "1MS22CS042"
                                userPreferences.isLoggedIn = true
                                isLoggedIn = true
                                Toast.makeText(
                                    this@MainActivity,
                                    "Welcome to MSRIT GO (Demo Mode)",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    } else {
                        // Main authenticated application scaffold
                        Scaffold(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(DarkBackground),
                            containerColor = DarkBackground,
                            topBar = {
                                TopHeaderBar(
                                    usn = studentProfile.usn.ifEmpty { userPreferences.savedUsn },
                                    isDemoMode = userPreferences.isDemoMode,
                                    lastSyncDisplay = lastSyncDisplay,
                                    isSyncing = isSyncing,
                                    onUsnClick = { showAccountDialog = true },
                                    onSyncClick = {
                                        syncUsn = userPreferences.savedUsn
                                        syncDay = userPreferences.savedDobDay
                                        syncMonth = userPreferences.savedDobMonth
                                        syncYear = userPreferences.savedDobYear
                                        showPortalSyncModal = true
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
                                            targetAttendance = userPreferences.targetAttendance,
                                            onTargetChanged = { newTarget ->
                                                userPreferences.targetAttendance = newTarget
                                            }
                                        )
                                        AppTab.MARKS -> MarksScreen(
                                            cieMarksList = cieMarksList,
                                            sgpa = studentProfile.sgpa,
                                            cgpa = studentProfile.cgpa
                                        )
                                        AppTab.PORTAL -> PortalWebViewScreen(
                                            userPreferences = userPreferences,
                                            onDataExtracted = { json ->
                                                repository.updateFromExtractedJson(json)
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "Records updated from portal page!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        )
                                        AppTab.HUB -> CampusHubScreen(
                                            services = repository.circulars.value.let {
                                                edu.msrit.go.data.MockDataProvider.campusServices
                                            }
                                        )
                                    }
                                }

                                if (showAccountDialog) {
                                    AccountProfileDialog(
                                        profile = studentProfile,
                                        userPreferences = userPreferences,
                                        lastSyncTime = lastSyncDisplay,
                                        onDismiss = { showAccountDialog = false },
                                        onTriggerSync = {
                                            syncUsn = userPreferences.savedUsn
                                            syncDay = userPreferences.savedDobDay
                                            syncMonth = userPreferences.savedDobMonth
                                            syncYear = userPreferences.savedDobYear
                                            showPortalSyncModal = true
                                        },
                                        onLogout = {
                                            userPreferences.logout()
                                            isLoggedIn = false
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Logged out from MSRIT GO",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Portal Sync & Authentication Modal
                    if (showPortalSyncModal) {
                        PortalSyncModal(
                            usn = syncUsn,
                            day = syncDay,
                            month = syncMonth,
                            year = syncYear,
                            onSuccess = { json ->
                                val ok = repository.updateFromExtractedJson(json)
                                if (ok) {
                                    userPreferences.isLoggedIn = true
                                    isLoggedIn = true
                                    showPortalSyncModal = false
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Connected to parents.msrit.edu! Records up to date.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            onError = { err ->
                                Toast.makeText(
                                    this@MainActivity,
                                    "Portal connection notice: $err",
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            onDismiss = { showPortalSyncModal = false }
                        )
                    }
                }
            }
        }
    }
}
