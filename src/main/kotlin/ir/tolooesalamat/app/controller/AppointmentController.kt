package ir.tolooesalamat.app.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import ir.tolooesalamat.app.dto.*
import ir.tolooesalamat.app.security.CustomUserDetails
import ir.tolooesalamat.app.service.AppointmentService
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/appointments")
@Tag(name = "Appointments", description = "مدیریت نوبت‌ها")
class AppointmentController(
    private val appointmentService: AppointmentService
) {

    @Operation(summary = "جستجوی نوبت‌ها", security = [SecurityRequirement(name = "bearer-jwt")])
    @PostMapping("/search")
    fun search(
        @RequestBody request: AppointmentSearchRequest
    ): ResponseEntity<PagedResponse<AppointmentSummaryDto>> =
        ResponseEntity.ok(appointmentService.search(request))

    @Operation(summary = "نوبت‌های کاربر جاری")
    @GetMapping("/my")
    fun myAppointments(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<List<AppointmentSummaryDto>> =
        ResponseEntity.ok(appointmentService.findByUser(userDetails.user))

    @Operation(summary = "صف امروز یک پزشک")
    @GetMapping("/queue/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    fun todayQueue(
        @PathVariable doctorId: Long,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?
    ): ResponseEntity<List<AppointmentSummaryDto>> =
        ResponseEntity.ok(appointmentService.getTodayQueue(doctorId, date ?: LocalDate.now()))

    @Operation(summary = "اطلاعات یک نوبت")
    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<AppointmentDto> =
        ResponseEntity.ok(appointmentService.getById(id, userDetails.user))

    @Operation(summary = "ثبت نوبت جدید")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'PATIENT')")
    fun create(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody dto: AppointmentDto
    ): ResponseEntity<AppointmentDto> =
        ResponseEntity.ok(appointmentService.create(userDetails.user, dto))

    @Operation(summary = "تأیید نوبت")
    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    fun confirm(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<AppointmentDto> =
        ResponseEntity.ok(appointmentService.confirm(id, userDetails.user))

    @Operation(summary = "پذیرش بیمار (check-in)")
    @PatchMapping("/{id}/check-in")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    fun checkIn(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<AppointmentDto> =
        ResponseEntity.ok(appointmentService.checkIn(id, userDetails.user))

    @Operation(summary = "شروع ویزیت")
    @PatchMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    fun start(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<AppointmentDto> =
        ResponseEntity.ok(appointmentService.startVisit(id, userDetails.user))

    @Operation(summary = "اتمام ویزیت")
    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    fun complete(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<AppointmentDto> =
        ResponseEntity.ok(appointmentService.completeVisit(id, userDetails.user))

    @Operation(summary = "لغو نوبت")
    @PatchMapping("/{id}/cancel")
    fun cancel(
        @PathVariable id: Long,
        @RequestBody(required = false) request: CancelAppointmentRequest?,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<AppointmentDto> =
        ResponseEntity.ok(appointmentService.cancel(id, request?.reason, userDetails.user))

    @Operation(summary = "ثبت عدم مراجعه")
    @PatchMapping("/{id}/no-show")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    fun markNoShow(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<AppointmentDto> =
        ResponseEntity.ok(appointmentService.markNoShow(id, userDetails.user))

    @Operation(summary = "اسلات‌های آزاد یک پزشک در یک روز")
    @GetMapping("/available-slots")
    fun availableSlots(
        @RequestParam doctorId: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): ResponseEntity<List<AvailableSlotDto>> =
        ResponseEntity.ok(appointmentService.findAvailableSlots(doctorId, date))

    @Operation(summary = "آمار روزانه پزشک")
    @GetMapping("/stats/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    fun doctorStats(
        @PathVariable doctorId: Long,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?
    ): ResponseEntity<Map<String, Any>> =
        ResponseEntity.ok(appointmentService.getDoctorDailyStats(doctorId, date ?: LocalDate.now()))
}