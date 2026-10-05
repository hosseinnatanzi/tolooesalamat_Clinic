package ir.tolooesalamat.app.service

import ir.tolooesalamat.app.domain.Role
import ir.tolooesalamat.app.domain.User
import ir.tolooesalamat.app.dto.ChangeUserRoleRequest
import ir.tolooesalamat.app.dto.PagedResponse
import ir.tolooesalamat.app.dto.UserDto
import ir.tolooesalamat.app.dto.UserSummaryDto
import ir.tolooesalamat.app.exception.AccessDeniedException
import ir.tolooesalamat.app.exception.BusinessException
import ir.tolooesalamat.app.exception.DuplicateResourceException
import ir.tolooesalamat.app.exception.ResourceNotFoundException
import ir.tolooesalamat.app.mapper.UserMapper
import ir.tolooesalamat.app.repository.UserRepository
import ir.tolooesalamat.app.repository.spec.UserSpecifications
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository,
    private val userMapper: UserMapper,
    private val passwordEncoder: PasswordEncoder,
    private val accessLogService: AccessLogService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    // ═══════════════════════════════════════════
    // 🔍 خواندن
    // ═══════════════════════════════════════════

    fun findById(id: Long): User =
        userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException.of("کاربر", id) }

    fun findByPhone(phone: String): User =
        userRepository.findByPhone(phone)
            ?: throw ResourceNotFoundException.of("کاربر", "phone", phone)

    fun getById(id: Long): UserDto = userMapper.toDto(findById(id))

    fun getByPhone(phone: String): UserDto = userMapper.toDto(findByPhone(phone))

    // ═══════════════════════════════════════════
    // 📋 جستجوی پیشرفته با Specification
    // ═══════════════════════════════════════════

    @PreAuthorize("hasRole('ADMIN')")
    fun searchUsers(
        role: Role?,
        enabled: Boolean?,
        keyword: String?,
        page: Int,
        size: Int
    ): PagedResponse<UserSummaryDto> {

        // ساخت Specification
        val spec: Specification<User> = Specification
            .where(UserSpecifications.isNotDeleted())
            .and(UserSpecifications.hasRole(role))
            .and(UserSpecifications.isEnabled(enabled))
            .and(UserSpecifications.matchesKeyword(keyword))

        // Pageable
        val pageable = PageRequest.of(
            page.coerceAtLeast(0),
            size.coerceIn(1, 100),
            Sort.by(Sort.Direction.DESC, "createdAt")
        )

        // 🎯 اینجا از findAll(spec, pageable) استفاده می‌کنیم
        val result = userRepository.findAll(spec, pageable)

        // تبدیل به DTO
        val summaries: List<UserSummaryDto> = result.content.map { userMapper.toSummary(it) }

        return PagedResponse(
            content = summaries,
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
            first = result.isFirst,
            last = result.isLast,
            hasNext = result.hasNext(),
            hasPrevious = result.hasPrevious()
        )
    }

    @PreAuthorize("hasRole('ADMIN')")
    fun findAllDoctors(): List<UserSummaryDto> =
        userRepository.findAllByRoleAndEnabledTrue(Role.DOCTOR)
            .map { userMapper.toSummary(it) }

    @PreAuthorize("hasRole('ADMIN')")
    fun findAllPatients(): List<UserSummaryDto> =
        userRepository.findAllByRoleAndEnabledTrue(Role.PATIENT)
            .map { userMapper.toSummary(it) }

    // ═══════════════════════════════════════════
    // ✏️ CRUD
    // ═══════════════════════════════════════════

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    fun createUser(dto: UserDto, currentUser: User): UserDto {
        if (userRepository.existsByPhone(dto.phone)) {
            throw DuplicateResourceException.of("کاربر", "phone", dto.phone)
        }

        if (dto.password.isNullOrBlank()) {
            throw BusinessException("رمز عبور الزامی است")
        }

        val encodedPassword = passwordEncoder.encode(dto.password)
        val user = userMapper.toEntity(dto, encodedPassword)
        val saved = userRepository.save(user)

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "USER",
            resourceId = saved.id,
            action = "CREATE",
            details = "ایجاد کاربر: ${saved.phone} (${saved.role})"
        )

        log.info("✅ کاربر جدید: ${saved.phone} - ${saved.role}")
        return userMapper.toDto(saved)
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    fun updateUser(id: Long, dto: UserDto, currentUser: User): UserDto {
        val user = findById(id)

        if (user.phone != dto.phone && userRepository.existsByPhone(dto.phone)) {
            throw DuplicateResourceException.of("کاربر", "phone", dto.phone)
        }

        userMapper.updateEntity(user, dto)
        val saved = userRepository.save(user)

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "USER",
            resourceId = saved.id,
            action = "UPDATE",
            details = "به‌روزرسانی: ${saved.phone}"
        )

        return userMapper.toDto(saved)
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    fun changeUserStatus(id: Long, enabled: Boolean, currentUser: User): UserDto {
        val user = findById(id)

        if (user.role == Role.ADMIN && !enabled) {
            throw BusinessException("غیرفعال کردن مدیر مجاز نیست")
        }

        user.enabled = enabled
        val saved = userRepository.save(user)

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "USER",
            resourceId = saved.id,
            action = if (enabled) "ENABLE" else "DISABLE"
        )

        return userMapper.toDto(saved)
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    fun changeUserRole(id: Long, request: ChangeUserRoleRequest, currentUser: User): UserDto {
        val user = findById(id)

        if (user.id == currentUser.id) {
            throw BusinessException("تغییر نقش خودتان مجاز نیست")
        }

        user.role = request.role
        val saved = userRepository.save(user)

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "USER",
            resourceId = saved.id,
            action = "CHANGE_ROLE",
            details = "نقش جدید: ${request.role.label}"
        )

        return userMapper.toDto(saved)
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteUser(id: Long, currentUser: User) {
        val user = findById(id)

        if (user.role == Role.ADMIN) {
            throw BusinessException("حذف مدیر مجاز نیست")
        }

        user.isDeleted = true
        user.enabled = false
        userRepository.save(user)

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "USER",
            resourceId = id,
            action = "DELETE",
            details = "حذف نرم: ${user.phone}"
        )
    }

    // ═══════════════════════════════════════════
    // 🔐 رمز عبور
    // ═══════════════════════════════════════════

    @Transactional
    fun changePassword(
        userId: Long,
        currentPassword: String,
        newPassword: String,
        currentUser: User
    ) {
        val user = findById(userId)

        if (user.id != currentUser.id && currentUser.role != Role.ADMIN) {
            throw AccessDeniedException("شما نمی‌توانید رمز کاربر دیگری را تغییر دهید")
        }

        if (!passwordEncoder.matches(currentPassword, user.password)) {
            throw BusinessException("رمز عبور فعلی اشتباه است")
        }

        user.password = passwordEncoder.encode(newPassword).toString()
        userRepository.save(user)

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "USER",
            resourceId = user.id,
            action = "CHANGE_PASSWORD"
        )

        log.info("✅ رمز عبور تغییر کرد: ${user.phone}")
    }
}