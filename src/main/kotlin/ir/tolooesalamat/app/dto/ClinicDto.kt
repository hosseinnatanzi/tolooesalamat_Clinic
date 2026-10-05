package ir.tolooesalamat.app.dto

import jakarta.validation.constraints.*
import java.time.LocalDateTime

data class ClinicDto(
    val id: Long? = null,

    @field:NotBlank(message = "نام مطب الزامی است")
    @field:Size(min = 2, max = 100)
    val name: String = "",

    @field:Size(max = 1000)
    val address: String? = null,

    @field:Pattern(regexp = "^$|^09[0-9]{9}$")
    val mobile: String? = null,

    @field:Pattern(regexp = "^$|^0[0-9]{1,2}[0-9]{7,11}$")
    val landline: String? = null,

    @field:Email
    val email: String? = null,

    val active: Boolean = true,

    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

data class ClinicSummaryDto(
    val id: Long,
    val name: String,
    val active: Boolean
)