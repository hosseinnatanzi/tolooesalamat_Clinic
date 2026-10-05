package ir.tolooesalamat.app.service

import ir.tolooesalamat.app.domain.PatientProfile
import ir.tolooesalamat.app.domain.Role
import ir.tolooesalamat.app.domain.User
import ir.tolooesalamat.app.dto.PagedResponse
import ir.tolooesalamat.app.dto.PatientProfileDto
import ir.tolooesalamat.app.dto.PatientSearchRequest
import ir.tolooesalamat.app.dto.PatientSummaryDto
import ir.tolooesalamat.app.exception.AccessDeniedException
import ir.tolooesalamat.app.exception.ResourceNotFoundException
import ir.tolooesalamat.app.mapper.PatientProfileMapper
import ir.tolooesalamat.app.repository.PatientProfileRepository
import ir.tolooesalamat.app.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PatientService(
    private val patientProfileRepository: PatientProfileRepository,
    private val userRepository: UserRepository,
    private val patientMapper: PatientProfileMapper,
    private val accessLogService: AccessLogService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun getByUserId(userId: Long, currentUser: User): PatientProfileDto {
        val profile = patientProfileRepository.findByUserId(userId)
            ?: throw ResourceNotFoundException.of("پروفایل بیمار", userId)

        // کنترل دسترسی
        val hasAccess = when (currentUser.role) {
            Role.ADMIN, Role.DOCTOR, Role.RECEPTIONIST -> true
            Role.PATIENT -> profile.user.id == currentUser.id
        }

        if (!hasAccess) {
            throw AccessDeniedException("شما به این پرونده دسترسی ندارید")
        }

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "PATIENT_PROFILE",
            resourceId = profile.id,
            action = "VIEW"
        )

        return patientMapper.toDto(profile)
    }

    fun getByFileNumber(fileNumber: String): PatientProfileDto =
        patientMapper.toDto(
            patientProfileRepository.findByFileNumber(fileNumber)
                ?: throw ResourceNotFoundException.of("پرونده", "fileNumber", fileNumber)
        )

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    fun search(request: PatientSearchRequest): PagedResponse<PatientSummaryDto> {
        val pageable = PageRequest.of(
            request.page.coerceAtLeast(0),
            request.size.coerceIn(1, 100),
            Sort.by(Sort.Direction.DESC, "createdAt")
        )

        val page = patientProfileRepository.searchPatients(
            keyword = request.keyword,
            doctorId = request.doctorId,
            fileStatus = request.fileStatus,
            pageable = pageable
        )

        return PagedResponse(
            content = page.content.map { patientMapper.toSummary(it) },
            page = page.number,
            size = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            first = page.isFirst,
            last = page.isLast,
            hasNext = page.hasNext(),
            hasPrevious = page.hasPrevious()
        )
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    fun updateProfile(
        userId: Long,
        dto: PatientProfileDto,
        currentUser: User
    ): PatientProfileDto {

        val profile = patientProfileRepository.findByUserId(userId)
            ?: throw ResourceNotFoundException.of("پروفایل بیمار", userId)

        patientMapper.updateEntity(profile, dto)
        val saved = patientProfileRepository.save(profile)

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "PATIENT_PROFILE",
            resourceId = saved.id,
            action = "UPDATE"
        )

        return patientMapper.toDto(saved)
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    fun changeFileStatus(
        userId: Long,
        newStatus: String,
        currentUser: User
    ): PatientProfileDto {

        val profile = patientProfileRepository.findByUserId(userId)
            ?: throw ResourceNotFoundException.of("پروفایل بیمار", userId)

        profile.fileStatus = newStatus
        val saved = patientProfileRepository.save(profile)

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "PATIENT_PROFILE",
            resourceId = saved.id,
            action = "CHANGE_STATUS",
            details = "وضعیت: $newStatus"
        )

        log.info("✅ وضعیت پرونده ${profile.fileNumber} → $newStatus")
        return patientMapper.toDto(saved)
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    fun findByDoctor(doctorId: Long): List<PatientSummaryDto> {
        val doctor = userRepository.findById(doctorId)
            .orElseThrow { ResourceNotFoundException.of("پزشک", doctorId) }

        return patientProfileRepository.findAllByPrimaryDoctorOrderByCreatedAtDesc(doctor)
            .map { patientMapper.toSummary(it) }
    }
}