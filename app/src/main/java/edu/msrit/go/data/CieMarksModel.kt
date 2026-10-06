package edu.msrit.go.data

data class SubjectCieMarks(
    val code: String,
    val title: String,
    val credits: Int = 4,
    val cie1: Double? = null,        // Max 50 (null if not yet conducted/entered)
    val cie2: Double? = null,        // Max 50 (null if not yet conducted/entered)
    val cie3: Double? = null,        // Max 50 (optional/upcoming)
    val assignment: Double? = null,  // Max 10/20 (null if not yet entered)
    val quiz: Double? = null,        // Max 10/20 (null if not yet entered)
    val labInternal: Double? = null, // Max 50 for lab courses
    val totalInternal: Double = 0.0, // Normalized total out of 50
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
