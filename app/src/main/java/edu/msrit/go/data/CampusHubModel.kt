package edu.msrit.go.data

data class CampusService(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String, // "Portal", "IT & Wi-Fi", "Academic", "Club"
    val iconName: String,
    val actionUrl: String,
    val contactPhone: String? = null,
    val contactEmail: String? = null
)
