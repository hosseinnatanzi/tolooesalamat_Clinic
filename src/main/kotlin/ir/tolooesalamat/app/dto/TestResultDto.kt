package ir.tolooesalamat.app.dto

import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class TestResultDto(
    val id: Long? = null,

    @field:NotNull(message = "انتخاب بیمار الزامی است")
    val patientId: Long? = null,

    @field:NotNull(message = "انتخاب تست الزامی است")
    val testId: Long? = null,

    val doctorId: Long? = null,
    val testDate: LocalDateTime? = null,

    val rawScore: String? = null,
    val interpretation: String? = null,
    val diagnosis: String? = null,
    val rawData: String? = null,
    val recommendations: String? = null,

    val doctorSignature: String? = null,
    val confidentialityLevel: String = "HIGHLY_CONFIDENTIAL",

    val patientName: String? = null,
    val patientFileNumber: String? = null,
    val doctorName: String? = null,
    val testName: String? = null,
    val testCode: String? = null,
    val isFinalized: Boolean = false,
    val createdAt: LocalDateTime? = null
)

data class TestResultSummaryDto(
    val id: Long,
    val testName: String,
    val testCode: String?,
    val testDate: LocalDateTime,
    val doctorName: String,
    val confidentialityLevel: String,
    val isFinalized: Boolean
)

data class RecordTestResultRequest(
    @field:NotNull(message = "انتخاب بیمار الزامی است")
    val patientId: Long? = null,

    @field:NotNull(message = "انتخاب تست الزامی است")
    val testId: Long? = null,

    val rawScore: String? = null,
    val interpretation: String? = null,
    val diagnosis: String? = null,
    val rawData: String? = null,
    val recommendations: String? = null
)

data class TestResultSearchRequest(
    val patientId: Long? = null,
    val doctorId: Long? = null,
    val testId: Long? = null,
    val from: LocalDateTime? = null,
    val to: LocalDateTime? = null,
    val page: Int = 0,
    val size: Int = 20
)