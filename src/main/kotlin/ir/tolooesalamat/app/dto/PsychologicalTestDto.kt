package ir.tolooesalamat.app.dto

import ir.tolooesalamat.app.domain.TestCategory
import jakarta.validation.constraints.*
import java.time.LocalDateTime

data class PsychologicalTestDto(
    val id: Long? = null,

    @field:NotBlank(message = "نام تست الزامی است")
    @field:Size(min = 2, max = 100)
    val name: String = "",

    @field:Size(max = 50)
    val code: String? = null,

    @field:Size(max = 2000)
    val description: String? = null,

    @field:Min(0) @field:Max(1000)
    val totalQuestions: Int = 0,

    @field:Min(0) @field:Max(300)
    val estimatedMinutes: Int = 0,

    val category: TestCategory? = null,
    val active: Boolean = true,

    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

data class PsychologicalTestSummaryDto(
    val id: Long,
    val name: String,
    val code: String?,
    val category: String?
)