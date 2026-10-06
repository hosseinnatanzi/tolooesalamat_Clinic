package ir.tolooesalamat.app.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import ir.tolooesalamat.app.dto.ClinicDto
import ir.tolooesalamat.app.dto.ClinicSummaryDto
import ir.tolooesalamat.app.security.CustomUserDetails
import ir.tolooesalamat.app.service.ClinicService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/clinics")
@Tag(name = "Clinics", description = "مدیریت مطب‌ها و شعبه‌ها")
class ClinicController(
    private val clinicService: ClinicService
) {

    // ═══════════════════════════════════════════
    // 📋 لیست‌ها
    // ═══════════════════════════════════════════

    @Operation(
        summary = "لیست مطب‌های فعال",
        description = "برای استفاده در dropdown ها — عمومی برای همه نقش‌های لاگین‌شده"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "لیست مطب‌های فعال"),
        ApiResponse(responseCode = "401", description = "احراز هویت لازم است")
    )
    @GetMapping("/active")
    fun listActive(): ResponseEntity<List<ClinicSummaryDto>> =
        ResponseEntity.ok(clinicService.findAllActive())

    @Operation(
        summary = "لیست تمام مطب‌ها",
        description = "شامل فعال و غیرفعال — فقط مدیر",
        security = [SecurityRequirement(name = "bearer-jwt")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "لیست تمام مطب‌ها"),
        ApiResponse(responseCode = "403", description = "دسترسی غیرمجاز")
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun listAll(): ResponseEntity<List<ClinicDto>> =
        ResponseEntity.ok(clinicService.findAll())

    // ═══════════════════════════════════════════
    // 🔍 خواندن یک مطب
    // ═══════════════════════════════════════════

    @Operation(
        summary = "اطلاعات یک مطب",
        description = "دریافت جزئیات مطب با شناسه",
        security = [SecurityRequirement(name = "bearer-jwt")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "اطلاعات مطب"),
        ApiResponse(responseCode = "404", description = "مطب یافت نشد")
    )
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<ClinicDto> =
        ResponseEntity.ok(clinicService.findById(id))

    // ═══════════════════════════════════════════
    // ➕ ایجاد
    // ═══════════════════════════════════════════

    @Operation(
        summary = "ایجاد مطب جدید",
        description = "فقط مدیر می‌تواند مطب ایجاد کند",
        security = [SecurityRequirement(name = "bearer-jwt")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "مطب با موفقیت ایجاد شد"),
        ApiResponse(responseCode = "400", description = "اطلاعات نامعتبر"),
        ApiResponse(responseCode = "403", description = "دسترسی غیرمجاز"),
        ApiResponse(responseCode = "409", description = "نام مطب تکراری")
    )
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun create(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody dto: ClinicDto
    ): ResponseEntity<ClinicDto> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(clinicService.create(dto, userDetails.user))

    // ═══════════════════════════════════════════
    // ✏️ به‌روزرسانی
    // ═══════════════════════════════════════════

    @Operation(
        summary = "به‌روزرسانی مطب",
        description = "فقط مدیر می‌تواند مطب را ویرایش کند",
        security = [SecurityRequirement(name = "bearer-jwt")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "مطب با موفقیت به‌روزرسانی شد"),
        ApiResponse(responseCode = "400", description = "اطلاعات نامعتبر"),
        ApiResponse(responseCode = "403", description = "دسترسی غیرمجاز"),
        ApiResponse(responseCode = "404", description = "مطب یافت نشد")
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun update(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody dto: ClinicDto
    ): ResponseEntity<ClinicDto> =
        ResponseEntity.ok(clinicService.update(id, dto, userDetails.user))

    // ═══════════════════════════════════════════
    // 🔄 تغییر وضعیت فعال/غیرفعال
    // ═══════════════════════════════════════════

    @Operation(
        summary = "تغییر وضعیت فعال/غیرفعال",
        description = "Toggle کردن وضعیت فعال بودن مطب — فقط مدیر",
        security = [SecurityRequirement(name = "bearer-jwt")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "وضعیت تغییر کرد"),
        ApiResponse(responseCode = "403", description = "دسترسی غیرمجاز"),
        ApiResponse(responseCode = "404", description = "مطب یافت نشد")
    )
    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    fun toggleActive(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<ClinicDto> =
        ResponseEntity.ok(clinicService.toggleActive(id, userDetails.user))

    // ═══════════════════════════════════════════
    // 🗑️ حذف نرم
    // ═══════════════════════════════════════════

    @Operation(
        summary = "حذف نرم مطب",
        description = "مطب حذف نمی‌شود، فقط isDeleted = true — فقط مدیر",
        security = [SecurityRequirement(name = "bearer-jwt")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "مطب حذف شد"),
        ApiResponse(responseCode = "403", description = "دسترسی غیرمجاز"),
        ApiResponse(responseCode = "404", description = "مطب یافت نشد")
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<Void> {
        clinicService.delete(id, userDetails.user)
        return ResponseEntity.noContent().build()
    }
}