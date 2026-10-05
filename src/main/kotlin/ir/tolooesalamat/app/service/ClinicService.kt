package ir.tolooesalamat.app.service

import ir.tolooesalamat.app.domain.Clinic
import ir.tolooesalamat.app.domain.User
import ir.tolooesalamat.app.dto.ClinicDto
import ir.tolooesalamat.app.dto.ClinicSummaryDto
import ir.tolooesalamat.app.exception.BusinessException
import ir.tolooesalamat.app.exception.DuplicateResourceException
import ir.tolooesalamat.app.exception.ResourceNotFoundException
import ir.tolooesalamat.app.mapper.ClinicMapper
import ir.tolooesalamat.app.repository.ClinicRepository
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ClinicService(
    private val clinicRepository: ClinicRepository,
    private val clinicMapper: ClinicMapper,
    private val accessLogService: AccessLogService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun findAll(): List<ClinicDto> =
        clinicRepository.findAll().map { clinicMapper.toDto(it) }

    fun findAllActive(): List<ClinicSummaryDto> =
        clinicRepository.findAllByActiveTrue().map { clinicMapper.toSummary(it) }

    fun findById(id: Long): ClinicDto =
        clinicMapper.toDto(
            clinicRepository.findById(id)
                .orElseThrow { ResourceNotFoundException.of("مطب", id) }
        )

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    fun create(dto: ClinicDto, currentUser: User): ClinicDto {
        if (clinicRepository.existsByName(dto.name)) {
            throw DuplicateResourceException.of("مطب", "name", dto.name)
        }

        val clinic = clinicMapper.toEntity(dto)
        val saved = clinicRepository.save(clinic)

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "CLINIC",
            resourceId = saved.id,
            action = "CREATE",
            details = "ایجاد مطب: ${saved.name}"
        )

        log.info("✅ مطب جدید: ${saved.name}")
        return clinicMapper.toDto(saved)
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    fun update(id: Long, dto: ClinicDto, currentUser: User): ClinicDto {
        val clinic = clinicRepository.findById(id)
            .orElseThrow { ResourceNotFoundException.of("مطب", id) }

        if (clinic.name != dto.name && clinicRepository.existsByName(dto.name)) {
            throw DuplicateResourceException.of("مطب", "name", dto.name)
        }

        clinicMapper.updateEntity(clinic, dto)
        val saved = clinicRepository.save(clinic)

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "CLINIC",
            resourceId = saved.id,
            action = "UPDATE"
        )

        return clinicMapper.toDto(saved)
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    fun toggleActive(id: Long, currentUser: User): ClinicDto {
        val clinic = clinicRepository.findById(id)
            .orElseThrow { ResourceNotFoundException.of("مطب", id) }

        clinic.active = !clinic.active
        val saved = clinicRepository.save(clinic)

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "CLINIC",
            resourceId = saved.id,
            action = if (saved.active) "ENABLE" else "DISABLE"
        )

        return clinicMapper.toDto(saved)
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(id: Long, currentUser: User) {
        val clinic = clinicRepository.findById(id)
            .orElseThrow { ResourceNotFoundException.of("مطب", id) }

        clinic.isDeleted = true
        clinic.active = false
        clinicRepository.save(clinic)

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "CLINIC",
            resourceId = id,
            action = "DELETE"
        )
    }
}