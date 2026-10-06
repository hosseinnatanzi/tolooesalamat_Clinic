package ir.tolooesalamat.app.mapper

import ir.tolooesalamat.app.domain.Appointment
import ir.tolooesalamat.app.domain.User
import ir.tolooesalamat.app.dto.AppointmentDto
import ir.tolooesalamat.app.dto.AppointmentSummaryDto
import org.springframework.stereotype.Component

@Component
class AppointmentMapper {

    fun toDto(appointment: Appointment): AppointmentDto = AppointmentDto(
        id = appointment.id,
        doctorId = appointment.doctor.id,
        patientId = appointment.patient.id,
        date = appointment.date,
        startTime = appointment.startTime,
        duration = appointment.duration,
        sessionType = appointment.sessionType,
        status = appointment.status,
        queueNumber = appointment.queueNumber,
        notes = appointment.notes,
        checkedInAt = appointment.checkedInAt,
        startedAt = appointment.startedAt,
        completedAt = appointment.completedAt,
        doctorName = appointment.doctor.fullName,
        patientName = appointment.patient.fullName,
        patientFileNumber = appointment.patient.patientProfile?.fileNumber,
        endTime = appointment.endTime,
        createdAt = appointment.createdAt
    )

    fun toSummary(appointment: Appointment): AppointmentSummaryDto = AppointmentSummaryDto(
        id = appointment.id ?: 0L,
        doctorName = appointment.doctor.fullName,
        patientName = appointment.patient.fullName,
        patientFileNumber = appointment.patient.patientProfile?.fileNumber,
        date = appointment.date,
        startTime = appointment.startTime,
        endTime = appointment.endTime,
        status = appointment.status.name,
        statusLabel = appointment.status.label,
        sessionType = appointment.sessionType.name,
        queueNumber = appointment.queueNumber
    )

    fun toEntity(
        dto: AppointmentDto,
        doctor: User,
        patient: User
    ): Appointment = Appointment(
        doctor = doctor,
        patient = patient,
        date = dto.date!!,
        startTime = dto.startTime!!,
        duration = dto.duration,
        sessionType = dto.sessionType,
        status = dto.status,
        notes = dto.notes
    )
}