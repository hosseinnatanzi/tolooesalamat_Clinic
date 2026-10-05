package ir.tolooesalamat.app.mapper

import ir.tolooesalamat.app.domain.Clinic
import ir.tolooesalamat.app.dto.ClinicDto
import ir.tolooesalamat.app.dto.ClinicSummaryDto
import org.springframework.stereotype.Component

@Component
class ClinicMapper {

    fun toDto(clinic: Clinic): ClinicDto = ClinicDto(
        id = clinic.id,
        name = clinic.name,
        address = clinic.address,
        mobile = clinic.mobile,
        landline = clinic.landline,
        email = clinic.email,
        active = clinic.active,
        createdAt = clinic.createdAt,
        updatedAt = clinic.updatedAt
    )

    fun toSummary(clinic: Clinic): ClinicSummaryDto = ClinicSummaryDto(
        id = clinic.id ?: 0L,
        name = clinic.name,
        active = clinic.active
    )

    fun toEntity(dto: ClinicDto): Clinic = Clinic(
        name = dto.name,
        address = dto.address,
        mobile = dto.mobile,
        landline = dto.landline,
        email = dto.email,
        active = dto.active
    )

    fun updateEntity(entity: Clinic, dto: ClinicDto) {
        entity.name = dto.name
        entity.address = dto.address
        entity.mobile = dto.mobile
        entity.landline = dto.landline
        entity.email = dto.email
        entity.active = dto.active
    }
}