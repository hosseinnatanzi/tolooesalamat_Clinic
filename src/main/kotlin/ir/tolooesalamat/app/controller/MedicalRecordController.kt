package ir.tolooesalamat.app.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import ir.tolooesalamat.app.dto.*
import ir.tolooesalamat.app.security.CustomUserDetails
import ir.tolooesalamat.app.service.MedicalRecordService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/records")
@Tag(name = "Medical Records", description = "پرونده پزشکی")
class MedicalRecordController(
    private val medicalRecordService: MedicalRecordService
) {

    @Operation(summary = "جستجوی پرونده‌ها", security = [SecurityRequirement(name = "bearer-jwt")])
    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    fun search(
        @RequestBody request: MedicalRecordSearchRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<PagedResponse<MedicalRecordSummaryDto>> =
        ResponseEntity.ok(medicalRecordService.search(request, userDetails.user))

    @Operation(summary = "سابقه پزشکی بیمار")
    @GetMapping("/patient/{patientId}/history")
    fun getPatientHistory(
        @PathVariable patientId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<List<MedicalRecordSummaryDto>> =
        ResponseEntity.ok(medicalRecordService.getPatientHistory(patientId, userDetails.user))

    @Operation(summary = "اطلاعات یک پرونده")
    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<MedicalRecordDto> =
        ResponseEntity.ok(medicalRecordService.getById(id, userDetails.user))

    @Operation(summary = "ثبت پرونده جدید")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    fun create(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody dto: MedicalRecordDto
    ): ResponseEntity<MedicalRecordDto> =
        ResponseEntity.ok(medicalRecordService.create(dto, userDetails.user))

    @Operation(summary = "به‌روزرسانی پرونده")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    fun update(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody dto: MedicalRecordDto
    ): ResponseEntity<MedicalRecordDto> =
        ResponseEntity.ok(medicalRecordService.update(id, dto, userDetails.user))

    @Operation(summary = "تأیید نهایی با امضای دیجیتال")
    @PostMapping("/{id}/finalize")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    fun finalize(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<MedicalRecordDto> =
        ResponseEntity.ok(medicalRecordService.finalizeRecord(id, userDetails.user))

    @Operation(summary = "بررسی صحت امضای دیجیتال")
    @GetMapping("/{id}/verify-signature")
    fun verifySignature(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<Map<String, Boolean>> =
        ResponseEntity.ok(mapOf("valid" to medicalRecordService.verifySignature(id, userDetails.user)))
}