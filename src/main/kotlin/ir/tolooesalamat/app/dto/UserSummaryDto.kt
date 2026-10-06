package ir.tolooesalamat.app.dto

data class UserSummaryDto(
    val id: Long,
    val phone: String,
    val landline: String? = null,
    val fullName: String,
    val firstName: String,
    val lastName: String,
    val age: Int? = null,
    val gender: String? = null,
    val genderLabel: String? = null,
    val role: String,
    val roleLabel: String
)