package ir.tolooesalamat.app.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import ir.tolooesalamat.app.dto.*
import ir.tolooesalamat.app.security.CustomUserDetails
import ir.tolooesalamat.app.service.PatientService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/patients")
@Tag(name = "Patients", description = "پروفایل بیماران")
class PatientController(
    private val patientService: PatientService
) {

    @Operation(summary = "جستجوی بیماران (منشی/ادمین/پزشک)")
    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    fun search(
        @RequestBody request: PatientSearchRequest
    ): ResponseEntity<PagedResponse<PatientSummaryDto>> =
        ResponseEntity.ok(patientService.search(request))

    @Operation(summary = "پروفایل بیمار با User ID")
    @GetMapping("/{userId}")
    fun getProfile(
        @PathVariable userId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<PatientProfileDto> =
        ResponseEntity.ok(patientService.getByUserId(userId, userDetails.user))

    @Operation(summary = "پیدا کردن با شماره پرونده")
    @GetMapping("/by-file/{fileNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    fun getByFileNumber(@PathVariable fileNumber: String): ResponseEntity<PatientProfileDto> =
        ResponseEntity.ok(patientService.getByFileNumber(fileNumber))

    @Operation(summary = "به‌روزرسانی پروفایل بیمار")
    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    fun updateProfile(
        @PathVariable userId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody dto: PatientProfileDto
    ): ResponseEntity<PatientProfileDto> =
        ResponseEntity.ok(patientService.updateProfile(userId, dto, userDetails.user))

    @Operation(summary = "تغییر وضعیت پرونده")
    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    fun changeStatus(
        @PathVariable userId: Long,
        @RequestBody request: ChangeFileStatusRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<PatientProfileDto> =
        ResponseEntity.ok(patientService.changeFileStatus(userId, request.status, userDetails.user))

    @Operation(summary = "بیماران یک پزشک")
    @GetMapping("/by-doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    fun listByDoctor(@PathVariable doctorId: Long): ResponseEntity<List<PatientSummaryDto>> =
        ResponseEntity.ok(patientService.findByDoctor(doctorId))
}