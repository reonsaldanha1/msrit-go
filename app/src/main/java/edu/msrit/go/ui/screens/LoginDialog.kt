package edu.msrit.go.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import edu.msrit.go.data.StudentProfile
import edu.msrit.go.data.UserPreferences
import edu.msrit.go.ui.theme.*

@Composable
fun AccountProfileDialog(
    profile: StudentProfile,
    userPreferences: UserPreferences,
    lastSyncTime: String,
    onDismiss: () -> Unit,
    onTriggerSync: () -> Unit,
    onLogout: () -> Unit
) {
    var targetAttendance by remember { mutableStateOf(userPreferences.targetAttendance) }
    var isEditingTarget by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(24.dp)),
            color = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MsritCrimson),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Column {
                            Text(
                                text = profile.name.ifEmpty { "Student Profile" },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = TextPrimary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (userPreferences.isDemoMode) StatusWarning else StatusSafe)
                                )
                                Text(
                                    text = if (userPreferences.isDemoMode) "Demo Preview Mode" else "Synced: ${userPreferences.savedUsn}",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                HorizontalDivider(color = DarkBorder)

                // Academic Information Summary
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = DarkSurfaceHigh,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("USN", fontSize = 12.sp, color = TextMuted)
                            Text(profile.usn.ifEmpty { userPreferences.savedUsn }, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Branch & Sem", fontSize = 12.sp, color = TextMuted)
                            val semLabel = when (profile.semester) {
                                1 -> "1st Sem"
                                2 -> "2nd Sem"
                                3 -> "3rd Sem"
                                else -> "${profile.semester}th Sem"
                            }
                            val branchShort = if (profile.usn.contains("CI", ignoreCase = true) || profile.department.contains("AIML", ignoreCase = true) || profile.department.contains("Cyber", ignoreCase = true)) "CSE (AIML)" else profile.department.take(24)
                            Text("$semLabel • $branchShort", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Proctor", fontSize = 12.sp, color = TextMuted)
                            Text(profile.proctorName, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AccentCyan)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Sync Status", fontSize = 12.sp, color = TextMuted)
                            Text(lastSyncTime, fontSize = 11.sp, color = StatusSafe)
                        }
                    }
                }

                // Target Attendance Preference
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Target Attendance Goal",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                        Text(
                            text = "${targetAttendance.toInt()}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan
                        )
                    }

                    Slider(
                        value = targetAttendance.toFloat(),
                        onValueChange = {
                            targetAttendance = it.toDouble()
                            userPreferences.targetAttendance = it.toDouble()
                        },
                        valueRange = 75f..95f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor = AccentCyan,
                            activeTrackColor = MsritCrimson,
                            inactiveTrackColor = DarkSurfaceHighest
                        )
                    )
                }

                // Action Buttons: Re-sync and Logout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            onDismiss()
                            onTriggerSync()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MsritCrimson)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Re-sync Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onLogout()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusCritical),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusCritical.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Logout", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
