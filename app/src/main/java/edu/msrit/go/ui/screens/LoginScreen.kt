package edu.msrit.go.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.msrit.go.data.UserPreferences
import edu.msrit.go.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    userPreferences: UserPreferences,
    onLoginWithPortal: (usn: String, day: String, month: String, year: String, rememberMe: Boolean) -> Unit,
    onExploreDemoMode: () -> Unit
) {
    var usn by remember { mutableStateOf(userPreferences.savedUsn) }
    var day by remember { mutableStateOf(userPreferences.savedDobDay) }
    var month by remember { mutableStateOf(userPreferences.savedDobMonth) }
    var year by remember { mutableStateOf(userPreferences.savedDobYear) }
    var rememberMe by remember { mutableStateOf(userPreferences.rememberMe) }
    var validationError by remember { mutableStateOf<String?>(null) }

    // Dropdown expansion states
    var dayExpanded by remember { mutableStateOf(false) }
    var monthExpanded by remember { mutableStateOf(false) }
    var yearExpanded by remember { mutableStateOf(false) }

    val daysList = remember { (1..31).map { it.toString().padStart(2, '0') } }
    val monthsList = remember {
        listOf(
            "01" to "Jan (01)",
            "02" to "Feb (02)",
            "03" to "Mar (03)",
            "04" to "Apr (04)",
            "05" to "May (05)",
            "06" to "Jun (06)",
            "07" to "Jul (07)",
            "08" to "Aug (08)",
            "09" to "Sep (09)",
            "10" to "Oct (10)",
            "11" to "Nov (11)",
            "12" to "Dec (12)"
        )
    }
    val yearsList = remember { (2009 downTo 1985).map { it.toString() } }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Decorative background glows
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-80).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(MsritCrimsonDark.copy(alpha = 0.35f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-100).dp, y = 80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(MsritNavyDark.copy(alpha = 0.5f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Brand Crest Header
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(MsritCrimson, MsritCrimsonDark)
                        )
                    )
                    .border(2.dp, MsritCrimsonLight.copy(alpha = 0.6f), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "MSRIT Crest",
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "MSRIT",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MsritCrimson)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "GO",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }

                Text(
                    text = "Ramaiah Institute of Technology",
                    fontSize = 13.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
            }

            // Portal Card Container
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
                        .padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Card Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Student Portal Login",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = "Sign in with parents.msrit.edu credentials",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurfaceHighest)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Contineo SIS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentCyan
                            )
                        }
                    }

                    HorizontalDivider(color = DarkBorder)

                    // USN Field
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "University Seat Number (USN)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                            Text(
                                text = "Format: 1MS22CS042",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        OutlinedTextField(
                            value = usn,
                            onValueChange = {
                                usn = it.uppercase().trim()
                                validationError = null
                            },
                            placeholder = { Text("e.g. 1MS22CS042", color = TextMuted) },
                            leadingIcon = {
                                Icon(Icons.Default.Badge, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                            },
                            trailingIcon = {
                                if (usn.isNotEmpty()) {
                                    IconButton(onClick = { usn = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentCyan,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = DarkSurfaceHigh,
                                unfocusedContainerColor = DarkSurfaceHigh
                            ),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )
                    }

                    // Date of Birth (Contineo Password Form)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Date of Birth (Portal Password)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                            Text(
                                text = "DD / MM / YYYY",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Day Dropdown
                            ExposedDropdownMenuBox(
                                expanded = dayExpanded,
                                onExpandedChange = { dayExpanded = it },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = day.ifEmpty { "Day" },
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded) },
                                    modifier = Modifier.menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentCyan,
                                        unfocusedBorderColor = DarkBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = if (day.isEmpty()) TextMuted else TextPrimary,
                                        focusedContainerColor = DarkSurfaceHigh,
                                        unfocusedContainerColor = DarkSurfaceHigh
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = dayExpanded,
                                    onDismissRequest = { dayExpanded = false },
                                    modifier = Modifier.background(DarkSurfaceHigh)
                                ) {
                                    daysList.forEach { d ->
                                        DropdownMenuItem(
                                            text = { Text(d, color = TextPrimary) },
                                            onClick = {
                                                day = d
                                                dayExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Month Dropdown
                            ExposedDropdownMenuBox(
                                expanded = monthExpanded,
                                onExpandedChange = { monthExpanded = it },
                                modifier = Modifier.weight(1.2f)
                            ) {
                                val monthDisplay = monthsList.find { it.first == month }?.second ?: "Month"
                                OutlinedTextField(
                                    value = if (month.isEmpty()) "Month" else monthDisplay,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthExpanded) },
                                    modifier = Modifier.menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentCyan,
                                        unfocusedBorderColor = DarkBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = if (month.isEmpty()) TextMuted else TextPrimary,
                                        focusedContainerColor = DarkSurfaceHigh,
                                        unfocusedContainerColor = DarkSurfaceHigh
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = monthExpanded,
                                    onDismissRequest = { monthExpanded = false },
                                    modifier = Modifier.background(DarkSurfaceHigh)
                                ) {
                                    monthsList.forEach { (mVal, mLabel) ->
                                        DropdownMenuItem(
                                            text = { Text(mLabel, color = TextPrimary) },
                                            onClick = {
                                                month = mVal
                                                monthExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Year Dropdown
                            ExposedDropdownMenuBox(
                                expanded = yearExpanded,
                                onExpandedChange = { yearExpanded = it },
                                modifier = Modifier.weight(1.3f)
                            ) {
                                OutlinedTextField(
                                    value = year.ifEmpty { "Year" },
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded) },
                                    modifier = Modifier.menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentCyan,
                                        unfocusedBorderColor = DarkBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = if (year.isEmpty()) TextMuted else TextPrimary,
                                        focusedContainerColor = DarkSurfaceHigh,
                                        unfocusedContainerColor = DarkSurfaceHigh
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = yearExpanded,
                                    onDismissRequest = { yearExpanded = false },
                                    modifier = Modifier.background(DarkSurfaceHigh)
                                ) {
                                    yearsList.forEach { y ->
                                        DropdownMenuItem(
                                            text = { Text(y, color = TextPrimary) },
                                            onClick = {
                                                year = y
                                                yearExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Remember Me & Sample Filler Chip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { rememberMe = !rememberMe }
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MsritCrimson,
                                    checkmarkColor = Color.White,
                                    uncheckedColor = TextMuted
                                )
                            )
                            Text(
                                text = "Remember me",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        // Sample student auto-populate chip
                        SuggestionChip(
                            onClick = {
                                usn = "1MS22CS042"
                                day = "15"
                                month = "08"
                                year = "2004"
                                validationError = null
                            },
                            label = { Text("Use Sample", fontSize = 11.sp, color = AccentCyan) },
                            icon = { Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(14.dp)) },
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = DarkSurfaceHighest),
                            border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = DarkBorder)
                        )
                    }

                    // Validation Error Text
                    if (validationError != null) {
                        Text(
                            text = validationError ?: "",
                            color = StatusCritical,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Primary Action: Login with MSRIT Portal
                    Button(
                        onClick = {
                            if (usn.isBlank()) {
                                validationError = "Please enter your University Seat Number (USN)"
                                return@Button
                            }
                            if (day.isBlank() || month.isBlank() || year.isBlank()) {
                                validationError = "Please select your complete Date of Birth (Day, Month, Year)"
                                return@Button
                            }
                            validationError = null
                            onLoginWithPortal(usn.trim(), day.trim(), month.trim(), year.trim(), rememberMe)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MsritCrimson)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Login with MSRIT Portal",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Secondary Action: Explore in Demo Mode
                    OutlinedButton(
                        onClick = onExploreDemoMode,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(DarkBorder, AccentCyan.copy(alpha = 0.5f))))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Explore with Sample Data (Demo Mode)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Security & Privacy Trust Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    tint = StatusSafe,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Encrypted SSL Direct Connection. Credentials are authenticated directly against parents.msrit.edu and saved locally on your phone.",
                    fontSize = 11.sp,
                    color = TextMuted,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
