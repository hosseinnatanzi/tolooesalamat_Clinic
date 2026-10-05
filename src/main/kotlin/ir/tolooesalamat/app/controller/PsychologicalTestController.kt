package ir.tolooesalamat.app.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import ir.tolooesalamat.app.dto.*
import ir.tolooesalamat.app.security.CustomUserDetails
import ir.tolooesalamat.app.service.PsychologicalTestService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/psychological-tests")
@Tag(name = "Psychological Tests", description = "تست‌های روانشناسی")
class PsychologicalTestController(
    private val psychologicalTestService: PsychologicalTestService
) {

    // ═══════════════════════════════════════════
    // 📋 کاتالوگ تست‌ها
    // ═══════════════════════════════════════════

    @Operation(summary = "لیست تست‌های فعال (عمومی)")
    @GetMapping("/catalog")
    fun listCatalog(): ResponseEntity<List<PsychologicalTestSummaryDto>> =
        ResponseEntity.ok(psychologicalTestService.getActiveTests())

    @Operation(summary = "لیست تمام تست‌ها (ادمین)")
    @GetMapping("/catalog/all")
    @PreAuthorize("hasRole('ADMIN')")
    fun listAll(): ResponseEntity<List<PsychologicalTestDto>> =
        ResponseEntity.ok(psychologicalTestService.getAllTests())

    @Operation(summary = "اطلاعات یک تست")
    @GetMapping("/catalog/{id}")
    fun getTest(@PathVariable id: Long): ResponseEntity<PsychologicalTestDto> =
        ResponseEntity.ok(psychologicalTestService.getTestById(id))

    @Operation(summary = "ایجاد تست جدید")
    @PostMapping("/catalog")
    @PreAuthorize("hasRole('ADMIN')")
    fun createTest(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody dto: PsychologicalTestDto
    ): ResponseEntity<PsychologicalTestDto> =
        ResponseEntity.ok(psychologicalTestService.createTest(dto, userDetails.user))

    @Operation(summary = "به‌روزرسانی تست")
    @PutMapping("/catalog/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateTest(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody dto: PsychologicalTestDto
    ): ResponseEntity<PsychologicalTestDto> =
        ResponseEntity.ok(psychologicalTestService.updateTest(id, dto, userDetails.user))

    @Operation(summary = "تغییر وضعیت فعال/غیرفعال")
    @PatchMapping("/catalog/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    fun toggleTest(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<PsychologicalTestDto> =
        ResponseEntity.ok(psychologicalTestService.toggleTestActive(id, userDetails.user))

    // ═══════════════════════════════════════════
    // 📝 نتایج تست
    // ═══════════════════════════════════════════

    @Operation(summary = "ثبت نتیجه تست (فقط پزشک)")
    @PostMapping("/results")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    fun recordResult(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody request: RecordTestResultRequest
    ): ResponseEntity<TestResultDto> =
        ResponseEntity.ok(psychologicalTestService.recordTestResult(request, userDetails.user))

    @Operation(summary = "تأیید نهایی نتیجه با امضای دیجیتال")
    @PostMapping("/results/{id}/finalize")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    fun finalizeResult(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<TestResultDto> =
        ResponseEntity.ok(psychologicalTestService.finalizeTestResult(id, userDetails.user))

    @Operation(summary = "اطلاعات یک نتیجه تست")
    @GetMapping("/results/{id}")
    fun getResult(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<TestResultDto> =
        ResponseEntity.ok(psychologicalTestService.getTestResultById(id, userDetails.user))

    @Operation(summary = "جستجوی نتایج تست")
    @PostMapping("/results/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    fun searchResults(
        @RequestBody request: TestResultSearchRequest
    ): ResponseEntity<PagedResponse<TestResultSummaryDto>> =
        ResponseEntity.ok(psychologicalTestService.searchTestResults(request))

    @Operation(summary = "سابقه تست‌های یک بیمار (بررسی طول درمان)")
    @GetMapping("/patients/{patientId}/history")
    fun getPatientHistory(
        @PathVariable patientId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<List<TestResultSummaryDto>> =
        ResponseEntity.ok(psychologicalTestService.getPatientTestHistory(patientId, userDetails.user))

    @Operation(summary = "آخرین نتایج یک بیمار")
    @GetMapping("/patients/{patientId}/latest")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    fun getLatestResults(
        @PathVariable patientId: Long,
        @RequestParam(defaultValue = "10") limit: Int,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<List<TestResultSummaryDto>> =
        ResponseEntity.ok(psychologicalTestService.getLatestResults(patientId, limit, userDetails.user))
}