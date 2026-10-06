package ir.tolooesalamat.app.security

import ir.tolooesalamat.app.domain.enum.Role
import ir.tolooesalamat.app.domain.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * پیاده‌سازی UserDetails برای Spring Security.
 *
 * شامل اطلاعات کاربر جاری که در SecurityContext ذخیره می‌شود.
 */
class CustomUserDetails(val user: User) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))

    override fun getPassword(): String = user.password

    /** شناسه لاگین = شماره موبایل */
    override fun getUsername(): String = user.phone

    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = user.enabled

    // ─── دسترسی سریع به اطلاعات کاربر ───
    val id: Long get() = user.id ?: 0L
    val role: Role get() = user.role
    val fullName: String get() = user.fullName
    val phone: String get() = user.phone

    /**
     * بررسی نقش.
     */
    fun hasRole(vararg roles: Role): Boolean = user.role in roles
}