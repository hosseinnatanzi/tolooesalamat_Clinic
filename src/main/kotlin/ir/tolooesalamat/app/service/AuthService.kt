package ir.tolooesalamat.app.service

import ir.tolooesalamat.app.domain.Gender
import ir.tolooesalamat.app.domain.PatientProfile
import ir.tolooesalamat.app.domain.Role
import ir.tolooesalamat.app.domain.User
import ir.tolooesalamat.app.dto.AuthResponse
import ir.tolooesalamat.app.dto.LoginRequest
import ir.tolooesalamat.app.dto.PatientRegisterRequest
import ir.tolooesalamat.app.dto.RefreshTokenRequest
import ir.tolooesalamat.app.exception.BusinessException
import ir.tolooesalamat.app.exception.DuplicateResourceException
import ir.tolooesalamat.app.exception.ResourceNotFoundException
import ir.tolooesalamat.app.mapper.UserMapper
import ir.tolooesalamat.app.repository.PatientProfileRepository
import ir.tolooesalamat.app.repository.UserRepository
import ir.tolooesalamat.app.security.jwt.JwtService
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val patientProfileRepository: PatientProfileRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val userMapper: UserMapper,
    private val accessLogService: AccessLogService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    // ═══════════════════════════════════════════
    // 📝 ثبت‌نام بیمار
    // ═══════════════════════════════════════════

    @Transactional
    fun registerPatient(request: PatientRegisterRequest): AuthResponse {

        if (request.password != request.confirmPassword) {
            throw BusinessException("رمز عبور و تکرار آن مطابقت ندارند")
        }

        if (userRepository.existsByPhone(request.phone)) {
            throw DuplicateResourceException.of("کاربر", "phone", request.phone)
        }

        val autoUsername = generateUsername(request.phone, Role.PATIENT)
        val hashedPassword = passwordEncoder.encode(request.password)

        val user = User(
            phone = request.phone,
            landline = request.landline?.takeIf { it.isNotBlank() },
            password = hashedPassword,
            firstName = request.firstName.trim(),
            lastName = request.lastName.trim(),
            age = request.age,
            gender = request.gender ?: Gender.FEMALE,
            username = autoUsername,
            role = Role.PATIENT,
            enabled = true
        )
        val savedUser = userRepository.save(user)

        val fileNumber = generateFileNumber()
        patientProfileRepository.save(
            PatientProfile(
                user = savedUser,
                fileNumber = fileNumber,
                gender = request.gender,
                fileStatus = "ACTIVE"
            )
        )

        log.info("✅ ثبت‌نام بیمار: ${savedUser.phone} - پرونده: $fileNumber")

        accessLogService.logAccess(
            user = savedUser,
            resourceType = "USER",
            resourceId = savedUser.id,
            action = "REGISTER",
            details = "شماره پرونده: $fileNumber"
        )

        return login(LoginRequest(phone = request.phone, password = request.password))
    }

    // ═══════════════════════════════════════════
    // 🔐 ورود
    // ═══════════════════════════════════════════

    @Transactional(readOnly = true)
    fun login(request: LoginRequest): AuthResponse {

        val user = userRepository.findByPhone(request.phone)
            ?: throw BadCredentialsException("شماره موبایل یا رمز عبور اشتباه است")

        if (!user.enabled) {
            throw DisabledException("حساب کاربری غیرفعال است")
        }

        try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(user.phone, request.password)
            )
        } catch (ex: BadCredentialsException) {
            log.warn("❌ ورود ناموفق: ${request.phone}")

            accessLogService.logAccess(
                user = user,
                resourceType = "AUTH",
                resourceId = user.id,
                action = "LOGIN",
                success = false,
                details = "رمز عبور اشتباه"
            )

            throw BusinessException("شماره موبایل یا رمز عبور اشتباه است")
        }

        val accessToken = jwtService.generateAccessToken(user)
        val refreshToken = jwtService.generateRefreshToken(user)

        log.info("✅ ورود موفق: ${user.phone}")

        accessLogService.logAccess(
            user = user,
            resourceType = "AUTH",
            resourceId = user.id,
            action = "LOGIN",
            success = true
        )

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = jwtService.getExpirationSeconds(),
            user = userMapper.toSummary(user)
        )
    }

    // ═══════════════════════════════════════════
    // 🔄 تازه‌سازی
    // ═══════════════════════════════════════════

    @Transactional(readOnly = true)
    fun refresh(request: RefreshTokenRequest): AuthResponse {

        if (!jwtService.isRefreshToken(request.refreshToken)) {
            throw BusinessException("Refresh Token نامعتبر است")
        }

        val phone = jwtService.extractPhone(request.refreshToken)
            ?: throw BusinessException("توکن نامعتبر است")

        val user = userRepository.findByPhone(phone)
            ?: throw ResourceNotFoundException.of("کاربر", "phone", phone)

        if (!user.enabled) {
            throw DisabledException("حساب کاربری غیرفعال است")
        }

        return AuthResponse(
            accessToken = jwtService.generateAccessToken(user),
            refreshToken = jwtService.generateRefreshToken(user),
            expiresIn = jwtService.getExpirationSeconds(),
            user = userMapper.toSummary(user)
        )
    }

    // ═══════════════════════════════════════════
    // 🚪 خروج
    // ═══════════════════════════════════════════

    fun logout(currentUser: User) {
        accessLogService.logAccess(
            user = currentUser,
            resourceType = "AUTH",
            resourceId = currentUser.id,
            action = "LOGOUT"
        )
        log.info("✅ خروج: ${currentUser.phone}")
    }

    // ═══════════════════════════════════════════
    // 🛠 متدهای کمکی
    // ═══════════════════════════════════════════

    private fun generateUsername(phone: String, role: Role): String = when (role) {
        Role.PATIENT -> "user_$phone"
        Role.DOCTOR -> "doctor_$phone"
        Role.RECEPTIONIST -> "recep_$phone"
        Role.ADMIN -> "admin_$phone"
    }

    private fun generateFileNumber(): String {
        val year = LocalDate.now().year
        val prefix = "P-$year-"
        val max = patientProfileRepository.getMaxFileNumber(prefix)
        return "$prefix${(max + 1).toString().padStart(5, '0')}"
    }
}