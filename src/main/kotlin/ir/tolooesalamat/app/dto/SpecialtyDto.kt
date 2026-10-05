package ir.tolooesalamat.app.dto

import jakarta.validation.constraints.*
import java.time.LocalDateTime

data class SpecialtyDto(
    val id: Long? = null,

    @field:NotBlank(message = "نام تخصص الزامی است")
    @field:Size(min = 2, max = 100)
    val name: String = "",

    @field:Size(max = 50)
    val code: String? = null,

    @field:Size(max = 2000)
    val description: String? = null,

    val active: Boolean = true,

    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

data class SpecialtySummaryDto(
    val id: Long,
    val name: String,
    val code: String?
)