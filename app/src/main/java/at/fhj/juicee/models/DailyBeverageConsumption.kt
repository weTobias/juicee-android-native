package at.fhj.juicee.models

import com.google.firebase.Timestamp

data class DailyBeverageConsumption(
    val timestamp: Timestamp,
    val consumptions: MutableList<BeverageConsumption> = mutableListOf()
)