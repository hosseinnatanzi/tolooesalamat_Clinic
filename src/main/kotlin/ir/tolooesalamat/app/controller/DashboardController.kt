package ir.tolooesalamat.app.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import ir.tolooesalamat.app.security.CustomUserDetails
import ir.tolooesalamat.app.service.AppointmentService
import ir.tolooesalamat.app.service.PatientService
import ir.tolooesalamat.app.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "آمار داشبورد بر اساس نقش")
class DashboardController(
    private val appointmentService: AppointmentService,
    private val userService: UserService
) {

    @Operation(
        summary = "داشبورد پزشک",
        description = "آمار امروز + صف انتظار",
        security = [SecurityRequirement(name = "bearer-jwt")]
    )
    @GetMapping("/doctor")
    fun doctorDashboard(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<Map<String, Any>> {

        val doctorId = userDetails.id
        val today = LocalDate.now()

        val stats = appointmentService.getDoctorDailyStats(doctorId, today)
        val queue = appointmentService.getTodayQueue(doctorId, today)

        return ResponseEntity.ok(
            mapOf(
                "stats" to stats,
                "queue" to queue,
                "today" to today.toString()
            )
        )
    }

    @Operation(
        summary = "داشبورد مدیر",
        description = "آمار کلی مطب",
        security = [SecurityRequirement(name = "bearer-jwt")]
    )
    @GetMapping("/admin")
    fun adminDashboard(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(
            mapOf(
                "totalDoctors" to userService.findAllDoctors().size,
                "totalPatients" to userService.findAllPatients().size,
                "today" to LocalDate.now().toString()
            )
        )
    }
}