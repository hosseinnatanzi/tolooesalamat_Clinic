package ir.tolooesalamat.app.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import ir.tolooesalamat.app.dto.DoctorProfileDto
import ir.tolooesalamat.app.dto.DoctorSummaryDto
import ir.tolooesalamat.app.security.CustomUserDetails
import ir.tolooesalamat.app.service.DoctorService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/doctors")
@Tag(name = "Doctors", description = "پروفایل پزشکان")
class DoctorController(
    private val doctorService: DoctorService
) {

    @Operation(summary = "لیست پزشکان فعال (عمومی)")
    @GetMapping("/active")
    fun listActive(): ResponseEntity<List<DoctorSummaryDto>> =
        ResponseEntity.ok(doctorService.findAllActive())

    @Operation(summary = "پزشکان یک تخصص")
    @GetMapping("/by-specialty/{specialtyId}")
    fun listBySpecialty(
        @PathVariable specialtyId: Long
    ): ResponseEntity<List<DoctorSummaryDto>> =
        ResponseEntity.ok(doctorService.findBySpecialty(specialtyId))

    @Operation(summary = "پروفایل پزشک با User ID")
    @GetMapping("/{userId}/profile")
    fun getProfile(@PathVariable userId: Long): ResponseEntity<DoctorProfileDto> =
        ResponseEntity.ok(doctorService.findByUserId(userId))

    @Operation(summary = "ایجاد پروفایل پزشک (فقط ادمین)")
    @PostMapping("/{userId}/profile")
    @PreAuthorize("hasRole('ADMIN')")
    fun createProfile(
        @PathVariable userId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody dto: DoctorProfileDto
    ): ResponseEntity<DoctorProfileDto> =
        ResponseEntity.ok(doctorService.createProfile(userId, dto, userDetails.user))

    @Operation(summary = "به‌روزرسانی پروفایل پزشک")
    @PutMapping("/{userId}/profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    fun updateProfile(
        @PathVariable userId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody dto: DoctorProfileDto
    ): ResponseEntity<DoctorProfileDto> =
        ResponseEntity.ok(doctorService.updateProfile(userId, dto, userDetails.user))
}