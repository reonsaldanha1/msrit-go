package edu.msrit.go.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.window.DialogProperties
import edu.msrit.go.data.AttendanceSession
import edu.msrit.go.data.AttendanceStatus
import edu.msrit.go.data.SubjectAttendance
import edu.msrit.go.ui.theme.*

@Composable
fun AttendanceDetailModal(
    subject: SubjectAttendance,
    onDismiss: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }

    val totalHeld = subject.total
    val totalAttended = subject.attended
    val totalAbsent = subject.absent

    val filteredSessions = remember(subject.sessions, selectedFilter) {
        when (selectedFilter) {
            "Present" -> subject.sessions.filter { it.isPresent }
            "Absent" -> subject.sessions.filter { !it.isPresent }
            else -> subject.sessions
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(26.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(26.dp)),
            color = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarkSurfaceHigh)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = subject.code,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentCyan
                                )
                            }
                            Text(
                                text = "${subject.credits} Credits • ${subject.type}",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = subject.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextPrimary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                // Summary Attendance Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(18.dp)),
                    color = DarkSurfaceLowest
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Attendance Standing",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (subject.status) {
                                                AttendanceStatus.SAFE -> StatusSafe
                                                AttendanceStatus.WARNING -> StatusWarning
                                                AttendanceStatus.CRITICAL -> StatusCritical
                                            }
                                        )
                                )
                                Text(
                                    text = when (subject.status) {
                                        AttendanceStatus.SAFE -> "Eligible (Safe Zone)"
                                        AttendanceStatus.WARNING -> "Condonation Zone"
                                        AttendanceStatus.CRITICAL -> "Shortage Alert"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = when (subject.status) {
                                        AttendanceStatus.SAFE -> StatusSafe
                                        AttendanceStatus.WARNING -> StatusWarning
                                        AttendanceStatus.CRITICAL -> StatusCritical
                                    }
                                )
                            }
                        }

                        Text(
                            text = "%.1f%%".format(subject.percentage),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black
                            ),
                            color = when (subject.status) {
                                AttendanceStatus.SAFE -> StatusSafe
                                AttendanceStatus.WARNING -> StatusWarning
                                AttendanceStatus.CRITICAL -> StatusCritical
                            }
                        )
                    }
                }

                // Quick Count Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CountPill(
                        label = "Attended",
                        count = totalAttended.toString(),
                        color = StatusSafe,
                        modifier = Modifier.weight(1f)
                    )
                    CountPill(
                        label = "Absent",
                        count = totalAbsent.toString(),
                        color = StatusCritical,
                        modifier = Modifier.weight(1f)
                    )
                    CountPill(
                        label = "Total Held",
                        count = totalHeld.toString(),
                        color = TextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(
                        "All" to "All (${subject.sessions.size})",
                        "Present" to "Present ($totalAttended)",
                        "Absent" to "Absent ($totalAbsent)"
                    )
                    items(filters) { (key, label) ->
                        val isSelected = selectedFilter == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) DarkSurfaceHighest else DarkSurfaceLowest)
                                .border(
                                    1.dp,
                                    if (isSelected) AccentCyan else DarkBorder,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedFilter = key }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else TextSecondary
                            )
                        }
                    }
                }

                // Date-wise Session History List
                if (subject.sessions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurfaceLowest)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.EventNote,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Summary: $totalAttended present out of $totalHeld classes",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Detailed class-by-class date records will be pulled directly from parents.msrit.edu during live sync.",
                                fontSize = 11.sp,
                                color = TextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredSessions) { session ->
                            SessionRowItem(session = session)
                        }
                    }
                }

                // Close Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceHigh),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun CountPill(
    label: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp)),
        color = DarkSurfaceLowest
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = count,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun SessionRowItem(session: AttendanceSession) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                1.dp,
                if (session.isPresent) DarkBorder else StatusCritical.copy(alpha = 0.4f),
                RoundedCornerShape(14.dp)
            ),
        color = DarkSurfaceLowest
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (session.isPresent) StatusSafeBg else StatusCriticalBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (session.isPresent) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (session.isPresent) StatusSafe else StatusCritical,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = session.date.ifEmpty { "Class Session" },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = session.timeOrSlot.ifEmpty { "Regular Period" },
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    if (session.topicOrRemark.isNotBlank()) {
                        Text(
                            text = session.topicOrRemark,
                            fontSize = 10.sp,
                            color = AccentCyan
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (session.isPresent) StatusSafeBg else StatusCriticalBg)
                    .border(
                        1.dp,
                        if (session.isPresent) StatusSafe else StatusCritical,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (session.isPresent) "PRESENT" else "ABSENT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (session.isPresent) StatusSafe else StatusCritical
                )
            }
        }
    }
}
