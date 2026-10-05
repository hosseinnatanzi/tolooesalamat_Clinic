package ir.tolooesalamat.app.mapper

import ir.tolooesalamat.app.domain.MedicalRecord
import ir.tolooesalamat.app.dto.MedicalRecordDto
import ir.tolooesalamat.app.dto.MedicalRecordSummaryDto
import org.springframework.stereotype.Component

@Component
class MedicalRecordMapper {

    fun toDto(record: MedicalRecord): MedicalRecordDto = MedicalRecordDto(
        id = record.id,
        patientId = record.patient.id,
        doctorId = record.doctor.id,
        sessionNumber = record.sessionNumber,
        sessionDate = record.sessionDate,
        // 🔓 رمزگشایی خودکار از طریق Converter
        chiefComplaint = record.chiefComplaint,
        presentIllness = record.presentIllness,
        mentalStatusExam = record.mentalStatusExam,
        diagnosis = record.diagnosis,
        treatmentPlan = record.treatmentPlan,
        medications = record.medications,
        sessionNotes = record.sessionNotes,
        diagnosisSignature = record.diagnosisSignature,
        isFinalized = record.isFinalized,
        finalizedAt = record.finalizedAt,
        patientName = record.patient.fullName,
        patientFileNumber = record.patient.patientProfile?.fileNumber,
        doctorName = record.doctor.fullName,
        createdAt = record.createdAt
    )

    fun toSummary(record: MedicalRecord): MedicalRecordSummaryDto = MedicalRecordSummaryDto(
        id = record.id ?: 0L,
        sessionNumber = record.sessionNumber,
        sessionDate = record.sessionDate,
        doctorName = record.doctor.fullName,
        isFinalized = record.isFinalized
    )

    fun toEntity(
        dto: MedicalRecordDto,
        patient: ir.tolooesalamat.app.domain.User,
        doctor: ir.tolooesalamat.app.domain.User
    ): MedicalRecord = MedicalRecord(
        patient = patient,
        doctor = doctor,
        sessionNumber = dto.sessionNumber,
        chiefComplaint = dto.chiefComplaint,
        presentIllness = dto.presentIllness,
        mentalStatusExam = dto.mentalStatusExam,
        diagnosis = dto.diagnosis,
        treatmentPlan = dto.treatmentPlan,
        medications = dto.medications,
        sessionNotes = dto.sessionNotes
    )
}