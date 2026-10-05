package ir.tolooesalamat.app.dto

import ir.tolooesalamat.app.domain.Gender
import jakarta.validation.constraints.*

// ═══════════════════════════════════════════════════════════
// 📝 درخواست‌های ورودی
// ═══════════════════════════════════════════════════════════

data class PatientRegisterRequest(
    @field:NotBlank(message = "نام الزامی است")
    @field:Size(min = 2, max = 50)
    val firstName: String = "",

    @field:NotBlank(message = "نام خانوادگی الزامی است")
    @field:Size(min = 2, max = 50)
    val lastName: String = "",

    @field:NotNull(message = "سن الزامی است")
    @field:Min(value = 1, message = "سن باید حداقل ۱ سال باشد")
    @field:Max(value = 120, message = "سن باید حداکثر ۱۲۰ سال باشد")
    val age: Int? = null,

    @field:NotNull(message = "جنسیت الزامی است")
    val gender: Gender? = null,

    @field:NotBlank(message = "شماره موبایل الزامی است")
    @field:Pattern(regexp = "^09[0-9]{9}$", message = "شماره موبایل نامعتبر است")
    val phone: String = "",

    @field:Pattern(
        regexp = "^$|^0[0-9]{1,2}[0-9]{7,11}$",
        message = "شماره تلفن ثابت نامعتبر است"
    )
    val landline: String? = null,

    @field:NotBlank(message = "رمز عبور الزامی است")
    @field:Size(min = 8, max = 100)
    @field:Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
        message = "رمز عبور باید شامل حرف و عدد باشد"
    )
    val password: String = "",

    @field:NotBlank(message = "تکرار رمز عبور الزامی است")
    val confirmPassword: String = ""
)

data class LoginRequest(
    @field:NotBlank(message = "شماره موبایل الزامی است")
    @field:Pattern(regexp = "^09[0-9]{9}$", message = "شماره موبایل نامعتبر است")
    val phone: String = "",

    @field:NotBlank(message = "رمز عبور الزامی است")
    val password: String = ""
)

data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh Token الزامی است")
    val refreshToken: String = ""
)

data class ChangePasswordRequest(
    @field:NotBlank(message = "رمز فعلی الزامی است")
    val currentPassword: String = "",

    @field:NotBlank(message = "رمز جدید الزامی است")
    @field:Size(min = 8, max = 100)
    @field:Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$")
    val newPassword: String = "",

    @field:NotBlank(message = "تکرار رمز جدید الزامی است")
    val confirmNewPassword: String = ""
)

// ═══════════════════════════════════════════════════════════
// 📤 پاسخ‌های خروجی
// ═══════════════════════════════════════════════════════════

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val user: UserSummaryDto
)

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