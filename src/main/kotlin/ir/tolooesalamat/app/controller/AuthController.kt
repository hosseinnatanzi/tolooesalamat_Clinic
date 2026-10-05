package ir.tolooesalamat.app.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import ir.tolooesalamat.app.dto.*
import ir.tolooesalamat.app.security.CustomUserDetails
import ir.tolooesalamat.app.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "ورود، ثبت‌نام، تازه‌سازی توکن")
class AuthController(
    private val authService: AuthService
) {

    @Operation(
        summary = "ثبت‌نام بیمار",
        description = "ثبت‌نام بیمار با نام، سن، جنسیت، موبایل و رمز عبور"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "ثبت‌نام موفق"),
        ApiResponse(responseCode = "400", description = "اطلاعات نامعتبر"),
        ApiResponse(responseCode = "409", description = "شماره موبایل تکراری")
    )
    @PostMapping("/register")
    fun register(
        @Valid @RequestBody request: PatientRegisterRequest
    ): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(authService.registerPatient(request))

    @Operation(
        summary = "ورود با موبایل",
        description = "ورود با شماره موبایل و رمز عبور"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "ورود موفق"),
        ApiResponse(responseCode = "401", description = "رمز اشتباه")
    )
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest
    ): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(authService.login(request))

    @Operation(
        summary = "تازه‌سازی توکن",
        description = "دریافت Access Token جدید با Refresh Token"
    )
    @PostMapping("/refresh")
    fun refresh(
        @Valid @RequestBody request: RefreshTokenRequest
    ): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(authService.refresh(request))

    @Operation(
        summary = "خروج از سیستم",
        description = "ثبت لاگ خروج و اعلام به کلاینت",
        security = [SecurityRequirement(name = "bearer-jwt")]
    )
    @PostMapping("/logout")
    fun logout(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<Map<String, String>> {
        authService.logout(userDetails.user)
        return ResponseEntity.ok(mapOf("message" to "خروج موفق"))
    }

    @Operation(
        summary = "اطلاعات کاربر جاری",
        security = [SecurityRequirement(name = "bearer-jwt")]
    )
    @GetMapping("/me")
    fun me(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<UserSummaryDto> =
        ResponseEntity.ok(
            UserSummaryDto(
                id = userDetails.id,
                phone = userDetails.phone,
                fullName = userDetails.fullName,
                firstName = userDetails.user.firstName,
                lastName = userDetails.user.lastName,
                age = userDetails.user.age,
                gender = userDetails.user.gender.name,
                genderLabel = userDetails.user.gender.label,
                role = userDetails.user.role.name,
                roleLabel = userDetails.user.role.label
            )
        )
}