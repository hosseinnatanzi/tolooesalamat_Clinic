package ir.tolooesalamat.app.dto

import ir.tolooesalamat.app.domain.Gender
import ir.tolooesalamat.app.domain.Role
import jakarta.validation.constraints.*
import java.time.LocalDateTime

data class UserDto(
    val id: Long? = null,

    @field:NotBlank(message = "شماره موبایل الزامی است")
    @field:Pattern(regexp = "^09[0-9]{9}$", message = "شماره موبایل نامعتبر است")
    val phone: String = "",

    val landline: String? = null,
    val password: String? = null,

    @field:NotBlank(message = "نام الزامی است")
    val firstName: String = "",

    @field:NotBlank(message = "نام خانوادگی الزامی است")
    val lastName: String = "",

    val age: Int? = null,
    val gender: Gender? = null,
    val username: String? = null,
    val email: String? = null,
    val nationalId: String? = null,
    val role: Role = Role.PATIENT,
    val enabled: Boolean = true,

    val clinicId: Long? = null,
    val clinicName: String? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

data class ChangeUserRoleRequest(
    @field:NotNull
    val role: Role
)

data class ChangeUserStatusRequest(
    @field:NotNull
    val enabled: Boolean
)