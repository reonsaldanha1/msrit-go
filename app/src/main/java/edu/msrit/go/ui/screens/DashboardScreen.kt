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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                                        text = "${profile.semester}th Sem CSE",
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
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Proctor: ${profile.proctorName.take(18)}...",
                                    fontSize = 11.sp,
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

                    ActionTile(
                        icon = Icons.Default.Class,
                        title = "Open Elective",
                        subtitle = "5th Sem Reg",
                        color = AccentPurple,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://msrit-oe.contineo.in:5055/index.php"))
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
