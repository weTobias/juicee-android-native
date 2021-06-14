package at.fhj.juicee.models

import com.google.firebase.Timestamp

data class DailyBeverageConsumption(
    val id: Int,
    val userId: String,
    val timestamp: Timestamp,
    val consumptions: List<BeverageConsumption> = mutableListOf()
)