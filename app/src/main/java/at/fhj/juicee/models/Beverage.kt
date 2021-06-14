package at.fhj.juicee.models

data class Beverage(
    val id: Int,
    val name: String,
    val caloriesPer100ml: Float,
    val hydrationPercentage: Float,
)