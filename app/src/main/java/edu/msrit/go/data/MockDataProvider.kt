package edu.msrit.go.data

object MockDataProvider {

    fun getStudentProfile(usn: String = "1MS22CS042"): StudentProfile {
        val u = usn.uppercase()
        val dept = when {
            u.contains("CI") -> "Computer Science & Engineering (Cyber Security)"
            u.contains("IS") -> "Information Science & Engineering"
            u.contains("AI") || u.contains("AD") -> "Artificial Intelligence & Data Science"
            u.contains("EC") -> "Electronics & Communication Engineering"
            u.contains("EE") -> "Electrical & Electronics Engineering"
            u.contains("ME") -> "Mechanical Engineering"
            u.contains("CV") -> "Civil Engineering"
            u.contains("BT") -> "Biotechnology"
            else -> "Computer Science & Engineering"
        }
        val name = if (u.contains("CI")) "MSRIT Cyber Security Student" else "Aarav Sharma"
        return StudentProfile(
            usn = usn.ifEmpty { "1MS22CS042" },
            name = name,
            department = dept,
            semester = 5,
            section = "A",
            cycle = "Higher Semester (UG)",
            academicYear = "2026 - 2027",
            proctorName = "Dr. Radhika K. (Dept Mentor)",
            proctorEmail = "proctor.dept@msrit.edu",
            proctorCabin = "Apex Block - 3rd Floor, Room 314",
            cgpa = 8.92,
            sgpa = 9.15
        )
    }

    val sampleAttendanceCi = listOf(
        SubjectAttendance(
            code = "22CI51",
            title = "Cryptography and Network Security",
            attended = 39,
            total = 42,
            credits = 4,
            faculty = "Dr. Shobha K.",
            type = "Theory"
        ),
        SubjectAttendance(
            code = "22CI52",
            title = "Computer Networks",
            attended = 36,
            total = 40,
            credits = 4,
            faculty = "Prof. Manoj Kumar",
            type = "Theory"
        ),
        SubjectAttendance(
            code = "22CI53",
            title = "Operating Systems and Virtualization",
            attended = 34,
            total = 38,
            credits = 4,
            faculty = "Dr. Pradeep N.",
            type = "Theory"
        ),
        SubjectAttendance(
            code = "22CI54",
            title = "Database Management Systems",
            attended = 35,
            total = 38,
            credits = 4,
            faculty = "Prof. Sneha D.",
            type = "Theory"
        ),
        SubjectAttendance(
            code = "22CIL56",
            title = "Network Security Laboratory",
            attended = 14,
            total = 14,
            credits = 2,
            faculty = "Dr. Shobha & Prof. Manoj",
            type = "Practical"
        ),
        SubjectAttendance(
            code = "22CIL57",
            title = "Database & OS Laboratory",
            attended = 13,
            total = 14,
            credits = 2,
            faculty = "Dr. Pradeep & Prof. Sneha",
            type = "Practical"
        ),
        SubjectAttendance(
            code = "22HSS51",
            title = "Universal Human Values & Professional Ethics",
            attended = 18,
            total = 20,
            credits = 1,
            faculty = "Prof. V. Sharma",
            type = "Theory"
        )
    )

    fun getAttendanceForUsn(usn: String = ""): List<SubjectAttendance> {
        return if (usn.uppercase().contains("CI")) sampleAttendanceCi else sampleAttendance
    }

    val sampleCieMarksCi = listOf(
        SubjectCieMarks(
            code = "22CI51",
            title = "Cryptography and Network Security",
            credits = 4,
            cie1 = 46.0,
            cie2 = 47.0,
            cie3 = 45.0,
            assignment = 9.5,
            quiz = 9.5,
            totalInternal = 47.0
        ),
        SubjectCieMarks(
            code = "22CI52",
            title = "Computer Networks",
            credits = 4,
            cie1 = 44.0,
            cie2 = 45.0,
            cie3 = 43.0,
            assignment = 9.0,
            quiz = 9.5,
            totalInternal = 45.0
        ),
        SubjectCieMarks(
            code = "22CI53",
            title = "Operating Systems and Virtualization",
            credits = 4,
            cie1 = 41.0,
            cie2 = 43.0,
            cie3 = 42.0,
            assignment = 9.0,
            quiz = 8.5,
            totalInternal = 42.0
        ),
        SubjectCieMarks(
            code = "22CI54",
            title = "Database Management Systems",
            credits = 4,
            cie1 = 43.0,
            cie2 = 44.0,
            cie3 = 42.0,
            assignment = 9.0,
            quiz = 9.0,
            totalInternal = 43.5
        ),
        SubjectCieMarks(
            code = "22CIL56",
            title = "Network Security Laboratory",
            credits = 2,
            labInternal = 49.0,
            assignment = 10.0,
            quiz = 10.0,
            totalInternal = 49.5
        ),
        SubjectCieMarks(
            code = "22CIL57",
            title = "Database & OS Laboratory",
            credits = 2,
            labInternal = 48.0,
            assignment = 9.5,
            quiz = 9.5,
            totalInternal = 48.5
        ),
        SubjectCieMarks(
            code = "22HSS51",
            title = "Universal Human Values & Professional Ethics",
            credits = 1,
            cie1 = 45.0,
            cie2 = 46.0,
            assignment = 9.0,
            quiz = 9.0,
            totalInternal = 45.5
        )
    )

