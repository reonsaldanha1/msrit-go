package edu.msrit.go.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import edu.msrit.go.data.CircularItem
import edu.msrit.go.data.StudentProfile
import edu.msrit.go.data.SubjectAttendance
import edu.msrit.go.data.SubjectCieMarks
import edu.msrit.go.ui.components.AppTab
import edu.msrit.go.ui.theme.*

@Composable
fun DashboardScreen(
    profile: StudentProfile,
    attendanceList: List<SubjectAttendance>,
    cieMarksList: List<SubjectCieMarks>,
    circulars: List<CircularItem>,
    onNavigateTab: (AppTab) -> Unit,
    onOpenPortal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showProctorModal by remember { mutableStateOf(false) }

    val semLabel = when (profile.semester) {
        1 -> "1st Sem"
        2 -> "2nd Sem"
        3 -> "3rd Sem"
        else -> "${profile.semester}th Sem"
    }
    val branchBadge = if (profile.usn.contains("CI", ignoreCase = true) || profile.department.contains("AIML", ignoreCase = true) || profile.department.contains("Cyber", ignoreCase = true)) "CSE (AIML)" else "CSE"

    val totalAttended = attendanceList.sumOf { it.attended }
    val totalClasses = attendanceList.sumOf { it.total }
    val overallPercentage = if (totalClasses == 0) 100.0 else (totalAttended.toDouble() / totalClasses) * 100.0
    val avgCie = if (cieMarksList.isEmpty()) 0.0 else cieMarksList.map { it.totalInternal }.average()

    val shortageSubjects = attendanceList.filter { it.percentage < 85.0 }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
    ) {
        // Student Profile Hero Banner
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(22.dp)),
                color = DarkSurface
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MsritNavyDark.copy(alpha = 0.8f),
                                    MsritCrimsonDark.copy(alpha = 0.5f)
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Welcome back,",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted
                                )
                                Text(
                                    text = profile.name,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Black
                                    ),
                                    color = Color.White
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkSurfaceHighest)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$semLabel • $branchBadge",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentCyan
                                    )
                                    Text(
                                        text = "Sec ${profile.section} • UG",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }

                        Divider(color = DarkBorder.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "USN: ${profile.usn}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showProctorModal = true }
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SupervisorAccount,
                                    contentDescription = null,
                                    tint = AccentCyan,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Proctor: ${profile.proctorName}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Attendance & Performance Quick Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Attendance Card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                        .clickable { onNavigateTab(AppTab.ATTENDANCE) },
                    color = DarkSurface
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ATTENDANCE",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (overallPercentage >= 85.0) StatusSafe else StatusWarning)
                            )
                        }

                        Text(
                            text = "%.1f%%".format(overallPercentage),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black
                            ),
                            color = if (overallPercentage >= 85.0) StatusSafe else StatusWarning
                        )

                        Text(
                            text = "$totalAttended of $totalClasses attended",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                // Average Internal Card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                        .clickable { onNavigateTab(AppTab.MARKS) },
                    color = DarkSurface
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AVG CIE SCORE",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Icon(
                                imageVector = Icons.Default.Grade,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = "%.1f".format(avgCie),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black
                            ),
                            color = AccentCyan
                        )

                        Text(
                            text = "Out of 50 • SGPA: ${profile.sgpa}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Shortage Warnings (if any subject < 85%)
        if (shortageSubjects.isNotEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, StatusWarning.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
                    color = StatusWarningBg
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = StatusWarning,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${shortageSubjects.size} Subject(s) Below 85% MSRIT Rule",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = StatusWarning
                            )
                            Text(
                                text = shortageSubjects.joinToString { "${it.code} (${it.percentage.toInt()}%)" },
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = TextPrimary
                            )
                        }

                        TextButton(onClick = { onNavigateTab(AppTab.ATTENDANCE) }) {
                            Text("Fix", color = AccentCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Proctorship & Faculty Mentorship Section
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(20.dp)),
                color = DarkSurface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header
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
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentCyan.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = AccentCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "PROCTORSHIP & MENTOR",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextMuted,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Faculty Mentorship System",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Status Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(StatusSafeBg)
                                .border(1.dp, StatusSafe.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(StatusSafe)
                                )
                                Text(
                                    text = "Assigned",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusSafe
                                )
                            }
                        }
                    }

                    // Proctor Profile Card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp)),
                        color = DarkSurfaceHigh
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Avatar with Initials
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(MsritNavyDark, AccentPurple)
                                        )
                                    )
                                    .border(1.5.dp, AccentCyan.copy(alpha = 0.6f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = profile.proctorName.split(" ")
                                        .filter { it.isNotBlank() }
                                        .take(2)
                                        .map { it.first() }
                                        .joinToString(""),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = profile.proctorName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${profile.proctorRole} • ${profile.proctorCabin}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AccentCyan
                                )
                            }
                        }
                    }

                    // Contact Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Email button
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:${profile.proctorEmail}")
                                        putExtra(Intent.EXTRA_SUBJECT, "MSRIT Student Query - USN ${profile.usn}")
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Ignore
                                    }
                                },
                            color = DarkSurfaceHighest
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = AccentCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Column {
                                    Text("Email Mentor", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text(profile.proctorEmail, fontSize = 9.sp, color = TextMuted, maxLines = 1)
                                }
                            }
                        }

                        // Call button
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${profile.proctorPhone}")
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Ignore
                                    }
                                },
                            color = DarkSurfaceHighest
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = StatusSafe,
                                    modifier = Modifier.size(16.dp)
                                )
                                Column {
                                    Text("Call Mentor", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text(profile.proctorPhone, fontSize = 9.sp, color = TextMuted)
                                }
                            }
                        }
                    }

                    // Latest Proctorial Notes from portal
                    if (profile.proctorNotes.isNotEmpty()) {
                        val latestNote = profile.proctorNotes.first()
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
                            color = DarkSurfaceHigh.copy(alpha = 0.6f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Notes,
                                            contentDescription = null,
                                            tint = StatusSafe,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Observation & Meeting Record",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StatusSafe
                                        )
                                    }
                                    Text(
                                        text = latestNote.date,
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }

                                Text(
                                    text = "\"${latestNote.note}\"",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontStyle = FontStyle.Italic,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    ),
                                    color = TextPrimary
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Recorded by ${latestNote.proctor}",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )

                                    if (profile.proctorNotes.size > 1) {
                                        Text(
                                            text = "+${profile.proctorNotes.size - 1} earlier record",
                                            fontSize = 10.sp,
                                            color = AccentCyan,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.clickable { showProctorModal = true }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Action link to view dialog or open portal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showProctorModal = true },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "View All Observation Notes",
                                color = AccentCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        TextButton(
                            onClick = onOpenPortal,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Parents Portal ☰",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Portal Shortcuts
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Quick Portals & Actions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionTile(
                        icon = Icons.Default.Language,
                        title = "Parents Portal",
                        subtitle = "Contineo Login",
                        color = MsritCrimson,
                        onClick = onOpenPortal,
                        modifier = Modifier.weight(1f)
                    )

                    ActionTile(
                        icon = Icons.Default.SupervisorAccount,
                        title = "Proctorship",
                        subtitle = profile.proctorName.take(14),
                        color = AccentCyan,
                        onClick = { showProctorModal = true },
                        modifier = Modifier.weight(1f)
                    )

                    ActionTile(
                        icon = Icons.Default.AssignmentTurnedIn,
                        title = "e-Results",
                        subtitle = "exam.msrit.edu",
                        color = MsritNavy,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://exam.msrit.edu/"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Live Notice Board & Circulars
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Notice Board & Circulars",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )

                    Text(
                        text = "Live Sync",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StatusSafe
                    )
                }

                circulars.forEach { circular ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(circular.linkUrl))
                                context.startActivity(intent)
                            },
                        color = DarkSurface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (circular.isUrgent) StatusCriticalBg else DarkSurfaceHigh),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (circular.isPdf) Icons.Default.PictureAsPdf else Icons.Default.Campaign,
                                    contentDescription = null,
                                    tint = if (circular.isUrgent) StatusCritical else AccentCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = circular.title,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    ),
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = circular.category,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (circular.isUrgent) StatusCritical else AccentCyan
                                    )
                                    Text(
                                        text = "•",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = circular.date,
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showProctorModal) {
        ProctorDetailsDialog(
            profile = profile,
            onDismiss = { showProctorModal = false },
            onOpenPortal = onOpenPortal
        )
    }
}

