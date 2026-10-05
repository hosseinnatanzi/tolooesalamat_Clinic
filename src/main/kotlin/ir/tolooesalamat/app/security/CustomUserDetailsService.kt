package ir.tolooesalamat.app.security

import ir.tolooesalamat.app.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * سرویس بارگذاری کاربر از دیتابیس.
 * جستجو با شماره موبایل (شناسه لاگین).
 */
@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    @Transactional(readOnly = true)
    override fun loadUserByUsername(username: String): UserDetails {
        // جستجو با phone (شناسه لاگین)
        val user = userRepository.findByPhone(username)
            ?: userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("کاربر '$username' یافت نشد")

        if (!user.enabled) {
            throw UsernameNotFoundException("حساب کاربری غیرفعال است")
        }

        return CustomUserDetails(user)
    }
}