    fun getCieMarksForUsn(usn: String = ""): List<SubjectCieMarks> {
        return if (usn.uppercase().contains("CI")) sampleCieMarksCi else sampleCieMarks
    }

    val sampleAttendance = listOf(
        SubjectAttendance(
            code = "CS510",
            title = "Analysis & Design of Algorithms",
            attended = 38,
            total = 42,
            credits = 4,
            faculty = "Dr. Anita S.",
            type = "Theory"
        ),
        SubjectAttendance(
            code = "CS520",
            title = "Database Management Systems",
            attended = 36,
            total = 40,
            credits = 4,
            faculty = "Prof. Ramesh Kumar",
            type = "Theory"
        ),
        SubjectAttendance(
            code = "CS530",
            title = "Computer Networks",
            attended = 32,
            total = 38,
            credits = 4,
            faculty = "Dr. Pradeep N.",
            type = "Theory"
        ),
        SubjectAttendance(
            code = "CS540",
            title = "Artificial Intelligence & ML",
            attended = 27,
            total = 36,
            credits = 4,
            faculty = "Prof. Sneha Deshmukh",
            type = "Theory"
        ),
        SubjectAttendance(
            code = "CS550",
            title = "Cloud Computing & Virtualization",
            attended = 35,
            total = 38,
            credits = 3,
            faculty = "Dr. Karthik M.",
            type = "Theory"
        ),
        SubjectAttendance(
            code = "CSL56",
            title = "DBMS & Networks Laboratory",
            attended = 14,
            total = 14,
            credits = 2,
            faculty = "Prof. Ramesh & Dr. Pradeep",
            type = "Practical"
        ),
        SubjectAttendance(
            code = "CS570",
            title = "Constitution of India & Ethics",
            attended = 18,
            total = 20,
            credits = 1,
            faculty = "Prof. V. Sharma",
            type = "Theory"
        )
    )

    val sampleCieMarks = listOf(
        SubjectCieMarks(
            code = "CS510",
            title = "Analysis & Design of Algorithms",
            credits = 4,
            cie1 = 46.0,
            cie2 = 47.0,
            cie3 = 45.0,
            assignment = 9.5,
            quiz = 9.5,
            totalInternal = 47.0
        ),
        SubjectCieMarks(
            code = "CS520",
            title = "Database Management Systems",
            credits = 4,
            cie1 = 44.0,
            cie2 = 46.0,
            cie3 = 43.0,
            assignment = 9.0,
            quiz = 9.5,
            totalInternal = 45.0
        ),
        SubjectCieMarks(
            code = "CS530",
            title = "Computer Networks",
            credits = 4,
            cie1 = 40.0,
            cie2 = 43.0,
            cie3 = 42.0,
            assignment = 9.0,
            quiz = 8.5,
            totalInternal = 41.5
        ),
        SubjectCieMarks(
            code = "CS540",
            title = "Artificial Intelligence & ML",
            credits = 4,
            cie1 = 38.0,
            cie2 = 41.0,
            cie3 = 39.0,
            assignment = 8.5,
            quiz = 8.0,
            totalInternal = 39.0
        ),
        SubjectCieMarks(
            code = "CS550",
            title = "Cloud Computing & Virtualization",
            credits = 3,
            cie1 = 48.0,
            cie2 = 49.0,
            cie3 = 47.0,
            assignment = 10.0,
            quiz = 9.5,
            totalInternal = 48.5
        ),
        SubjectCieMarks(
            code = "CSL56",
            title = "DBMS & Networks Laboratory",
            credits = 2,
            labInternal = 49.0,
            assignment = 10.0,
            quiz = 10.0,
            totalInternal = 49.5
        ),
        SubjectCieMarks(
            code = "CS570",
            title = "Constitution of India & Ethics",
            credits = 1,
            cie1 = 45.0,
            cie2 = 46.0,
            assignment = 9.0,
            quiz = 9.0,
            totalInternal = 45.5
        )
    )

