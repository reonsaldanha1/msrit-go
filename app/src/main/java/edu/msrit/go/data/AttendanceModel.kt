package edu.msrit.go.data

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

data class AttendanceSession(
    val date: String,
    val timeOrSlot: String = "Regular Class",
    val isPresent: Boolean = true,
    val topicOrRemark: String = ""
)

data class SubjectAttendance(
    val code: String,
    val title: String,
    val attended: Int,
    val total: Int,
    val credits: Int = 4,
    val faculty: String = "Prof. Faculty",
    val type: String = "Theory", // Theory, Practical, Integrated
    val sessions: List<AttendanceSession> = emptyList()
) {
    val percentage: Double
        get() = if (total == 0) 100.0 else (attended.toDouble() / total.toDouble()) * 100.0

    val absent: Int
        get() = max(0, total - attended)

    val status: AttendanceStatus
        get() = when {
            percentage >= 85.0 -> AttendanceStatus.SAFE
            percentage >= 75.0 -> AttendanceStatus.WARNING
            else -> AttendanceStatus.CRITICAL
        }

    // Number of future classes student can skip while staying >= target
    fun canBunkClasses(targetPercentage: Double = 85.0): Int {
        if (percentage < targetPercentage) return 0
        val maxTotal = floor((attended * 100.0) / targetPercentage).toInt()
        return max(0, maxTotal - total)
    }

    // Number of consecutive upcoming classes student must attend to reach >= target
    fun classesNeededToReach(targetPercentage: Double = 85.0): Int {
        if (percentage >= targetPercentage) return 0
        val numerator = (targetPercentage * total) - (100.0 * attended)
        val denominator = 100.0 - targetPercentage
        if (denominator <= 0) return 99
        return max(0, ceil(numerator / denominator).toInt())
    }
}

enum class AttendanceStatus {
    SAFE,       // >= 85%
    WARNING,    // 75% - 85% (VTU / MSRIT condonation zone)
    CRITICAL    // < 75% (Shortage / Detained without condonation)
}
