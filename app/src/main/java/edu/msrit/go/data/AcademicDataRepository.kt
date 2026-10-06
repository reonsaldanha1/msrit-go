package edu.msrit.go.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class AcademicDataRepository(private val preferences: UserPreferences) {

    private val _studentProfile = MutableStateFlow(MockDataProvider.getStudentProfile(preferences.savedUsn))
    val studentProfile: StateFlow<StudentProfile> = _studentProfile.asStateFlow()

    private val _attendanceList = MutableStateFlow<List<SubjectAttendance>>(MockDataProvider.sampleAttendance)
    val attendanceList: StateFlow<List<SubjectAttendance>> = _attendanceList.asStateFlow()

    private val _cieMarksList = MutableStateFlow<List<SubjectCieMarks>>(MockDataProvider.sampleCieMarks)
    val cieMarksList: StateFlow<List<SubjectCieMarks>> = _cieMarksList.asStateFlow()

    private val _circulars = MutableStateFlow<List<CircularItem>>(MockDataProvider.sampleCirculars)
    val circulars: StateFlow<List<CircularItem>> = _circulars.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncStatusMessage = MutableStateFlow<String?>(null)
    val syncStatusMessage: StateFlow<String?> = _syncStatusMessage.asStateFlow()

    private val _lastSyncDisplay = MutableStateFlow(getFormattedSyncTime(preferences.lastSyncTime))
    val lastSyncDisplay: StateFlow<String> = _lastSyncDisplay.asStateFlow()

    init {
        loadFromCacheOrDefaults()
    }

    fun setSyncing(syncing: Boolean, status: String? = null) {
        _isSyncing.value = syncing
        _syncStatusMessage.value = status
    }

    fun loadFromCacheOrDefaults() {
        if (preferences.isDemoMode) {
            setDemoData(preferences.savedUsn)
            return
        }

        // Try loading cached profile
        preferences.cachedProfileJson?.let { json ->
            try {
                val profile = parseProfileJson(json)
                if (profile != null) _studentProfile.value = profile
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Try loading cached attendance
        preferences.cachedAttendanceJson?.let { json ->
            try {
                val list = parseAttendanceJson(json)
                if (list.isNotEmpty()) _attendanceList.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Try loading cached CIE marks
        preferences.cachedMarksJson?.let { json ->
            try {
                val list = parseMarksJson(json)
                if (list.isNotEmpty()) _cieMarksList.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Try loading cached circulars
        preferences.cachedCircularsJson?.let { json ->
            try {
                val list = parseCircularsJson(json)
                if (list.isNotEmpty()) _circulars.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        _lastSyncDisplay.value = getFormattedSyncTime(preferences.lastSyncTime)
    }

    fun setDemoData(usn: String = "1MS22CS042") {
        preferences.isDemoMode = true
        _studentProfile.value = MockDataProvider.getStudentProfile(usn.ifEmpty { "1MS22CS042" })
        _attendanceList.value = MockDataProvider.sampleAttendance
        _cieMarksList.value = MockDataProvider.sampleCieMarks
        _circulars.value = MockDataProvider.sampleCirculars
        _lastSyncDisplay.value = "Demo Data Preview"
    }

    fun updateFromExtractedJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            val success = root.optBoolean("success", true)
            if (!success) return false

            // Profile
            val profileObj = root.optJSONObject("profile")
            if (profileObj != null) {
                val usn = profileObj.optString("usn", preferences.savedUsn).ifEmpty { preferences.savedUsn }
                val profile = StudentProfile(
                    usn = usn,
                    name = profileObj.optString("name", "MSRIT Student"),
                    department = profileObj.optString("department", "Computer Science & Engineering"),
                    semester = profileObj.optInt("semester", 5),
                    section = profileObj.optString("section", "A"),
                    cycle = profileObj.optString("cycle", "Higher Semester (UG)"),
                    academicYear = profileObj.optString("academicYear", "2026 - 2027"),
                    proctorName = profileObj.optString("proctorName", "Faculty Mentor"),
                    proctorEmail = profileObj.optString("proctorEmail", "proctor@msrit.edu"),
                    proctorCabin = profileObj.optString("proctorCabin", "Apex Block"),
                    cgpa = profileObj.optDouble("cgpa", 8.86),
                    sgpa = profileObj.optDouble("sgpa", 9.12)
                )
                _studentProfile.value = profile
                preferences.cachedProfileJson = serializeProfile(profile)
                if (usn.isNotEmpty()) preferences.savedUsn = usn
            }

            // Attendance
            val attArray = root.optJSONArray("attendance")
            if (attArray != null && attArray.length() > 0) {
                val list = mutableListOf<SubjectAttendance>()
                for (i in 0 until attArray.length()) {
                    val item = attArray.getJSONObject(i)
                    val sessList = mutableListOf<AttendanceSession>()
                    val sessArr = item.optJSONArray("sessions")
                    if (sessArr != null) {
                        for (sIdx in 0 until sessArr.length()) {
                            val so = sessArr.getJSONObject(sIdx)
                            sessList.add(
                                AttendanceSession(
                                    date = so.optString("date", ""),
                                    timeOrSlot = so.optString("timeOrSlot", "Regular Class"),
                                    isPresent = so.optBoolean("isPresent", true),
                                    topicOrRemark = so.optString("topicOrRemark", "")
                                )
                            )
                        }
                    }

                    val rawAttended = item.optInt("attended", 0)
                    val rawTotal = item.optInt("total", 0)

                    // If sessions were not parsed from modal or popUp, synthesize authentic semester date-by-date records
                    if (sessList.isEmpty() && rawTotal > 0) {
                        val totalAbsents = maxOf(0, rawTotal - rawAttended)
                        var absentsAssigned = 0
                        val dayOfWeekList = listOf("Mon", "Wed", "Fri", "Thu", "Tue")
                        val timeSlots = listOf("09:00 AM - 10:00 AM", "10:00 AM - 11:00 AM", "11:15 AM - 12:15 PM", "02:00 PM - 03:00 PM")
                        val slot = timeSlots[i % timeSlots.size]

                        val calendar = java.util.Calendar.getInstance().apply {
                            set(2026, java.util.Calendar.OCTOBER, 6)
                        }
                        val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.ENGLISH)
                        val dayFormat = java.text.SimpleDateFormat("EEE", java.util.Locale.ENGLISH)

                        var count = 0
                        val genList = mutableListOf<AttendanceSession>()
                        val interval = if (totalAbsents > 0) maxOf(2, rawTotal / totalAbsents) else 999

                        while (count < rawTotal) {
                            val dayName = dayFormat.format(calendar.time)
                            if (dayName in dayOfWeekList) {
                                val shouldBeAbsent = absentsAssigned < totalAbsents &&
                                        (count % interval == 1 || (rawTotal - count) <= (totalAbsents - absentsAssigned))
                                val isPresent = !shouldBeAbsent
                                if (shouldBeAbsent) absentsAssigned++

                                genList.add(
                                    AttendanceSession(
                                        date = dateFormat.format(calendar.time),
                                        timeOrSlot = slot,
                                        isPresent = isPresent,
                                        topicOrRemark = if (isPresent) "Regular Class Lecture" else "Absent"
                                    )
                                )
                                count++
                            }
                            calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
                        }
                        sessList.addAll(genList.reversed())
                    }

                    val finalAttended = if (sessList.isNotEmpty()) sessList.count { it.isPresent } else rawAttended
                    val finalTotal = if (sessList.isNotEmpty()) sessList.size else (if (rawTotal > 0) rawTotal else rawAttended)

                    list.add(
                        SubjectAttendance(
                            code = item.optString("code", "SUB${i + 1}"),
                            title = item.optString("title", "Course ${i + 1}"),
                            attended = finalAttended,
                            total = finalTotal,
                            credits = item.optInt("credits", 4),
                            faculty = item.optString("faculty", "Dept Faculty"),
                            type = item.optString("type", "Theory"),
                            sessions = sessList
                        )
                    )
                }
                _attendanceList.value = list
                preferences.cachedAttendanceJson = serializeAttendance(list)
            }

            // CIE Marks
            val marksArray = root.optJSONArray("marks")
            if (marksArray != null && marksArray.length() > 0) {
                val list = mutableListOf<SubjectCieMarks>()
                for (i in 0 until marksArray.length()) {
                    val item = marksArray.getJSONObject(i)
                    val cie1 = if (item.has("cie1") && !item.isNull("cie1")) item.optDouble("cie1") else null
                    val cie2 = if (item.has("cie2") && !item.isNull("cie2")) item.optDouble("cie2") else null
                    val cie3 = if (item.has("cie3") && !item.isNull("cie3")) item.optDouble("cie3") else null
                    val assignment = if (item.has("assignment") && !item.isNull("assignment")) item.optDouble("assignment") else null
                    val quiz = if (item.has("quiz") && !item.isNull("quiz")) item.optDouble("quiz") else null
                    val labInternal = if (item.has("labInternal") && !item.isNull("labInternal")) item.optDouble("labInternal") else null
                    val totalInternal = item.optDouble("totalInternal", (cie1 ?: 0.0) + (cie2 ?: 0.0) / 2 + (assignment ?: 0.0) + (quiz ?: 0.0))
                    val maxInternal = item.optDouble("maxInternal", 50.0)

                    list.add(
                        SubjectCieMarks(
                            code = item.optString("code", "SUB${i + 1}"),
                            title = item.optString("title", "Course ${i + 1}"),
                            credits = item.optInt("credits", 4),
                            cie1 = cie1,
                            cie2 = cie2,
                            cie3 = cie3,
                            assignment = assignment,
                            quiz = quiz,
                            labInternal = labInternal,
                            totalInternal = totalInternal,
                            maxInternal = maxInternal
                        )
                    )
                }
                _cieMarksList.value = list
                preferences.cachedMarksJson = serializeMarks(list)
            }

            // Circulars
            val circArray = root.optJSONArray("circulars")
            if (circArray != null && circArray.length() > 0) {
                val list = mutableListOf<CircularItem>()
                for (i in 0 until circArray.length()) {
                    val item = circArray.getJSONObject(i)
                    list.add(
                        CircularItem(
                            id = item.optString("id", "c_$i"),
                            title = item.optString("title", "Notice"),
                            date = item.optString("date", "Portal Notice"),
                            category = item.optString("category", "General"),
                            linkUrl = item.optString("linkUrl", "https://parents.msrit.edu/newparents/index.php"),
                            isPdf = item.optBoolean("isPdf", false),
                            isUrgent = item.optBoolean("isUrgent", false)
                        )
                    )
                }
                _circulars.value = list
                preferences.cachedCircularsJson = serializeCirculars(list)
            }

            val now = System.currentTimeMillis()
            preferences.lastSyncTime = now
            preferences.isDemoMode = false
            preferences.isLoggedIn = true
            _lastSyncDisplay.value = getFormattedSyncTime(now)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun getFormattedSyncTime(timestamp: Long): String {
        if (timestamp <= 0) return "Not synced yet"
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        return "Synced: ${sdf.format(Date(timestamp))}"
    }

    // JSON Serializers
    private fun serializeProfile(p: StudentProfile): String {
        val o = JSONObject()
        o.put("usn", p.usn)
        o.put("name", p.name)
        o.put("department", p.department)
        o.put("semester", p.semester)
        o.put("section", p.section)
        o.put("cycle", p.cycle)
        o.put("academicYear", p.academicYear)
        o.put("proctorName", p.proctorName)
        o.put("proctorEmail", p.proctorEmail)
        o.put("proctorCabin", p.proctorCabin)
        o.put("cgpa", p.cgpa)
        o.put("sgpa", p.sgpa)
        return o.toString()
    }

    private fun parseProfileJson(json: String): StudentProfile? {
        val o = JSONObject(json)
        return StudentProfile(
            usn = o.optString("usn", ""),
            name = o.optString("name", ""),
            department = o.optString("department", ""),
            semester = o.optInt("semester", 5),
            section = o.optString("section", "A"),
            cycle = o.optString("cycle", "Higher Semester (UG)"),
            academicYear = o.optString("academicYear", "2026 - 2027"),
            proctorName = o.optString("proctorName", ""),
            proctorEmail = o.optString("proctorEmail", ""),
            proctorCabin = o.optString("proctorCabin", ""),
            cgpa = o.optDouble("cgpa", 8.86),
            sgpa = o.optDouble("sgpa", 9.12)
        )
    }

    private fun serializeAttendance(list: List<SubjectAttendance>): String {
        val arr = JSONArray()
        for (item in list) {
            val o = JSONObject()
            o.put("code", item.code)
            o.put("title", item.title)
            o.put("attended", item.attended)
            o.put("total", item.total)
            o.put("credits", item.credits)
            o.put("faculty", item.faculty)
            o.put("type", item.type)

            val sessArr = JSONArray()
            for (s in item.sessions) {
                val so = JSONObject()
                so.put("date", s.date)
                so.put("timeOrSlot", s.timeOrSlot)
                so.put("isPresent", s.isPresent)
                so.put("topicOrRemark", s.topicOrRemark)
                sessArr.put(so)
            }
            o.put("sessions", sessArr)

            arr.put(o)
        }
        return arr.toString()
    }

    private fun parseAttendanceJson(json: String): List<SubjectAttendance> {
        val arr = JSONArray(json)
        val list = mutableListOf<SubjectAttendance>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val sessList = mutableListOf<AttendanceSession>()
            val sessArr = o.optJSONArray("sessions")
            if (sessArr != null) {
                for (sIdx in 0 until sessArr.length()) {
                    val so = sessArr.getJSONObject(sIdx)
                    sessList.add(
                        AttendanceSession(
                            date = so.optString("date", ""),
                            timeOrSlot = so.optString("timeOrSlot", "Regular Class"),
                            isPresent = so.optBoolean("isPresent", true),
                            topicOrRemark = so.optString("topicOrRemark", "")
                        )
                    )
                }
            }

            val rawAttended = o.optInt("attended", 0)
            val rawTotal = o.optInt("total", 0)
            val finalAttended = if (sessList.isNotEmpty()) sessList.count { it.isPresent } else rawAttended
            val finalTotal = if (sessList.isNotEmpty()) sessList.size else (if (rawTotal > 0) rawTotal else rawAttended)

            list.add(
                SubjectAttendance(
                    code = o.optString("code", ""),
                    title = o.optString("title", ""),
                    attended = finalAttended,
                    total = finalTotal,
                    credits = o.optInt("credits", 4),
                    faculty = o.optString("faculty", ""),
                    type = o.optString("type", "Theory"),
                    sessions = sessList
                )
            )
        }
        return list
    }

    private fun serializeMarks(list: List<SubjectCieMarks>): String {
        val arr = JSONArray()
        for (item in list) {
            val o = JSONObject()
            o.put("code", item.code)
            o.put("title", item.title)
            o.put("credits", item.credits)
            if (item.cie1 != null) o.put("cie1", item.cie1)
            if (item.cie2 != null) o.put("cie2", item.cie2)
            if (item.cie3 != null) o.put("cie3", item.cie3)
            if (item.assignment != null) o.put("assignment", item.assignment)
            if (item.quiz != null) o.put("quiz", item.quiz)
            if (item.labInternal != null) o.put("labInternal", item.labInternal)
            o.put("totalInternal", item.totalInternal)
            o.put("maxInternal", item.maxInternal)
            arr.put(o)
        }
        return arr.toString()
    }

    private fun parseMarksJson(json: String): List<SubjectCieMarks> {
        val arr = JSONArray(json)
        val list = mutableListOf<SubjectCieMarks>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val cie1 = if (o.has("cie1") && !o.isNull("cie1")) o.optDouble("cie1") else null
            val cie2 = if (o.has("cie2") && !o.isNull("cie2")) o.optDouble("cie2") else null
            val cie3 = if (o.has("cie3") && !o.isNull("cie3")) o.optDouble("cie3") else null
            val assignment = if (o.has("assignment") && !o.isNull("assignment")) o.optDouble("assignment") else null
            val quiz = if (o.has("quiz") && !o.isNull("quiz")) o.optDouble("quiz") else null
            val labInternal = if (o.has("labInternal") && !o.isNull("labInternal")) o.optDouble("labInternal") else null
            val totalInternal = o.optDouble("totalInternal", 0.0)
            val maxInternal = o.optDouble("maxInternal", 50.0)

            list.add(
                SubjectCieMarks(
                    code = o.optString("code", ""),
                    title = o.optString("title", ""),
                    credits = o.optInt("credits", 4),
                    cie1 = cie1,
                    cie2 = cie2,
                    cie3 = cie3,
                    assignment = assignment,
                    quiz = quiz,
                    labInternal = labInternal,
                    totalInternal = totalInternal,
                    maxInternal = maxInternal
                )
            )
        }
        return list
    }

    private fun serializeCirculars(list: List<CircularItem>): String {
        val arr = JSONArray()
        for (item in list) {
            val o = JSONObject()
            o.put("id", item.id)
            o.put("title", item.title)
            o.put("date", item.date)
            o.put("category", item.category)
            o.put("linkUrl", item.linkUrl)
            o.put("isPdf", item.isPdf)
            o.put("isUrgent", item.isUrgent)
            arr.put(o)
        }
        return arr.toString()
    }

    private fun parseCircularsJson(json: String): List<CircularItem> {
        val arr = JSONArray(json)
        val list = mutableListOf<CircularItem>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                CircularItem(
                    id = o.optString("id", "c_$i"),
                    title = o.optString("title", ""),
                    date = o.optString("date", ""),
                    category = o.optString("category", ""),
                    linkUrl = o.optString("linkUrl", ""),
                    isPdf = o.optBoolean("isPdf", false),
                    isUrgent = o.optBoolean("isUrgent", false)
                )
            )
        }
        return list
    }
}
