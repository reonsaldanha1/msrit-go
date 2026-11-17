package edu.msrit.go.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.msrit.go.R
import edu.msrit.go.data.UserPreferences
import edu.msrit.go.ui.theme.*
import edu.msrit.go.util.CustomTabsHelper

@Composable
fun PortalCustomTabScreen(
    userPreferences: UserPreferences,
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    fun copyToClipboard(label: String, text: String) {
        if (text.isNotBlank()) {
            clipboardManager.setText(AnnotatedString(text))
            Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Background ambient glow
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-60).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(MsritCrimsonDark.copy(alpha = 0.35f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-100).dp, y = 80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(MsritNavyDark.copy(alpha = 0.45f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkSurfaceHigh)
                            .border(1.dp, MsritCrimsonLight.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.msrit_logo),
                            contentDescription = "MSRIT Crest",
                            modifier = Modifier
                                .size(34.dp)
                                .padding(2.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "MSRIT",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "GO",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MsritCrimson
                            )
                        }
                        Text(
                            text = "Portal Hub (Chrome Custom Tabs)",
                            fontSize = 11.sp,
                            color = AccentCyan,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceHigh)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Sign Out",
                        tint = StatusCritical,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // User Info Badge
            if (userPreferences.savedUsn.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurfaceHigh,
                    border = BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MsritCrimson.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MsritCrimson,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = userPreferences.savedUsn,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                val dob = listOf(
                                    userPreferences.savedDobDay,
                                    userPreferences.savedDobMonth,
                                    userPreferences.savedDobYear
                                ).filter { it.isNotBlank() }.joinToString(" - ")

                                if (dob.isNotBlank()) {
                                    Text(
                                        text = "DOB: $dob",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { copyToClipboard("USN", userPreferences.savedUsn) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copy USN",
                                tint = AccentCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Primary Action Card: Open Parents Portal
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = DarkSurfaceHigh,
                border = BorderStroke(1.5.dp, MsritCrimson.copy(alpha = 0.8f)),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(MsritCrimson, MsritCrimsonDark)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Language,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Parents Portal",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "parents.msrit.edu • Attendance, Marks & CIE",
                            fontSize = 12.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }

                    Button(
                        onClick = {
                            CustomTabsHelper.launchPortal(context, CustomTabsHelper.PARENTS_PORTAL_URL)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MsritCrimson)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.OpenInBrowser,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Launch Portal in Chrome",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Quick Portals Grid (Exam & Open Electives)
            Text(
                text = "Other MSRIT Services",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, top = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Exam Portal
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurfaceHigh,
                    border = BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            CustomTabsHelper.launchPortal(context, CustomTabsHelper.EXAM_PORTAL_URL)
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Assignment,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "Exam Portal",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "exam.msrit.edu",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                // Open Electives
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurfaceHigh,
                    border = BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            CustomTabsHelper.launchPortal(context, CustomTabsHelper.OE_PORTAL_URL)
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = Color(0xFFA78BFA),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "Open Electives",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "msrit-oe portal",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            // Quick Copy Credentials Card
            if (userPreferences.savedUsn.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = DarkSurfaceHigh,
                    border = BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Key,
                                    contentDescription = null,
                                    tint = AccentCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Saved Credentials",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            TextButton(
                                onClick = {
                                    val full = buildString {
                                        append("USN: ").append(userPreferences.savedUsn).append("\n")
                                        val d = userPreferences.savedDobDay
                                        val m = userPreferences.savedDobMonth
                                        val y = userPreferences.savedDobYear
                                        if (d.isNotBlank()) append("DOB: ").append("$d-$m-$y").append("\n")
                                        if (userPreferences.savedVerificationDigits.isNotBlank()) {
                                            append(userPreferences.savedVerificationType).append(": ").append(userPreferences.savedVerificationDigits)
                                        }
                                    }
                                    copyToClipboard("All credentials", full)
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Copy All", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        HorizontalDivider(color = DarkBorder.copy(alpha = 0.5f))

                        // USN Row
                        CredentialRow(
                            label = "USN",
                            value = userPreferences.savedUsn,
                            onCopy = { copyToClipboard("USN", userPreferences.savedUsn) }
                        )

                        // Password (DOB) Row
                        val dobFormatted = listOf(
                            userPreferences.savedDobDay,
                            userPreferences.savedDobMonth,
                            userPreferences.savedDobYear
                        ).filter { it.isNotBlank() }.joinToString("-")

                        if (dobFormatted.isNotBlank()) {
                            CredentialRow(
                                label = "Password (DOB)",
                                value = dobFormatted,
                                onCopy = { copyToClipboard("Password (DOB)", dobFormatted) }
                            )
                        }

                        // 2-Step verification digits
                        if (userPreferences.savedVerificationDigits.isNotBlank()) {
                            CredentialRow(
                                label = userPreferences.savedVerificationType.replace("Last 4 Digits", "4 Digits"),
                                value = userPreferences.savedVerificationDigits,
                                onCopy = { copyToClipboard("Verification digits", userPreferences.savedVerificationDigits) }
                            )
                        }
                    }
                }
            }

            // Genuine Chrome Benefits Info
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkSurfaceLowest,
                border = BorderStroke(1.dp, DarkBorder.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StatusSafe,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Why Chrome Custom Tabs?",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Text(
                        text = "• Genuine Chrome Engine: Solves all third-party app blocks permanently.\n" +
                               "• 5-Character Security Code: Rendered cleanly with full zoom support.\n" +
                               "• Native Autofill: Chrome securely remembers your USN and Password.\n" +
                               "• Fast & Integrated: Styled with MSRIT Crimson and quick return to the app.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun CredentialRow(
    label: String,
    value: String,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextMuted
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        IconButton(
            onClick = onCopy,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                Icons.Default.ContentCopy,
                contentDescription = "Copy $label",
                tint = TextSecondary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
