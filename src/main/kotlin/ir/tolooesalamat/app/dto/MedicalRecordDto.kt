package ir.tolooesalamat.app.dto

import jakarta.validation.constraints.*
import java.time.LocalDateTime

data class MedicalRecordDto(
    val id: Long? = null,
    val patientId: Long? = null,
    val doctorId: Long? = null,

    @field:Min(1)
    val sessionNumber: Int = 1,

    val sessionDate: LocalDateTime? = null,

    @field:Size(max = 5000)
    val chiefComplaint: String? = null,

    @field:Size(max = 10000)
    val presentIllness: String? = null,

    @field:Size(max = 5000)
    val mentalStatusExam: String? = null,

    @field:Size(max = 5000)
    val diagnosis: String? = null,

    @field:Size(max = 5000)
    val treatmentPlan: String? = null,

    @field:Size(max = 5000)
    val medications: String? = null,

    @field:Size(max = 5000)
    val sessionNotes: String? = null,

    val diagnosisSignature: String? = null,
    val isFinalized: Boolean = false,
    val finalizedAt: LocalDateTime? = null,

    val patientName: String? = null,
    val patientFileNumber: String? = null,
    val doctorName: String? = null,
    val createdAt: LocalDateTime? = null
)

data class MedicalRecordSummaryDto(
    val id: Long,
    val sessionNumber: Int,
    val sessionDate: LocalDateTime,
    val doctorName: String,
    val isFinalized: Boolean
)

data class MedicalRecordSearchRequest(
    val patientId: Long? = null,
    val doctorId: Long? = null,
    val fromDate: LocalDateTime? = null,
    val toDate: LocalDateTime? = null,
    val page: Int = 0,
    val size: Int = 20
)