@Composable
private fun ActionTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = DarkSurface
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun ProctorDetailsDialog(
    profile: StudentProfile,
    onDismiss: () -> Unit,
    onOpenPortal: () -> Unit
) {
    val context = LocalContext.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(24.dp)),
            color = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AccentCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SupervisorAccount,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Proctorship Record",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "MSRIT Student Mentoring System",
                                style = MaterialTheme.typography.labelSmall,
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
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Divider(color = DarkBorder)

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Student Info Card
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp)),
                            color = DarkSurfaceHigh
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "STUDENT PROFILE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = profile.name,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "USN: ${profile.usn} • 1st Sem • Sec ${profile.section}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AccentCyan
                                )
                                Text(
                                    text = profile.department,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // Assigned Mentor Card
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
                            color = DarkSurfaceHighest
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "ASSIGNED PROCTOR / MENTOR",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted,
                                        letterSpacing = 1.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(StatusSafeBg)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Active",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StatusSafe
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(MsritNavyDark, AccentPurple)
                                                )
                                            )
                                            .border(1.5.dp, AccentCyan, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = profile.proctorName.split(" ")
                                                .filter { it.isNotBlank() }
                                                .take(2)
                                                .map { it.first() }
                                                .joinToString(""),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = profile.proctorName,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Text(
                                            text = "${profile.proctorRole} • ${profile.proctorCabin}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AccentCyan
                                        )
                                    }
                                }

                                Divider(color = DarkBorder.copy(alpha = 0.5f))

                                // Contact Actions
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                                data = Uri.parse("mailto:${profile.proctorEmail}")
                                                putExtra(Intent.EXTRA_SUBJECT, "MSRIT Student Query - USN ${profile.usn}")
                                            }
                                            try { context.startActivity(intent) } catch (e: Exception) {}
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceHigh),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = null,
                                            tint = AccentCyan,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Email", fontSize = 11.sp, color = TextPrimary)
                                    }

                                    Button(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                                data = Uri.parse("tel:${profile.proctorPhone}")
                                            }
                                            try { context.startActivity(intent) } catch (e: Exception) {}
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceHigh),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Phone,
                                            contentDescription = null,
                                            tint = StatusSafe,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Call", fontSize = 11.sp, color = TextPrimary)
                                    }
                                }
                            }
                        }
                    }

                    // Observation Notes Section
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "PROCTORIAL OBSERVATIONS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "${profile.proctorNotes.size} Records Found",
                                    fontSize = 10.sp,
                                    color = AccentCyan
                                )
                            }

                            if (profile.proctorNotes.isEmpty()) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp)),
                                    color = DarkSurfaceHigh
                                ) {
                                    Text(
                                        text = "No observation notes logged yet for this semester.",
                                        fontSize = 12.sp,
                                        color = TextMuted,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            } else {
                                profile.proctorNotes.forEach { note ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp)),
                                        color = DarkSurfaceHigh
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = note.date,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccentCyan
                                                )
                                                Text(
                                                    text = "by ${note.proctor}",
                                                    fontSize = 10.sp,
                                                    color = TextMuted
                                                )
                                            }
                                            Text(
                                                text = note.note,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontSize = 12.sp,
                                                    lineHeight = 16.sp
                                                ),
                                                color = TextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Footer button to open webview
                Button(
                    onClick = {
                        onDismiss()
                        onOpenPortal()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MsritCrimson),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open MSRIT Portal Proctorship (☰ Menu)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
