package at.fhj.juicee.models

data class BeverageConsumption(
    val beverage: Beverage,
    var consumptionInMl: Int? = null
)