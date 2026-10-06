package ir.tolooesalamat.app.dto

import ir.tolooesalamat.app.domain.enum.*  // ← import جدید
 import jakarta.validation.constraints.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class AppointmentDto(
    val id: Long? = null,

    @field:NotNull val doctorId: Long? = null,
    val patientId: Long? = null,

    @field:NotNull @field:FutureOrPresent val date: LocalDate? = null,
    @field:NotNull val startTime: LocalTime? = null,
    @field:Min(15) @field:Max(180) val duration: Int = 45,

    val sessionType: SessionType = SessionType.FOLLOW_UP,
    val status: AppointmentStatus = AppointmentStatus.PENDING,
    val queueNumber: Int? = null,
    val notes: String? = null,
    val checkedInAt: LocalDateTime? = null,
    val startedAt: LocalDateTime? = null,
    val completedAt: LocalDateTime? = null,

    val doctorName: String? = null,
    val patientName: String? = null,
    val patientFileNumber: String? = null,
    val endTime: LocalTime? = null,
    val createdAt: LocalDateTime? = null
)

data class AppointmentSummaryDto(
    val id: Long,
    val doctorName: String,
    val patientName: String,
    val patientFileNumber: String?,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val status: String,
    val statusLabel: String,
    val sessionType: String,
    val queueNumber: Int?
)

data class BookAppointmentRequest(
    @field:NotNull val doctorId: Long? = null,
    @field:NotNull @field:FutureOrPresent val date: LocalDate? = null,
    @field:NotNull val startTime: LocalTime? = null,
    val notes: String? = null
)

data class CancelAppointmentRequest(val reason: String? = null)

data class ChangeAppointmentStatusRequest(
    @field:NotNull val status: AppointmentStatus
)

data class AppointmentSearchRequest(
    val doctorId: Long? = null,
    val patientId: Long? = null,
    val status: AppointmentStatus? = null,
    val fromDate: LocalDate? = null,
    val toDate: LocalDate? = null,
    val page: Int = 0,
    val size: Int = 20
)

data class AvailableSlotDto(
    val startTime: LocalTime,
    val endTime: LocalTime
)