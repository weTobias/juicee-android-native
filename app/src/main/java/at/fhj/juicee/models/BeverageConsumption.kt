package at.fhj.juicee.models

data class BeverageConsumption(
    val id: Int,
    val beverage: Beverage,
    var consumptionInMl: Int? = null
)