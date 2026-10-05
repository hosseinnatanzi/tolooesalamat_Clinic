package ir.tolooesalamat.app.mapper

import ir.tolooesalamat.app.domain.DoctorProfile
import ir.tolooesalamat.app.dto.DoctorProfileDto
import ir.tolooesalamat.app.dto.DoctorSummaryDto
import org.springframework.stereotype.Component

@Component
class DoctorProfileMapper {

    fun toDto(profile: DoctorProfile): DoctorProfileDto = DoctorProfileDto(
        id = profile.id,
        userId = profile.user.id,
        specialtyId = profile.specialty.id,
        medicalCode = profile.medicalCode,
        bio = profile.bio,
        yearsOfExperience = profile.yearsOfExperience,
        visitFee = profile.visitFee,
        defaultSessionDuration = profile.defaultSessionDuration,
        maxDailyAppointments = profile.maxDailyAppointments,
        userFullName = profile.user.fullName,
        userPhone = profile.user.phone,
        specialtyName = profile.specialty.name,
        createdAt = profile.createdAt
    )

    fun toSummary(profile: DoctorProfile): DoctorSummaryDto = DoctorSummaryDto(
        userId = profile.user.id ?: 0L,
        fullName = profile.user.fullName,
        specialty = profile.specialty.name,
        medicalCode = profile.medicalCode,
        visitFee = profile.visitFee,
        sessionDuration = profile.defaultSessionDuration
    )

    fun updateEntity(
        entity: DoctorProfile,
        dto: DoctorProfileDto,
        specialty: ir.tolooesalamat.app.domain.Specialty
    ) {
        entity.specialty = specialty
        entity.medicalCode = dto.medicalCode
        entity.bio = dto.bio
        entity.yearsOfExperience = dto.yearsOfExperience
        entity.visitFee = dto.visitFee
        entity.defaultSessionDuration = dto.defaultSessionDuration
        entity.maxDailyAppointments = dto.maxDailyAppointments
    }
}