    val sampleCirculars = listOf(
        CircularItem(
            id = "c1",
            title = "Registration / commencement of ODD semester (UG): 24-08-2026. Minimum 85% attendance is mandatory for CIEs and SEE.",
            date = "Active Circular",
            category = "Important",
            linkUrl = "https://parents.msrit.edu/newparents/index.php",
            isUrgent = true
        ),
        CircularItem(
            id = "c2",
            title = "Regular results of Bachelor of Engineering Semester 6th announced on e-results portal",
            date = "June 2026",
            category = "Exam",
            linkUrl = "https://exam.msrit.edu/"
        ),
        CircularItem(
            id = "c3",
            title = "Circular – Students: Mandatory Registration of Courses",
            date = "Academic",
            category = "Circular",
            linkUrl = "https://parents.msrit.edu/newparents/templates/contineoReg/pdf/Student-Reg-Circular.pdf",
            isPdf = true
        ),
        CircularItem(
            id = "c4",
            title = "Circular – Parents: Importance of Timely Course Registration",
            date = "Academic",
            category = "Circular",
            linkUrl = "https://parents.msrit.edu/newparents/templates/contineoReg/pdf/Parents-Circular.pdf",
            isPdf = true
        ),
        CircularItem(
            id = "c5",
            title = "Open Elective Registration for 5th semester on Contineo Portal",
            date = "Semester 5",
            category = "Elective",
            linkUrl = "https://msrit-oe.contineo.in:5055/index.php"
        )
    )

    val campusServices = listOf(
        CampusService(
            id = "s1",
            title = "Parents & Student Contineo Portal",
            subtitle = "Official portal for academic records, timetable, and attendance",
            category = "Portal",
            iconName = "School",
            actionUrl = "https://parents.msrit.edu/newparents/index.php"
        ),
        CampusService(
            id = "s2",
            title = "MSRIT Exam & e-Results Portal",
            subtitle = "Semester End Examination (SEE) results, grade cards, and revaluation",
            category = "Portal",
            iconName = "Assessment",
            actionUrl = "https://exam.msrit.edu/"
        ),
        CampusService(
            id = "s3",
            title = "Open Elective Registration",
            subtitle = "Course allocation portal for 5th and 7th semester electives",
            category = "Portal",
            iconName = "Class",
            actionUrl = "https://msrit-oe.contineo.in:5055/index.php"
        ),
        CampusService(
            id = "s4",
            title = "Campus Wi-Fi & IT Center",
            subtitle = "High-speed campus network support, MAC registration & troubleshooting",
            category = "IT & Wi-Fi",
            iconName = "Wifi",
            actionUrl = "https://www.msrit.edu",
            contactEmail = "noc@msrit.edu",
            contactPhone = "080-23600822 Ext: 250"
        ),
        CampusService(
            id = "s5",
            title = "Proctorial System & Student Counseling",
            subtitle = "Dedicated faculty mentorship and academic grievance redressal",
            category = "Academic",
            iconName = "Psychology",
            actionUrl = "https://www.msrit.edu",
            contactEmail = "proctorial@msrit.edu"
        ),
        CampusService(
            id = "s6",
            title = "IEEE MSRIT Student Branch",
            subtitle = "One of the most active technical student chapters in Karnataka",
            category = "Club",
            iconName = "Code",
            actionUrl = "https://ieee.msrit.edu"
        ),
        CampusService(
            id = "s7",
            title = "Team Chimera - Formula Student",
            subtitle = "Premier formula student engineering team representing Ramaiah",
            category = "Club",
            iconName = "Speed",
            actionUrl = "https://teamchimera.org"
        ),
        CampusService(
            id = "s8",
            title = "Google Developer Student Club (GDSC)",
            subtitle = "Community for learning Android, Cloud, AI, and Web technologies",
            category = "Club",
            iconName = "Group",
            actionUrl = "https://gdsc.community.dev/ramaiah-institute-of-technology/"
        )
    )
}
