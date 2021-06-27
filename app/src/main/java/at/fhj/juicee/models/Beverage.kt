package at.fhj.juicee.models

data class Beverage(
    val name: String = "",
    val caloriesPer100ml: Float = 0f,
    val hydrationPercentage: Float = 0f,
)