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
import edu.msrit.go.data.SubjectAttendance
import edu.msrit.go.ui.theme.*

@Composable
fun AttendanceDetailModal(
    subject: SubjectAttendance,
    allSubjects: List<SubjectAttendance> = emptyList(),
    onSubjectSelected: (SubjectAttendance) -> Unit = {},
    onDismiss: () -> Unit
) {
    var currentSubject by remember(subject) { mutableStateOf(subject) }

    val courseList = remember(allSubjects, currentSubject) {
        if (allSubjects.isNotEmpty()) allSubjects else listOf(currentSubject)
    }

    val presentSessions = remember(currentSubject.sessions) {
        currentSubject.sessions.filter { it.isPresent }
    }

    val absentSessions = remember(currentSubject.sessions) {
        currentSubject.sessions.filter { !it.isPresent }
    }

    val totalHeld = currentSubject.total
    val totalAttended = currentSubject.attended
    val totalAbsent = currentSubject.absent
    val stillToGo = if (currentSubject.stillToGo > 0) currentSubject.stillToGo else maxOf(0, 75 - totalHeld)

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
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(24.dp)),
            color = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MsritCrimson),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "MSRIT Attendance Portal",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = "Ramaiah Institute of Technology",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }

                // 1. Course Code Selector Tabs Row (Switch courses directly as per MSRIT portal)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "SELECT COURSE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 0.5.sp
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(courseList) { sub ->
                            val isSelected = sub.code.equals(currentSubject.code, ignoreCase = true)
                            Surface(
                                onClick = {
                                    currentSubject = sub
                                    onSubjectSelected(sub)
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MsritCrimson else DarkSurfaceLowest,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) MsritCrimsonLight else DarkBorder
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = sub.code,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                        color = if (isSelected) Color.White else TextSecondary
                                    )
                                    // Attendance percentage badge in chip
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isSelected) Color.Black.copy(alpha = 0.3f) else DarkSurfaceHigh)
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "${sub.percentage.toInt()}%",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else (if (sub.percentage >= 85) StatusSafe else StatusCritical)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Main Scrollable Content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 2. Faculty Profile Card (matching Image 1 from Portal)
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = DarkSurfaceLowest,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar Placeholder
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(DarkSurfaceHigh)
                                        .border(1.dp, AccentCyan.copy(alpha = 0.4f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = AccentCyan,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = currentSubject.faculty.ifEmpty { "Dept Faculty Mentor" },
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${currentSubject.code} - ${currentSubject.title}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextSecondary
                                    )
                                    if (currentSubject.venueOrBatch.isNotBlank()) {
                                        Text(
                                            text = currentSubject.venueOrBatch,
                                            fontSize = 11.sp,
                                            color = AccentCyan
                                        )
                                    }
                                    if (currentSubject.facultyEmail.isNotBlank()) {
                                        Text(
                                            text = currentSubject.facultyEmail,
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }
                                    if (currentSubject.facultyPhone.isNotBlank()) {
                                        Text(
                                            text = "Phone: ${currentSubject.facultyPhone}",
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Attendance Status Section (matching Image 3 from Portal)
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = DarkSurfaceLowest,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Attendance Status",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = TextPrimary
                                    )

                                    // Percentage pill
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (currentSubject.percentage >= 85) StatusSafeBg else StatusCriticalBg,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (currentSubject.percentage >= 85) StatusSafe else StatusCritical
                                        )
                                    ) {
                                        Text(
                                            text = "%.1f%%".format(currentSubject.percentage),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (currentSubject.percentage >= 85) StatusSafe else StatusCritical,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                // 3 Badges: PRESENT [X], ABSENT [Y], STILL TO GO [Z]
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Present badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF059669))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "PRESENT[$totalAttended]",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    // Absent badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFDC2626))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = if (totalAbsent > 0) "ABSENT[$totalAbsent]" else "ABSENT[]",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    // Still to go badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF4B5563))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "STILL TO GO [$stillToGo]",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                // Two-tone Progress Bar (Green present vs grey still to go)
                                val denom = maxOf(1, totalAttended + totalAbsent + stillToGo)
                                val presentRatio = (totalAttended.toFloat() / denom).coerceIn(0f, 1f)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(Color(0xFF4B5563))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(presentRatio)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(Color(0xFF10B981))
                                    )
                                }
                            }
                        }
                    }

                    // 4. Present Classes Table Section (matching Image 3)
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Present",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF059669))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "CLASSES $totalAttended",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            // Table Header
                            TableHeaderRow()

                            // Sessions Rows
                            if (presentSessions.isEmpty()) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = DarkSurfaceLowest
                                ) {
                                    Text(
                                        text = "No attended sessions recorded yet.",
                                        fontSize = 12.sp,
                                        color = TextMuted,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    presentSessions.forEachIndexed { index, session ->
                                        SessionTableRow(
                                            sl = if (session.slNo.isNotBlank()) session.slNo else "${index + 1}",
                                            date = session.date,
                                            time = session.timeOrSlot,
                                            status = "Present",
                                            isPresent = true,
                                            isEven = index % 2 == 0
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 5. Absent Classes Table Section
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Absent List",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (totalAbsent > 0) Color(0xFFDC2626) else DarkSurfaceHigh)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (totalAbsent > 0) "CLASSES $totalAbsent" else "CLASSES 0",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            if (absentSessions.isEmpty()) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = DarkSurfaceLowest,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = StatusSafe,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "No absences recorded. You have 100% attendance in this subject!",
                                            fontSize = 12.sp,
                                            color = StatusSafe,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            } else {
                                TableHeaderRow()
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    absentSessions.forEachIndexed { index, session ->
                                        SessionTableRow(
                                            sl = if (session.slNo.isNotBlank()) session.slNo else "${index + 1}",
                                            date = session.date,
                                            time = session.timeOrSlot,
                                            status = "Absent",
                                            isPresent = false,
                                            isEven = index % 2 == 0
                                        )
                                    }
                                }
                            }
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
                    Text("Close", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TableHeaderRow() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = DarkSurfaceHigh
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SL NO",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                modifier = Modifier.width(48.dp)
            )
            Text(
                text = "DATE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                modifier = Modifier.width(96.dp)
            )
            Text(
                text = "TIME",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "STATUS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                modifier = Modifier.width(64.dp)
            )
        }
    }
}

@Composable
private fun SessionTableRow(
    sl: String,
    date: String,
    time: String,
    status: String,
    isPresent: Boolean,
    isEven: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        color = if (isEven) DarkSurfaceLowest else DarkSurfaceLowest.copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            if (isPresent) DarkBorder else StatusCritical.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = sl,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                modifier = Modifier.width(48.dp)
            )
            Text(
                text = date,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.width(96.dp)
            )
            Text(
                text = time,
                fontSize = 11.sp,
                color = TextSecondary,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isPresent) StatusSafeBg else StatusCriticalBg)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = status,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPresent) StatusSafe else StatusCritical
                )
            }
        }
    }
}
