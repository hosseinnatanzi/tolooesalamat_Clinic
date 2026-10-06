package ir.tolooesalamat.app.dto

import ir.tolooesalamat.app.domain.enum.Gender        // ← import جدید
import jakarta.validation.constraints.*

data class PatientRegisterRequest(
    @field:NotBlank val firstName: String = "",
    @field:NotBlank val lastName: String = "",
    @field:NotNull val age: Int? = null,
    @field:NotNull val gender: Gender? = null,
    @field:NotBlank @field:Pattern(regexp = "^09[0-9]{9}$") val phone: String = "",
    val landline: String? = null,
    @field:NotBlank @field:Size(min = 8) val password: String = "",
    @field:NotBlank val confirmPassword: String = ""
)

data class LoginRequest(
    @field:NotBlank @field:Pattern(regexp = "^09[0-9]{9}$") val phone: String = "",
    @field:NotBlank val password: String = ""
)

data class RefreshTokenRequest(
    @field:NotBlank val refreshToken: String = ""
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val user: UserSummaryDto
)