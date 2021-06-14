package at.fhj.juicee.models

data class UserInformation(
    var height: Float? = null,
    var weight: Float? = null,
    var age: Int? = null,
    var activityLevel: Int? = null,
    var gender: Gender? = null,
)