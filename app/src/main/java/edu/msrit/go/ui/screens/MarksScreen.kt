package edu.msrit.go.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.msrit.go.data.SubjectCieMarks
import edu.msrit.go.ui.components.CieMarksCard
import edu.msrit.go.ui.theme.*

@Composable
fun MarksScreen(
    cieMarksList: List<SubjectCieMarks>,
    sgpa: Double = 9.15,
    cgpa: Double = 8.92,
    modifier: Modifier = Modifier
) {
    val avgCie = if (cieMarksList.isEmpty()) 0.0 else cieMarksList.map { it.totalInternal }.average()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
    ) {
        // Hero Score & SGPA Card
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
                                text = "Internal Performance",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = "Continuous Internal Evaluation (CIE)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "%.1f".format(avgCie),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 32.sp
                                ),
                                color = AccentCyan
                            )
                            Text(
                                text = "Average / 50",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    // SGPA & CGPA Split Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ScoreStatChip(
                            icon = Icons.Default.Verified,
                            title = "Projected SGPA",
                            value = "%.2f".format(sgpa),
                            color = StatusSafe,
                            modifier = Modifier.weight(1f)
                        )
                        ScoreStatChip(
                            icon = Icons.Default.Star,
                            title = "Cumulative CGPA",
                            value = "%.2f".format(cgpa),
                            color = StatusWarning,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Passing Threshold Banner
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, StatusSafe.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        color = StatusSafeBg
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = StatusSafe,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "All subjects have cleared the minimum 20/50 CIE threshold for SEE eligibility.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StatusSafe
                            )
                        }
                    }
                }
            }
        }

        // Section Title
        item {
            Text(
                text = "Course-wise CIE Breakdown",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        }

        // Marks List
        items(cieMarksList) { marks ->
            CieMarksCard(marks = marks)
        }
    }
}

@Composable
private fun ScoreStatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceHigh)
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
        color = DarkSurfaceHigh
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Column {
                Text(text = title, fontSize = 11.sp, color = TextMuted)
                Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = TextPrimary)
            }
        }
    }
}
