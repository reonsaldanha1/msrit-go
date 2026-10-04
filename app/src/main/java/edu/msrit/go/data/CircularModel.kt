package edu.msrit.go.data

data class CircularItem(
    val id: String,
    val title: String,
    val date: String,
    val category: String, // "Exam", "Academic", "Circular", "Important"
    val linkUrl: String,
    val isPdf: Boolean = false,
    val isUrgent: Boolean = false
)
