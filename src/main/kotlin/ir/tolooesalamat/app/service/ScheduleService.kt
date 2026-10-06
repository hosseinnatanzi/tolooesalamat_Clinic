package ir.tolooesalamat.app.service

import ir.tolooesalamat.app.domain.User
import ir.tolooesalamat.app.domain.WeeklySchedule
import ir.tolooesalamat.app.domain.enum.Role
import ir.tolooesalamat.app.dto.WeeklyScheduleDto
import ir.tolooesalamat.app.exception.AccessDeniedException
import ir.tolooesalamat.app.exception.BusinessException
import ir.tolooesalamat.app.exception.DuplicateResourceException
import ir.tolooesalamat.app.exception.ResourceNotFoundException
import ir.tolooesalamat.app.mapper.WeeklyScheduleMapper
import ir.tolooesalamat.app.repository.DoctorProfileRepository
import ir.tolooesalamat.app.repository.WeeklyScheduleRepository
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalTime

@Service
@Transactional(readOnly = true)
class ScheduleService(
    private val scheduleRepository: WeeklyScheduleRepository,
    private val doctorProfileRepository: DoctorProfileRepository,
    private val scheduleMapper: WeeklyScheduleMapper,
    private val accessLogService: AccessLogService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    // ------------------------------------------------------------------ read

    fun getDoctorSchedule(doctorProfileId: Long): List<WeeklyScheduleDto> {
        // Fail fast if the doctor profile doesn't exist
        if (!doctorProfileRepository.existsById(doctorProfileId)) {
            throw ResourceNotFoundException.of("پروفایل پزشک", doctorProfileId)
        }
        return scheduleRepository.findAllByDoctorProfileId(doctorProfileId)
            .sortedBy { it.dayOfWeek.ordinal }
            .map { scheduleMapper.toDto(it) }
    }

    // ----------------------------------------------------------------- create

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    fun addSchedule(
        doctorProfileId: Long,
        dto: WeeklyScheduleDto,
        currentUser: User
    ): WeeklyScheduleDto {

        val profile = doctorProfileRepository.findById(doctorProfileId)
            .orElseThrow { ResourceNotFoundException.of("پروفایل پزشک", doctorProfileId) }

        ensureOwnership(profile.user.id, currentUser)
        validateTimeRange(dto)
        ensureNoDayConflict(doctorProfileId, dto.dayOfWeek!!)

        val schedule = scheduleMapper.toEntity(dto, profile)
        val saved = scheduleRepository.save(schedule)

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "SCHEDULE",
            resourceId = saved.id,
            action = "CREATE",
            details = "برنامه ${dto.dayOfWeek}: ${dto.startTime}-${dto.endTime}"
        )

        log.info("✅ برنامه پزشک اضافه شد: ${profile.user.phone} - ${dto.dayOfWeek}")
        return scheduleMapper.toDto(saved)
    }

    // ----------------------------------------------------------------- update

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    fun updateSchedule(
        scheduleId: Long,
        dto: WeeklyScheduleDto,
        currentUser: User
    ): WeeklyScheduleDto {

        val schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow { ResourceNotFoundException.of("برنامه", scheduleId) }

        ensureOwnership(schedule.doctorProfile.user.id, currentUser)
        validateTimeRange(dto)

        // If the day is being changed, make sure the new day is free for this doctor
        val newDay = dto.dayOfWeek!!
        if (newDay != schedule.dayOfWeek) {
            ensureNoDayConflict(schedule.doctorProfile.id, newDay)
        }

        scheduleMapper.updateEntity(schedule, dto)
        val saved = scheduleRepository.save(schedule)

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "SCHEDULE",
            resourceId = saved.id,
            action = "UPDATE",
            details = "برنامه ${dto.dayOfWeek}: ${dto.startTime}-${dto.endTime}"
        )

        return scheduleMapper.toDto(saved)
    }

    // ----------------------------------------------------------------- delete

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    fun deleteSchedule(scheduleId: Long, currentUser: User) {
        val schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow { ResourceNotFoundException.of("برنامه", scheduleId) }

        ensureOwnership(schedule.doctorProfile.user.id, currentUser)

        scheduleRepository.delete(schedule)

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "SCHEDULE",
            resourceId = scheduleId,
            action = "DELETE"
        )
    }

    // ------------------------------------------------------- replace all

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    fun replaceAllSchedules(
        doctorProfileId: Long,
        schedules: List<WeeklyScheduleDto>,
        currentUser: User
    ): List<WeeklyScheduleDto> {

        val profile = doctorProfileRepository.findById(doctorProfileId)
            .orElseThrow { ResourceNotFoundException.of("پروفایل پزشک", doctorProfileId) }

        ensureOwnership(profile.user.id, currentUser)

        // Validate every entry *before* deleting the old ones
        schedules.forEach { validateTimeRange(it) }
        ensureNoDuplicateDays(schedules)

        scheduleRepository.deleteAllByDoctorProfileId(doctorProfileId)

        val saved: List<WeeklySchedule> = schedules.map { dto ->
            scheduleRepository.save(scheduleMapper.toEntity(dto, profile))
        }

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "SCHEDULE",
            resourceId = doctorProfileId,
            action = "REPLACE_ALL",
            details = "${saved.size} برنامه جایگزین شد"
        )

        return saved.map { scheduleMapper.toDto(it) }
    }

    // ----------------------------------------------------------------- helpers

    private fun ensureOwnership(ownerUserId: Long?, currentUser: User) {
        if (currentUser.role == Role.DOCTOR && ownerUserId != currentUser.id) {
            throw AccessDeniedException("شما نمی‌توانید برنامه پزشک دیگری را تغییر دهید")
        }
    }

    private fun validateTimeRange(dto: WeeklyScheduleDto) {
        val start: LocalTime = dto.startTime
            ?: throw BusinessException("ساعت شروع الزامی است")
        val end: LocalTime = dto.endTime
            ?: throw BusinessException("ساعت پایان الزامی است")
        if (!start.isBefore(end)) {
            throw BusinessException("ساعت پایان باید بعد از ساعت شروع باشد")
        }
    }

    private fun ensureNoDayConflict(doctorProfileId: Long?, day: DayOfWeek) {
        if (scheduleRepository.existsByDoctorProfileIdAndDayOfWeek(doctorProfileId, day)) {
            throw DuplicateResourceException("برای روز $day قبلاً برنامه ثبت شده است")
        }
    }

    private fun ensureNoDuplicateDays(schedules: List<WeeklyScheduleDto>) {
        val days = schedules.map { it.dayOfWeek }
        val duplicates = days.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
        if (duplicates.isNotEmpty()) {
            throw DuplicateResourceException("روزهای تکراری در لیست: $duplicates")
        }
    }
}