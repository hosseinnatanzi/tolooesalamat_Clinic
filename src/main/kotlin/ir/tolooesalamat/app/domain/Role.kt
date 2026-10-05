package ir.tolooesalamat.app.domain

/**
 * نقش کاربری در سیستم.
 * برای RBAC (Role-Based Access Control) استفاده می‌شود.
 */
enum class Role(val label: String) {
    ADMIN("مدیر"),
    DOCTOR("پزشک"),
    RECEPTIONIST("منشی"),
    PATIENT("بیمار");

    companion object {
        fun fromString(value: String?): Role? =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}