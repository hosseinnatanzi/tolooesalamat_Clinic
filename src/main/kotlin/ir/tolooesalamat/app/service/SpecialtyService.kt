package ir.tolooesalamat.app.service

import ir.tolooesalamat.app.domain.Specialty
import ir.tolooesalamat.app.domain.User
import ir.tolooesalamat.app.dto.SpecialtyDto
import ir.tolooesalamat.app.dto.SpecialtySummaryDto
import ir.tolooesalamat.app.exception.DuplicateResourceException
import ir.tolooesalamat.app.exception.ResourceNotFoundException
import ir.tolooesalamat.app.mapper.SpecialtyMapper
import ir.tolooesalamat.app.repository.SpecialtyRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class SpecialtyService(
    private val specialtyRepository: SpecialtyRepository,
    private val specialtyMapper: SpecialtyMapper,
    private val accessLogService: AccessLogService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /** کش می‌شود چون به‌ندرت تغییر می‌کند. */
    @Cacheable("specialties")
    fun findAllActive(): List<SpecialtySummaryDto> =
        specialtyRepository.findAllByActiveTrue().map { specialtyMapper.toSummary(it) }

    @PreAuthorize("hasRole('ADMIN')")
    fun findAll(): List<SpecialtyDto> =
        specialtyRepository.findAll().map { specialtyMapper.toDto(it) }

    fun findById(id: Long): SpecialtyDto =
        specialtyMapper.toDto(
            specialtyRepository.findById(id)
                .orElseThrow { ResourceNotFoundException.of("تخصص", id) }
        )

    @Transactional
    @CacheEvict(value = ["specialties"], allEntries = true)
    @PreAuthorize("hasRole('ADMIN')")
    fun create(dto: SpecialtyDto, currentUser: User): SpecialtyDto {
        if (specialtyRepository.existsByName(dto.name)) {
            throw DuplicateResourceException.of("تخصص", "name", dto.name)
        }
        if (dto.code != null && specialtyRepository.existsByCode(dto.code)) {
            throw DuplicateResourceException.of("تخصص", "code", dto.code)
        }

        val specialty = specialtyMapper.toEntity(dto)
        val saved = specialtyRepository.save(specialty)

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "SPECIALTY",
            resourceId = saved.id,
            action = "CREATE",
            details = "ایجاد تخصص: ${saved.name}"
        )

        log.info("✅ تخصص جدید: ${saved.name}")
        return specialtyMapper.toDto(saved)
    }

    @Transactional
    @CacheEvict(value = ["specialties"], allEntries = true)
    @PreAuthorize("hasRole('ADMIN')")
    fun update(id: Long, dto: SpecialtyDto, currentUser: User): SpecialtyDto {
        val specialty = specialtyRepository.findById(id)
            .orElseThrow { ResourceNotFoundException.of("تخصص", id) }

        if (specialty.name != dto.name && specialtyRepository.existsByName(dto.name)) {
            throw DuplicateResourceException.of("تخصص", "name", dto.name)
        }

        specialtyMapper.updateEntity(specialty, dto)
        val saved = specialtyRepository.save(specialty)

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "SPECIALTY",
            resourceId = saved.id,
            action = "UPDATE"
        )

        return specialtyMapper.toDto(saved)
    }

    @Transactional
    @CacheEvict(value = ["specialties"], allEntries = true)
    @PreAuthorize("hasRole('ADMIN')")
    fun toggleActive(id: Long, currentUser: User): SpecialtyDto {
        val specialty = specialtyRepository.findById(id)
            .orElseThrow { ResourceNotFoundException.of("تخصص", id) }

        specialty.active = !specialty.active
        val saved = specialtyRepository.save(specialty)

        return specialtyMapper.toDto(saved)
    }
}