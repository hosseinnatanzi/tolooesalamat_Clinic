package ir.tolooesalamat.app.dto

import jakarta.validation.constraints.*
import java.time.LocalDateTime

data class DoctorProfileDto(
    val id: Long? = null,
    val userId: Long? = null,

    @field:NotNull(message = "تخصص الزامی است")
    val specialtyId: Long? = null,

    @field:NotBlank(message = "کد نظام پزشکی الزامی است")
    @field:Size(min = 4, max = 20)
    val medicalCode: String = "",

    @field:Size(max = 2000)
    val bio: String? = null,

    @field:Min(0) @field:Max(70)
    val yearsOfExperience: Int = 0,

    @field:Min(0)
    val visitFee: Long = 0,

    @field:Min(15) @field:Max(120)
    val defaultSessionDuration: Int = 45,

    @field:Min(1) @field:Max(50)
    val maxDailyAppointments: Int = 20,

    val userFullName: String? = null,
    val userPhone: String? = null,
    val specialtyName: String? = null,
    val createdAt: LocalDateTime? = null
)

data class DoctorSummaryDto(
    val userId: Long,
    val fullName: String,
    val specialty: String,
    val medicalCode: String,
    val visitFee: Long,
    val sessionDuration: Int
)