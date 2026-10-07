package edu.msrit.go.data

data class ProctorNote(
    val date: String,
    val proctor: String,
    val note: String
)

data class StudentProfile(
    val usn: String,
    val name: String,
    val department: String,
    val semester: Int,
    val section: String,
    val cycle: String = "First Semester (UG)",
    val academicYear: String = "2026 - 2027",
    val proctorName: String,
    val proctorEmail: String,
    val proctorCabin: String = "First Year Faculty",
    val proctorPhone: String = "9901287316",
    val proctorRole: String = "First Year Faculty Mentor",
    val cgpa: Double = 8.86,
    val sgpa: Double = 9.12,
    val proctorNotes: List<ProctorNote> = emptyList()
)
