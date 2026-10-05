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

    @field:Pattern(regexp = "^$|^0[0-9]{1,2}[0-9]{7,11}$")
    val landline: String? = null,

    val password: String? = null,

    @field:NotBlank(message = "نام الزامی است")
    @field:Size(min = 2, max = 50)
    val firstName: String = "",

    @field:NotBlank(message = "نام خانوادگی الزامی است")
    @field:Size(min = 2, max = 50)
    val lastName: String = "",

    @field:Min(1) @field:Max(120)
    val age: Int? = null,

    @field:NotNull(message = "جنسیت الزامی است")
    val gender: Gender? = null,

    @field:Size(min = 4, max = 50)
    val username: String? = null,

    @field:Email(message = "ایمیل نامعتبر است")
    val email: String? = null,

    @field:Pattern(regexp = "^$|^[0-9]{10}$", message = "کد ملی باید ۱۰ رقم باشد")
    val nationalId: String? = null,

    @field:NotNull
    val role: Role = Role.PATIENT,

    val enabled: Boolean = true,

    // ─── فقط خواندنی ───
    val clinicId: Long? = null,
    val clinicName: String? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

data class ChangeUserStatusRequest(
    @field:NotNull
    val enabled: Boolean
)

data class ChangeUserRoleRequest(
    @field:NotNull
    val role: Role
)