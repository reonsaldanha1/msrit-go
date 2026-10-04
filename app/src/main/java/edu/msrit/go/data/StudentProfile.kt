package edu.msrit.go.data

data class StudentProfile(
    val usn: String,
    val name: String,
    val department: String,
    val semester: Int,
    val section: String,
    val cycle: String = "Higher Semester (UG)",
    val academicYear: String = "2026 - 2027",
    val proctorName: String,
    val proctorEmail: String,
    val proctorCabin: String = "CSE Dept - Room 314, Apex Block",
    val cgpa: Double = 8.86,
    val sgpa: Double = 9.12
)
