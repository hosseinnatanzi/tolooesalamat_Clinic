package ir.tolooesalamat.app.mapper

import ir.tolooesalamat.app.domain.PsychologicalTest
import ir.tolooesalamat.app.dto.PsychologicalTestDto
import ir.tolooesalamat.app.dto.PsychologicalTestSummaryDto
import org.springframework.stereotype.Component

@Component
class PsychologicalTestMapper {

    fun toDto(test: PsychologicalTest): PsychologicalTestDto = PsychologicalTestDto(
        id = test.id,
        name = test.name,
        code = test.code,
        description = test.description,
        totalQuestions = test.totalQuestions,
        estimatedMinutes = test.estimatedMinutes,
        category = test.category,        // ← مستقیم، بدون تبدیل
        active = test.active
    )

    fun toSummary(test: PsychologicalTest): PsychologicalTestSummaryDto =
        PsychologicalTestSummaryDto(
            id = test.id ?: 0L,
            name = test.name,
            code = test.code,
            category = test.category?.name    // ← enum → String
        )

    fun toEntity(dto: PsychologicalTestDto): PsychologicalTest = PsychologicalTest(
        name = dto.name,
        code = dto.code,
        description = dto.description,
        totalQuestions = dto.totalQuestions,
        estimatedMinutes = dto.estimatedMinutes,
        category = dto.category,        // ← مستقیم
        active = dto.active
    )

    fun updateEntity(entity: PsychologicalTest, dto: PsychologicalTestDto) {
        entity.name = dto.name
        entity.code = dto.code
        entity.description = dto.description
        entity.totalQuestions = dto.totalQuestions
        entity.estimatedMinutes = dto.estimatedMinutes
        entity.category = dto.category
        entity.active = dto.active
    }
}