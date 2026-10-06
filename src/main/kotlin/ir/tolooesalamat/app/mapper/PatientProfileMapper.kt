package ir.tolooesalamat.app.mapper

import ir.tolooesalamat.app.domain.PatientProfile
import ir.tolooesalamat.app.domain.User
import ir.tolooesalamat.app.dto.PatientProfileDto
import ir.tolooesalamat.app.dto.PatientSummaryDto
import org.springframework.stereotype.Component

@Component
class PatientProfileMapper {

    fun toDto(profile: PatientProfile): PatientProfileDto = PatientProfileDto(
        id = profile.id,
        userId = profile.user.id,
        fileNumber = profile.fileNumber,
        birthDate = profile.birthDate,
        gender = profile.gender,
        address = profile.address,
        emergencyContactName = profile.emergencyContactName,
        emergencyContactMobile = profile.emergencyContactMobile,
        emergencyContactLandline = profile.emergencyContactLandline,
        emergencyContactRelation = profile.emergencyContactRelation,
        medicalHistory = profile.medicalHistory,
        currentMedications = profile.currentMedications,
        allergies = profile.allergies,
        substanceUse = profile.substanceUse,
        primaryDoctorId = profile.primaryDoctor?.id,
        primaryDoctorName = profile.primaryDoctor?.fullName,
        fileStatus = profile.fileStatus,
        userFullName = profile.user.fullName,
        userPhone = profile.user.phone,
        // totalTests حذف شد — از Repository محاسبه می‌شود
        createdAt = profile.createdAt
    )

    fun toSummary(profile: PatientProfile): PatientSummaryDto = PatientSummaryDto(
        userId = profile.user.id ?: 0L,
        fileNumber = profile.fileNumber,
        fullName = profile.user.fullName,
        phone = profile.user.phone,
        gender = profile.gender?.name,
        fileStatus = profile.fileStatus
    )

    fun toEntity(
        dto: PatientProfileDto,
        user: User,
        primaryDoctor: User? = null
    ): PatientProfile = PatientProfile(
        user = user,
        fileNumber = dto.fileNumber,
        birthDate = dto.birthDate,
        gender = dto.gender,
        address = dto.address,
        emergencyContactName = dto.emergencyContactName,
        emergencyContactMobile = dto.emergencyContactMobile,
        emergencyContactLandline = dto.emergencyContactLandline,
        emergencyContactRelation = dto.emergencyContactRelation,
        medicalHistory = dto.medicalHistory,
        currentMedications = dto.currentMedications,
        allergies = dto.allergies,
        substanceUse = dto.substanceUse,
        primaryDoctor = primaryDoctor,
        fileStatus = dto.fileStatus
    )

    fun updateEntity(entity: PatientProfile, dto: PatientProfileDto) {
        entity.birthDate = dto.birthDate
        entity.gender = dto.gender
        entity.address = dto.address
        entity.emergencyContactName = dto.emergencyContactName
        entity.emergencyContactMobile = dto.emergencyContactMobile
        entity.emergencyContactLandline = dto.emergencyContactLandline
        entity.emergencyContactRelation = dto.emergencyContactRelation
        entity.medicalHistory = dto.medicalHistory
        entity.currentMedications = dto.currentMedications
        entity.allergies = dto.allergies
        entity.substanceUse = dto.substanceUse
        entity.fileStatus = dto.fileStatus
    }
}