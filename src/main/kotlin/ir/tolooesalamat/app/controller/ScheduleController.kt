package ir.tolooesalamat.app.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import ir.tolooesalamat.app.dto.WeeklyScheduleDto
import ir.tolooesalamat.app.security.CustomUserDetails
import ir.tolooesalamat.app.service.ScheduleService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/schedules")
@Tag(name = "Schedules", description = "برنامه هفتگی پزشکان")
class ScheduleController(
    private val scheduleService: ScheduleService
) {

    @Operation(summary = "برنامه هفتگی پزشک")
    @GetMapping("/doctor/{doctorProfileId}")
    fun getDoctorSchedule(
        @PathVariable doctorProfileId: Long
    ): ResponseEntity<List<WeeklyScheduleDto>> =
        ResponseEntity.ok(scheduleService.getDoctorSchedule(doctorProfileId))

    @Operation(summary = "افزودن برنامه")
    @PostMapping("/doctor/{doctorProfileId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    fun addSchedule(
        @PathVariable doctorProfileId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody dto: WeeklyScheduleDto
    ): ResponseEntity<WeeklyScheduleDto> =
        ResponseEntity.ok(scheduleService.addSchedule(doctorProfileId, dto, userDetails.user))

    @Operation(summary = "به‌روزرسانی برنامه")
    @PutMapping("/{scheduleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    fun updateSchedule(
        @PathVariable scheduleId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody dto: WeeklyScheduleDto
    ): ResponseEntity<WeeklyScheduleDto> =
        ResponseEntity.ok(scheduleService.updateSchedule(scheduleId, dto, userDetails.user))

    @Operation(summary = "حذف برنامه")
    @DeleteMapping("/{scheduleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    fun deleteSchedule(
        @PathVariable scheduleId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<Void> {
        scheduleService.deleteSchedule(scheduleId, userDetails.user)
        return ResponseEntity.noContent().build()
    }
}