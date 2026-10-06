package ir.tolooesalamat.app.dto

import ir.tolooesalamat.app.domain.enum.TestCategory   // ← import جدید
import jakarta.validation.constraints.*

data class PsychologicalTestDto(
    val id: Long? = null,

    @field:NotBlank(message = "نام تست الزامی است")
    @field:Size(min = 2, max = 100)
    val name: String = "",

    @field:Size(max = 50)
    val code: String? = null,

    val description: String? = null,

    @field:Min(0)
    val totalQuestions: Int = 0,

    @field:Min(0)
    val estimatedMinutes: Int = 0,

    val category: TestCategory? = null,

    val active: Boolean = true
)

data class PsychologicalTestSummaryDto(
    val id: Long,
    val name: String,
    val code: String?,
    val category: String?
)