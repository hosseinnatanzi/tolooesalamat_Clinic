package ir.tolooesalamat.app.dto

import ir.tolooesalamat.app.domain.AppointmentStatus
import ir.tolooesalamat.app.domain.SessionType
import jakarta.validation.constraints.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * DTO کامل نوبت.
 */
data class AppointmentDto(
    val id: Long? = null,

    @field:NotNull(message = "انتخاب پزشک الزامی است")
    val doctorId: Long? = null,

    val patientId: Long? = null,

    @field:NotNull(message = "تاریخ الزامی است")
    @field:FutureOrPresent(message = "تاریخ نمی‌تواند در گذشته باشد")
    val date: LocalDate? = null,

    @field:NotNull(message = "ساعت شروع الزامی است")
    val startTime: LocalTime? = null,

    @field:Min(15) @field:Max(180)
    val duration: Int = 45,

    val sessionType: SessionType = SessionType.FOLLOW_UP,
    val status: AppointmentStatus = AppointmentStatus.PENDING,
    val queueNumber: Int? = null,
    val notes: String? = null,
    val checkedInAt: LocalDateTime? = null,
    val startedAt: LocalDateTime? = null,
    val completedAt: LocalDateTime? = null,

    // ─── نمایشی ───
    val doctorName: String? = null,
    val patientName: String? = null,
    val patientFileNumber: String? = null,
    val endTime: LocalTime? = null,
    val createdAt: LocalDateTime? = null
)

/**
 * DTO خلاصه نوبت (برای لیست‌ها).
 */
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

/**
 * درخواست ثبت نوبت.
 */
data class BookAppointmentRequest(
    @field:NotNull val doctorId: Long? = null,
    @field:NotNull @field:FutureOrPresent val date: LocalDate? = null,
    @field:NotNull val startTime: LocalTime? = null,
    val notes: String? = null
)

/**
 * درخواست لغو نوبت.
 */
data class CancelAppointmentRequest(
    val reason: String? = null
)

/**
 * درخواست تغییر وضعیت.
 */
data class ChangeAppointmentStatusRequest(
    @field:NotNull val status: AppointmentStatus
)

/**
 * درخواست جستجوی نوبت.
 */
data class AppointmentSearchRequest(
    val doctorId: Long? = null,
    val patientId: Long? = null,
    val status: AppointmentStatus? = null,
    val fromDate: LocalDate? = null,
    val toDate: LocalDate? = null,
    val page: Int = 0,
    val size: Int = 20
)

/**
 * اسلات آزاد.
 */
data class AvailableSlotDto(
    val startTime: LocalTime,
    val endTime: LocalTime
)