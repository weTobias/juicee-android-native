package at.fhj.juicee.models

data class BeverageConsumption(
    val beverage: Beverage = Beverage(),
    var consumptionInMl: Int = 0
)