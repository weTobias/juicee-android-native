package at.fhj.juicee.models

data class DailyBeverageConsumption(
    val consumptions: MutableList<BeverageConsumption> = mutableListOf()
)