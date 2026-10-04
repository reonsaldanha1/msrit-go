package edu.msrit.go.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import edu.msrit.go.ui.theme.*

@Composable
fun CieMarksCard(
    marks: SubjectCieMarks,
    modifier: Modifier = Modifier
) {
    val gradeColor = Color(marks.gradeColorHex)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(18.dp)),
        color = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Code Badge + Title + Score Out of 50
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
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
                                text = marks.code,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = AccentCyan
                            )
                        }

                        Text(
                            text = "${marks.credits} Credits",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = marks.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = TextPrimary
                    )
                }

                // Internal Score Badge
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "%.1f".format(marks.totalInternal),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black
                        ),
                        color = gradeColor
                    )
                    Text(
                        text = "/ ${marks.maxInternal.toInt()} Marks",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            // Test & Assessment Grid (CIE 1, CIE 2, Assignment, Quiz)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScoreChip(
                    label = "CIE 1",
                    score = marks.cie1.let { "%.1f".format(it) },
                    max = "50",
                    modifier = Modifier.weight(1f)
                )
                ScoreChip(
                    label = "CIE 2",
                    score = marks.cie2.let { "%.1f".format(it) },
                    max = "50",
                    modifier = Modifier.weight(1f)
                )
                ScoreChip(
                    label = "Assign",
                    score = "%.1f".format(marks.assignment),
                    max = "10",
                    modifier = Modifier.weight(1f)
                )
                ScoreChip(
                    label = "Quiz",
                    score = "%.1f".format(marks.quiz),
                    max = "10",
                    modifier = Modifier.weight(1f)
                )
            }

            // Grade Estimate & SEE Recommendation
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
                    Text(
                        text = "Projected Grade:",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Text(
                        text = marks.estimatedGrade,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = gradeColor
                    )
                }

                // SEE target for O grade (Internal + SEE/2 >= 90)
                val seeTarget = ((90.0 - marks.totalInternal) * 2.0).coerceIn(35.0, 100.0)
                Text(
                    text = "Need ${seeTarget.toInt()}/100 in SEE for 'O'",
                    fontSize = 11.sp,
                    color = AccentCyan
                )
            }
        }
    }
}

@Composable
private fun ScoreChip(
    label: String,
    score: String,
    max: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceHigh)
            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$score/$max",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}
