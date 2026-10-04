package edu.msrit.go.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.msrit.go.data.AttendanceStatus
import edu.msrit.go.data.SubjectAttendance
import edu.msrit.go.ui.components.AttendanceCard
import edu.msrit.go.ui.components.BunkCalculatorModal
import edu.msrit.go.ui.theme.*

@Composable
fun AttendanceScreen(
    attendanceList: List<SubjectAttendance>,
    targetAttendance: Double,
    onTargetChanged: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedSubjectForCalc by remember { mutableStateOf<SubjectAttendance?>(null) }

    val totalAttended = attendanceList.sumOf { it.attended }
    val totalClasses = attendanceList.sumOf { it.total }
    val overallPercentage = if (totalClasses == 0) 100.0 else (totalAttended.toDouble() / totalClasses) * 100.0

    val filteredList = remember(attendanceList, selectedFilter) {
        when (selectedFilter) {
            "Safe (>=85%)" -> attendanceList.filter { it.status == AttendanceStatus.SAFE }
            "Warning (75-85%)" -> attendanceList.filter { it.status == AttendanceStatus.WARNING }
            "Critical (<75%)" -> attendanceList.filter { it.status == AttendanceStatus.CRITICAL }
            else -> attendanceList
        }
    }

    Box(modifier = modifier.fillMaxSize().background(DarkBackground)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
        ) {
            // Overall Hero Summary Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(22.dp)),
                    color = DarkSurface
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Overall Attendance",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "$totalAttended attended out of $totalClasses held",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted
                                )
                            }

                            Text(
                                text = "%.1f%%".format(overallPercentage),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 34.sp
                                ),
                                color = if (overallPercentage >= targetAttendance) StatusSafe else StatusWarning
                            )
                        }

                        // Target Selector
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceHigh)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = AccentCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "MSRIT Mandate Target:",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(85.0, 80.0, 75.0).forEach { target ->
                                    val isSelected = targetAttendance == target
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) MsritCrimson else DarkSurfaceLowest)
                                            .clickable { onTargetChanged(target) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${target.toInt()}%",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf("All", "Safe (>=85%)", "Warning (75-85%)", "Critical (<75%)")
                    items(filters) { filter ->
                        val isSelected = filter == selectedFilter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) DarkSurfaceHighest else DarkSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) AccentCyan else DarkBorder,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedFilter = filter }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = filter,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else TextSecondary
                            )
                        }
                    }
                }
            }

            // Subject Cards List
            items(filteredList) { subject ->
                AttendanceCard(
                    subject = subject,
                    targetAttendance = targetAttendance,
                    onCalculateClick = { selectedSubjectForCalc = subject }
                )
            }
        }

        // Show Modal if Subject Selected
        selectedSubjectForCalc?.let { subject ->
            BunkCalculatorModal(
                subject = subject,
                onDismiss = { selectedSubjectForCalc = null }
            )
        }
    }
}
