package edu.msrit.go.data

data class SubjectCieMarks(
    val code: String,
    val title: String,
    val credits: Int = 4,
    val cie1: Double = 42.0,      // Max 50
    val cie2: Double = 45.0,      // Max 50
    val cie3: Double? = null,     // Max 50 (optional/upcoming)
    val assignment: Double = 9.5, // Max 10
    val quiz: Double = 9.0,       // Max 10
    val labInternal: Double? = null, // Max 50 for lab courses
    val totalInternal: Double = 44.5, // Normalized out of 50
    val maxInternal: Double = 50.0
) {
    val percentage: Double
        get() = (totalInternal / maxInternal) * 100.0

    val estimatedGrade: String
        get() = when {
            percentage >= 90.0 -> "O (Outstanding)"
            percentage >= 80.0 -> "A+ (Excellent)"
            percentage >= 70.0 -> "A (Very Good)"
            percentage >= 60.0 -> "B+ (Good)"
            percentage >= 50.0 -> "B (Above Average)"
            percentage >= 40.0 -> "C (Pass)"
            else -> "F (Fail)"
        }

    val gradeColorHex: Long
        get() = when {
            percentage >= 90.0 -> 0xFF10B981 // Emerald
            percentage >= 80.0 -> 0xFF38BDF8 // Sky
            percentage >= 70.0 -> 0xFFF59E0B // Amber
            else -> 0xFFEF4444 // Red
        }
}
