package edu.msrit.go.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
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
import edu.msrit.go.data.SubjectAttendance
import edu.msrit.go.ui.theme.*

@Composable
fun BunkCalculatorModal(
    subject: SubjectAttendance,
    onDismiss: () -> Unit
) {
    var classesToBunk by remember { mutableStateOf(1) }
    var classesToAttend by remember { mutableStateOf(1) }
    var selectedMode by remember { mutableStateOf(0) } // 0: "If I miss", 1: "If I attend"

    val simulatedPercentage = remember(classesToBunk, classesToAttend, selectedMode) {
        if (selectedMode == 0) {
            // Missing classes -> total increases, attended stays same
            val newTotal = subject.total + classesToBunk
            (subject.attended.toDouble() / newTotal) * 100.0
        } else {
            // Attending classes -> both increase
            val newAttended = subject.attended + classesToAttend
            val newTotal = subject.total + classesToAttend
            (newAttended.toDouble() / newTotal) * 100.0
        }
    }

    val simulatedStatusColor = when {
        simulatedPercentage >= 85.0 -> StatusSafe
        simulatedPercentage >= 75.0 -> StatusWarning
        else -> StatusCritical
    }

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
                    Column {
                        Text(
                            text = "Attendance Simulator",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = "${subject.code} • ${subject.title}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = AccentCyan
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }

                // Current Status Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurfaceHigh)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Current Attendance",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "${subject.attended} / ${subject.total} classes",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = "%.1f%%".format(subject.percentage),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = if (subject.percentage >= 85.0) StatusSafe else StatusWarning
                    )
                }

                // Simulation Mode Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceLowest)
                        .padding(4.dp)
                ) {
                    TabPill(
                        title = "If I miss classes",
                        isSelected = selectedMode == 0,
                        onClick = { selectedMode = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    TabPill(
                        title = "If I attend classes",
                        isSelected = selectedMode == 1,
                        onClick = { selectedMode = 1 },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Stepper Counter
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val count = if (selectedMode == 0) classesToBunk else classesToAttend
                    val countLabel = if (selectedMode == 0) "Classes to skip" else "Classes to attend"

                    Text(
                        text = countLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (selectedMode == 0 && classesToBunk > 1) classesToBunk--
                                if (selectedMode == 1 && classesToAttend > 1) classesToAttend--
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceHigh)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = TextPrimary)
                        }

                        Text(
                            text = "$count",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black
                            ),
                            color = TextPrimary
                        )

                        IconButton(
                            onClick = {
                                if (selectedMode == 0) classesToBunk++
                                if (selectedMode == 1) classesToAttend++
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceHigh)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = TextPrimary)
                        }
                    }
                }

                // Simulated Result Badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceHigh)
                        .border(1.dp, simulatedStatusColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Projected Attendance",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "%.2f%%".format(simulatedPercentage),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = simulatedStatusColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when {
                                simulatedPercentage >= 85.0 -> "Eligible for CIE & SEE (Safe Zone)"
                                simulatedPercentage >= 75.0 -> "Warning Zone (Requires Condonation)"
                                else -> "Shortage Alert (Below Minimum 75%)"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = simulatedStatusColor
                        )
                    }
                }

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MsritCrimson),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Got it",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun TabPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) DarkSurfaceHigh else Color.Transparent)
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
