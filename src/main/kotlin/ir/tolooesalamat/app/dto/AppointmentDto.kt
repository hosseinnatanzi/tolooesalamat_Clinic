package ir.tolooesalamat.app.dto

import ir.tolooesalamat.app.domain.AppointmentStatus
import ir.tolooesalamat.app.domain.SessionType
import jakarta.validation.constraints.*
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class AppointmentDto(
    val id: Long? = null,

    @field:NotNull(message = "انتخاب پزشک الزامی است")
    val doctorId: Long? = null,

    val patientId: Long? = null,

    @field:NotNull(message = "تاریخ الزامی است")
    @field:FutureOrPresent(message = "تاریخ نمی‌تواند در گذشته باشد")
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val date: LocalDate? = null,

    @field:NotNull(message = "ساعت شروع الزامی است")
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    val startTime: LocalTime? = null,

    @field:Min(15) @field:Max(180)
    val duration: Int = 45,

    val sessionType: SessionType = SessionType.FOLLOW_UP,
    val status: AppointmentStatus = AppointmentStatus.PENDING,
    val queueNumber: Int? = null,

    @field:Size(max = 2000)
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
    @field:NotNull(message = "انتخاب پزشک الزامی است")
    val doctorId: Long? = null,

    @field:NotNull(message = "تاریخ الزامی است")
    @field:FutureOrPresent
    val date: LocalDate? = null,

    @field:NotNull(message = "ساعت شروع الزامی است")
    val startTime: LocalTime? = null,

    @field:Size(max = 1000)
    val notes: String? = null
)

data class CancelAppointmentRequest(
    @field:Size(max = 500)
    val reason: String? = null
)

data class ChangeAppointmentStatusRequest(
    @field:NotNull
    val status: AppointmentStatus
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