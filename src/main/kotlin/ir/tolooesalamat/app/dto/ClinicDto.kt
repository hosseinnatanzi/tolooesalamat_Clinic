package ir.tolooesalamat.app.dto

import jakarta.validation.constraints.*
import java.time.LocalDateTime

/**
 * DTO کامل مطب.
 */
data class ClinicDto(
    val id: Long? = null,

    @field:NotBlank(message = "نام مطب الزامی است")
    @field:Size(min = 2, max = 100)
    val name: String = "",

    @field:Size(max = 2000)
    val address: String? = null,

    val mobile: String? = null,
    val landline: String? = null,
    val email: String? = null,

    val active: Boolean = true,

    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

/**
 * DTO خلاصه مطب (برای dropdown).
 */
data class ClinicSummaryDto(
    val id: Long,
    val name: String,
    val active: Boolean
)