package ir.tolooesalamat.app.controller

import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

/**
 * کنترلر صفحات HTML (Thymeleaf).
 */
@Controller
class WebController {

    /**
     * 🏠 صفحه اول = ثبت‌نام بیمار.
     */
    @GetMapping("/")
    fun home(): String = "auth/register"

    /**
     * 🔐 صفحه ورود.
     */
    @GetMapping("/login")
    fun loginPage(): String = "auth/login"

    /**
     * 📊 داشبورد (بعد از ورود).
     */
    @GetMapping("/dashboard")
    fun dashboard(auth: Authentication?): String {
        if (auth == null) return "redirect:/login"

        val role = auth.authorities.firstOrNull()?.authority ?: return "redirect:/login"

        return when (role) {
            "ROLE_ADMIN" -> "dashboard/admin"
            "ROLE_DOCTOR" -> "dashboard/doctor"
            "ROLE_RECEPTIONIST" -> "dashboard/receptionist"
            "ROLE_PATIENT" -> "dashboard/patient"
            else -> "redirect:/login"
        }
    }

    /**
     * ❌ صفحه خطای 403.
     */
    @GetMapping("/403")
    fun accessDenied(): String = "errors/403"
}