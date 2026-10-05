package ir.tolooesalamat.app.mapper

import ir.tolooesalamat.app.domain.Specialty
import ir.tolooesalamat.app.dto.SpecialtyDto
import ir.tolooesalamat.app.dto.SpecialtySummaryDto
import org.springframework.stereotype.Component

@Component
class SpecialtyMapper {

    fun toDto(specialty: Specialty): SpecialtyDto = SpecialtyDto(
        id = specialty.id,
        name = specialty.name,
        code = specialty.code,
        description = specialty.description,
        active = specialty.active,
        createdAt = specialty.createdAt,
        updatedAt = specialty.updatedAt
    )

    fun toSummary(specialty: Specialty): SpecialtySummaryDto = SpecialtySummaryDto(
        id = specialty.id ?: 0L,
        name = specialty.name,
        code = specialty.code
    )

    fun toEntity(dto: SpecialtyDto): Specialty = Specialty(
        name = dto.name,
        code = dto.code,
        description = dto.description,
        active = dto.active
    )

    fun updateEntity(entity: Specialty, dto: SpecialtyDto) {
        entity.name = dto.name
        entity.code = dto.code
        entity.description = dto.description
        entity.active = dto.active
    }
}