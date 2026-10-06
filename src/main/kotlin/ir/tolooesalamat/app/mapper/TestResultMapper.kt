package ir.tolooesalamat.app.mapper

import ir.tolooesalamat.app.domain.TestResult
import ir.tolooesalamat.app.dto.TestResultDto
import ir.tolooesalamat.app.dto.TestResultSummaryDto
import org.springframework.stereotype.Component

@Component
class TestResultMapper {

    fun toDto(result: TestResult): TestResultDto = TestResultDto(
        id = result.id,
        patientId = result.patient.id,
        testId = result.test.id,
        doctorId = result.doctor.id,
        testDate = result.testDate,
        rawScore = result.rawScore,
        interpretation = result.interpretation,
        diagnosis = result.diagnosis,
        rawData = result.rawData,
        recommendations = result.recommendations,
        doctorSignature = result.doctorSignature,
        confidentialityLevel = result.confidentialityLevel,
        patientName = result.patient.fullName,
        patientFileNumber = result.patient.patientProfile?.fileNumber,
        doctorName = result.doctor.fullName,
        testName = result.test.name,
        testCode = result.test.code,
        isFinalized = result.isFinalized,
        createdAt = result.createdAt
    )

    fun toSummary(result: TestResult): TestResultSummaryDto = TestResultSummaryDto(
        id = result.id ?: 0L,
        testName = result.test.name,
        testCode = result.test.code,
        testDate = result.testDate,
        doctorName = result.doctor.fullName,
        confidentialityLevel = result.confidentialityLevel,
        isFinalized = result.isFinalized
    )
}