package ir.tolooesalamat.app.config

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag

/**
 * مثال استفاده:
 *
 * @RestController
 * @RequestMapping("/api/auth")
 * @Tag(name = "Authentication", description = "ورود، ثبت‌نام، تازه‌سازی")
 * class AuthController {
 *
 *     @Operation(
 *         summary = "ورود با موبایل",
 *         description = "با شماره موبایل و رمز عبور وارد شوید"
 *     )
 *     @ApiResponses(
 *         ApiResponse(responseCode = "200", description = "ورود موفق"),
 *         ApiResponse(responseCode = "400", description = "اطلاعات نامعتبر"),
 *         ApiResponse(responseCode = "401", description = "رمز اشتباه")
 *     )
 *     @PostMapping("/login")
 *     fun login(@RequestBody request: LoginRequest): AuthResponse { ... }
 * }
 */
object OpenApiAnnotations