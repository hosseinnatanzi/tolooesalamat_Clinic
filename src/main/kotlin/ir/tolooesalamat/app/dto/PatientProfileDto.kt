package ir.tolooesalamat.app.dto

import ir.tolooesalamat.app.domain.enum.Gender        // ← import جدید
import jakarta.validation.constraints.*
import java.time.LocalDate
import java.time.LocalDateTime

data class PatientProfileDto(
    val id: Long? = null,
    val userId: Long? = null,

    @field:NotBlank(message = "شماره پرونده الزامی است")
    @field:Size(max = 20)
    val fileNumber: String = "",

    val birthDate: LocalDate? = null,
    val gender: Gender? = null,

    @field:Size(max = 2000)
    val address: String? = null,

    @field:Size(max = 100)
    val emergencyContactName: String? = null,

    val emergencyContactMobile: String? = null,
    val emergencyContactLandline: String? = null,

    @field:Size(max = 50)
    val emergencyContactRelation: String? = null,

    val medicalHistory: String? = null,
    val currentMedications: String? = null,
    val allergies: String? = null,
    val substanceUse: String? = null,

    val primaryDoctorId: Long? = null,
    val primaryDoctorName: String? = null,

    val fileStatus: String = "ACTIVE",

    val userFullName: String? = null,
    val userPhone: String? = null,
    val createdAt: LocalDateTime? = null
)

data class ChangeFileStatusRequest(
    @field:NotBlank
    @field:Pattern(regexp = "^(ACTIVE|ARCHIVED|CLOSED)$")
    val status: String
)

data class PatientSummaryDto(
    val userId: Long,
    val fileNumber: String,
    val fullName: String,
    val phone: String,
    val gender: String?,
    val fileStatus: String
)

data class PatientSearchRequest(
    val keyword: String? = null,
    val doctorId: Long? = null,
    val fileStatus: String? = null,
    val page: Int = 0,
    val size: Int = 20
)