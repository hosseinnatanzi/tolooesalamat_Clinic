package ir.tolooesalamat.app.service

import ir.tolooesalamat.app.domain.*
import ir.tolooesalamat.app.domain.enum.Role
import ir.tolooesalamat.app.dto.*
import ir.tolooesalamat.app.exception.BusinessException
import ir.tolooesalamat.app.exception.DuplicateResourceException
import ir.tolooesalamat.app.exception.ResourceNotFoundException
import ir.tolooesalamat.app.mapper.DoctorProfileMapper
import ir.tolooesalamat.app.repository.DoctorProfileRepository
import ir.tolooesalamat.app.repository.SpecialtyRepository
import ir.tolooesalamat.app.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DoctorService(
    private val doctorProfileRepository: DoctorProfileRepository,
    private val userRepository: UserRepository,
    private val specialtyRepository: SpecialtyRepository,
    private val doctorMapper: DoctorProfileMapper,
    private val accessLogService: AccessLogService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Cacheable("active-doctors")
    fun findAllActive(): List<DoctorSummaryDto> =
        doctorProfileRepository.findAllActive().map { doctorMapper.toSummary(it) }

    fun findByUserId(userId: Long): DoctorProfileDto =
        doctorMapper.toDto(
            doctorProfileRepository.findByUserId(userId)
                ?: throw ResourceNotFoundException.of("پروفایل پزشک", userId)
        )

    fun findBySpecialty(specialtyId: Long): List<DoctorSummaryDto> =
        doctorProfileRepository.findActiveBySpecialtyId(specialtyId)
            .map { doctorMapper.toSummary(it) }

    @Transactional
    @CacheEvict(value = ["active-doctors"], allEntries = true)
    @PreAuthorize("hasRole('ADMIN')")
    fun createProfile(
        userId: Long,
        dto: DoctorProfileDto,
        currentUser: User
    ): DoctorProfileDto {

        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException.of("کاربر", userId) }

        if (user.role != Role.DOCTOR) {
            throw BusinessException("کاربر انتخابی پزشک نیست")
        }

        if (doctorProfileRepository.findByUserId(userId) != null) {
            throw DuplicateResourceException("این پزشک قبلاً پروفایل دارد")
        }

        if (doctorProfileRepository.existsByMedicalCode(dto.medicalCode)) {
            throw DuplicateResourceException.of("پزشک", "medicalCode", dto.medicalCode)
        }

        val specialty = specialtyRepository.findById(dto.specialtyId!!)
            .orElseThrow { ResourceNotFoundException.of("تخصص", dto.specialtyId) }

        val profile = DoctorProfile(
            user = user,
            specialty = specialty,
            medicalCode = dto.medicalCode,
            bio = dto.bio,
            yearsOfExperience = dto.yearsOfExperience,
            visitFee = dto.visitFee,
            defaultSessionDuration = dto.defaultSessionDuration,
            maxDailyAppointments = dto.maxDailyAppointments
        )

        val saved = doctorProfileRepository.save(profile)

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "DOCTOR_PROFILE",
            resourceId = saved.id,
            action = "CREATE",
            details = "پروفایل پزشک: ${user.phone}"
        )

        log.info("✅ پروفایل پزشک ساخته شد: ${user.phone}")
        return doctorMapper.toDto(saved)
    }

    @Transactional
    @CacheEvict(value = ["active-doctors"], allEntries = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    fun updateProfile(
        userId: Long,
        dto: DoctorProfileDto,
        currentUser: User
    ): DoctorProfileDto {

        val profile = doctorProfileRepository.findByUserId(userId)
            ?: throw ResourceNotFoundException.of("پروفایل پزشک", userId)

        // پزشک فقط پروفایل خودش
        if (currentUser.role == Role.DOCTOR && currentUser.id != userId) {
            throw ir.tolooesalamat.app.exception.AccessDeniedException(
                "شما نمی‌توانید پروفایل پزشک دیگری را ویرایش کنید"
            )
        }

        if (profile.medicalCode != dto.medicalCode &&
            doctorProfileRepository.existsByMedicalCode(dto.medicalCode)) {
            throw DuplicateResourceException.of("پزشک", "medicalCode", dto.medicalCode)
        }

        val specialty = specialtyRepository.findById(dto.specialtyId!!)
            .orElseThrow { ResourceNotFoundException.of("تخصص", dto.specialtyId) }

        doctorMapper.updateEntity(profile, dto, specialty)
        val saved = doctorProfileRepository.save(profile)

        return doctorMapper.toDto(saved)
    }
}