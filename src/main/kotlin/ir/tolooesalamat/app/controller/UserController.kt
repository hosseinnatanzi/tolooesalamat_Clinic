package ir.tolooesalamat.app.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import ir.tolooesalamat.app.domain.enum.Role
import ir.tolooesalamat.app.dto.*
import ir.tolooesalamat.app.security.CustomUserDetails
import ir.tolooesalamat.app.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "User Management", description = "مدیریت کاربران (فقط ادمین)")
class UserController(
    private val userService: UserService
) {

    @Operation(summary = "لیست کاربران با Pagination و فیلتر")
    @GetMapping
    fun listUsers(
        @RequestParam(required = false) role: Role?,
        @RequestParam(required = false) enabled: Boolean?,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PagedResponse<UserSummaryDto>> =
        ResponseEntity.ok(userService.searchUsers(role, enabled, keyword, page, size))

    @Operation(summary = "اطلاعات یک کاربر")
    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): ResponseEntity<UserDto> =
        ResponseEntity.ok(userService.getById(id))

    @Operation(summary = "ایجاد کاربر جدید (پزشک/منشی/ادمین)")
    @PostMapping
    fun createUser(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody dto: UserDto
    ): ResponseEntity<UserDto> =
        ResponseEntity.ok(userService.createUser(dto, userDetails.user))

    @Operation(summary = "به‌روزرسانی کاربر")
    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody dto: UserDto
    ): ResponseEntity<UserDto> =
        ResponseEntity.ok(userService.updateUser(id, dto, userDetails.user))

    @Operation(summary = "تغییر وضعیت فعال/غیرفعال")
    @PatchMapping("/{id}/status")
    fun changeStatus(
        @PathVariable id: Long,
        @RequestBody request: ChangeUserStatusRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<UserDto> =
        ResponseEntity.ok(userService.changeUserStatus(id, request.enabled, userDetails.user))

    @Operation(summary = "تغییر نقش کاربر")
    @PatchMapping("/{id}/role")
    fun changeRole(
        @PathVariable id: Long,
        @RequestBody request: ChangeUserRoleRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<UserDto> =
        ResponseEntity.ok(userService.changeUserRole(id, request, userDetails.user))

    @Operation(summary = "حذف نرم کاربر")
    @DeleteMapping("/{id}")
    fun deleteUser(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<Void> {
        userService.deleteUser(id, userDetails.user)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "لیست تمام پزشکان")
    @GetMapping("/doctors")
    fun listDoctors(): ResponseEntity<List<UserSummaryDto>> =
        ResponseEntity.ok(userService.findAllDoctors())

    @Operation(summary = "لیست تمام بیماران")
    @GetMapping("/patients")
    fun listPatients(): ResponseEntity<List<UserSummaryDto>> =
        ResponseEntity.ok(userService.findAllPatients())
}