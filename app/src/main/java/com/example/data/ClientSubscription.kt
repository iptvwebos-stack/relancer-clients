package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "client_subscriptions")
data class ClientSubscription(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val login: String,
    val password: String,
    val remainingTimeRaw: String,
    val daysRaw: String,
    val status: String,
    val expirationDaysRaw: String,
    val phoneNumber: String = "",
    val isContacted: Boolean = false,
    val noPhoneFound: Boolean = false,
    val contactDate: Long? = null,
    val contactNotes: String = "",
    val hasNotified10Days: Boolean = false
) {
    fun isExpiringSoon(): Boolean {
        val days = getRemainingDays()
        return days in 0..10 && !status.equals("Expired", ignoreCase = true) && !remainingTimeRaw.equals("Expired", ignoreCase = true)
    }

    fun getRemainingDays(): Int {
        if (status.equals("Expired", ignoreCase = true) || remainingTimeRaw.equals("Expired", ignoreCase = true)) {
            return -999
        }
        val sdf = java.text.SimpleDateFormat("M/d/yy H:mm", java.util.Locale.US)
        return try {
            val date = sdf.parse(remainingTimeRaw) ?: return 0
            val currentTime = System.currentTimeMillis()
            val diff = date.time - currentTime
            (diff / (1000 * 60 * 60 * 24)).toInt()
        } catch (e: Exception) {
            try {
                val sdf2 = java.text.SimpleDateFormat("M/d/yy", java.util.Locale.US)
                val date = sdf2.parse(remainingTimeRaw) ?: return 0
                val diff = date.time - System.currentTimeMillis()
                (diff / (1000 * 60 * 60 * 24)).toInt()
            } catch (ex: Exception) {
                try {
                    expirationDaysRaw.toInt()
                } catch (e2: Exception) {
                    try {
                        daysRaw.replace(" days", "").trim().toInt()
                    } catch (e3: Exception) {
                        0
                    }
                }
            }
        }
    }
}
