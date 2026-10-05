package ir.tolooesalamat.app.service

import ir.tolooesalamat.app.domain.DoctorProfile
import ir.tolooesalamat.app.domain.Role
import ir.tolooesalamat.app.domain.User
import ir.tolooesalamat.app.domain.WeeklySchedule
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

@Service
@Transactional(readOnly = true)
class ScheduleService(
    private val scheduleRepository: WeeklyScheduleRepository,
    private val doctorProfileRepository: DoctorProfileRepository,
    private val scheduleMapper: WeeklyScheduleMapper,
    private val accessLogService: AccessLogService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun getDoctorSchedule(doctorProfileId: Long): List<WeeklyScheduleDto> =
        scheduleRepository.findAllByDoctorProfileId(doctorProfileId)
            .sortedBy { it.dayOfWeek.ordinal }
            .map { scheduleMapper.toDto(it) }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    fun addSchedule(
        doctorProfileId: Long,
        dto: WeeklyScheduleDto,
        currentUser: User
    ): WeeklyScheduleDto {

        val profile = doctorProfileRepository.findById(doctorProfileId)
            .orElseThrow { ResourceNotFoundException.of("پروفایل پزشک", doctorProfileId) }

        // پزشک فقط برنامه خودش
        if (currentUser.role == Role.DOCTOR && profile.user.id != currentUser.id) {
            throw AccessDeniedException("شما نمی‌توانید برنامه پزشک دیگری را تنظیم کنید")
        }

        // بررسی تداخل
        if (scheduleRepository.existsByDoctorProfileIdAndDayOfWeek(
                doctorProfileId, dto.dayOfWeek!!
            )
        ) {
            throw DuplicateResourceException(
                "برای روز ${dto.dayOfWeek} قبلاً برنامه ثبت شده است"
            )
        }

        // اعتبارسنجی زمان
        if (!dto.startTime!!.isBefore(dto.endTime!!)) {
            throw BusinessException("ساعت پایان باید بعد از ساعت شروع باشد")
        }

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

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    fun updateSchedule(
        scheduleId: Long,
        dto: WeeklyScheduleDto,
        currentUser: User
    ): WeeklyScheduleDto {

        val schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow { ResourceNotFoundException.of("برنامه", scheduleId) }

        if (currentUser.role == Role.DOCTOR &&
            schedule.doctorProfile.user.id != currentUser.id) {
            throw AccessDeniedException("دسترسی غیرمجاز")
        }

        if (!dto.startTime!!.isBefore(dto.endTime!!)) {
            throw BusinessException("ساعت پایان باید بعد از ساعت شروع باشد")
        }

        scheduleMapper.updateEntity(schedule, dto)
        val saved = scheduleRepository.save(schedule)

        return scheduleMapper.toDto(saved)
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    fun deleteSchedule(scheduleId: Long, currentUser: User) {
        val schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow { ResourceNotFoundException.of("برنامه", scheduleId) }

        if (currentUser.role == Role.DOCTOR &&
            schedule.doctorProfile.user.id != currentUser.id) {
            throw AccessDeniedException("دسترسی غیرمجاز")
        }

        scheduleRepository.delete(schedule)

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "SCHEDULE",
            resourceId = scheduleId,
            action = "DELETE"
        )
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    fun replaceAllSchedules(
        doctorProfileId: Long,
        schedules: List<WeeklyScheduleDto>,
        currentUser: User
    ): List<WeeklyScheduleDto> {

        val profile = doctorProfileRepository.findById(doctorProfileId)
            .orElseThrow { ResourceNotFoundException.of("پروفایل پزشک", doctorProfileId) }

        if (currentUser.role == Role.DOCTOR && profile.user.id != currentUser.id) {
            throw AccessDeniedException("دسترسی غیرمجاز")
        }

        scheduleRepository.deleteAllByDoctorProfileId(doctorProfileId)

        val saved = schedules.map { dto ->
            scheduleRepository.save(scheduleMapper.toEntity(dto, profile))
        }

        return saved.map { scheduleMapper.toDto(it) }
    }
}