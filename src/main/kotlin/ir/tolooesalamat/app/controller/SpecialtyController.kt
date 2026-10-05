package ir.tolooesalamat.app.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import ir.tolooesalamat.app.dto.SpecialtyDto
import ir.tolooesalamat.app.dto.SpecialtySummaryDto
import ir.tolooesalamat.app.security.CustomUserDetails
import ir.tolooesalamat.app.service.SpecialtyService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/specialties")
@Tag(name = "Specialties", description = "مدیریت تخصص‌های پزشکی")
class SpecialtyController(
    private val specialtyService: SpecialtyService
) {

    @Operation(summary = "لیست تخصص‌های فعال (عمومی)")
    @GetMapping("/active")
    fun listActive(): ResponseEntity<List<SpecialtySummaryDto>> =
        ResponseEntity.ok(specialtyService.findAllActive())

    @Operation(summary = "لیست تمام تخصص‌ها (ادمین)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun listAll(): ResponseEntity<List<SpecialtyDto>> =
        ResponseEntity.ok(specialtyService.findAll())

    @Operation(summary = "اطلاعات یک تخصص")
    @GetMapping("/{id}")
    fun getSpecialty(@PathVariable id: Long): ResponseEntity<SpecialtyDto> =
        ResponseEntity.ok(specialtyService.findById(id))

    @Operation(summary = "ایجاد تخصص جدید")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun create(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody dto: SpecialtyDto
    ): ResponseEntity<SpecialtyDto> =
        ResponseEntity.ok(specialtyService.create(dto, userDetails.user))

    @Operation(summary = "به‌روزرسانی تخصص")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun update(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody dto: SpecialtyDto
    ): ResponseEntity<SpecialtyDto> =
        ResponseEntity.ok(specialtyService.update(id, dto, userDetails.user))

    @Operation(summary = "تغییر وضعیت فعال/غیرفعال")
    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    fun toggle(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<SpecialtyDto> =
        ResponseEntity.ok(specialtyService.toggleActive(id, userDetails.user))
}