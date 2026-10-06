package ir.tolooesalamat.app.service

import ir.tolooesalamat.app.domain.*
import ir.tolooesalamat.app.domain.enum.Role
import ir.tolooesalamat.app.dto.*
import ir.tolooesalamat.app.exception.AccessDeniedException
import ir.tolooesalamat.app.exception.BusinessException
import ir.tolooesalamat.app.exception.ResourceNotFoundException
import ir.tolooesalamat.app.mapper.AppointmentMapper
import ir.tolooesalamat.app.repository.AppointmentRepository
import ir.tolooesalamat.app.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class AppointmentService(
    private val appointmentRepository: AppointmentRepository,
    private val userRepository: UserRepository,
    private val appointmentMapper: AppointmentMapper,
    private val accessLogService: AccessLogService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    // ═══════════════════════════════════════════
    // 📋 لیست‌ها
    // ═══════════════════════════════════════════

    fun search(request: AppointmentSearchRequest): PagedResponse<AppointmentSummaryDto> {
        val pageable = PageRequest.of(
            request.page.coerceAtLeast(0),
            request.size.coerceIn(1, 100),
            Sort.by(Sort.Direction.DESC, "date", "startTime")
        )

        val page = appointmentRepository.search(
            doctorId = request.doctorId,
            patientId = request.patientId,
            status = request.status,
            from = request.fromDate,
            to = request.toDate,
            pageable = pageable
        )

        return PagedResponse(
            content = page.content.map { appointmentMapper.toSummary(it) },
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

    fun findByUser(currentUser: User): List<AppointmentSummaryDto> {
        val appointments = when (currentUser.role) {
            Role.ADMIN, Role.RECEPTIONIST ->
                appointmentRepository.findAll(
                    Sort.by(Sort.Direction.DESC, "date", "startTime")
                )
            Role.DOCTOR ->
                appointmentRepository.findAllByDoctorOrderByDateDescStartTimeDesc(currentUser)
            Role.PATIENT ->
                appointmentRepository.findAllByPatientOrderByDateDescStartTimeDesc(currentUser)
        }
        return appointments.map { appointmentMapper.toSummary(it) }
    }

    fun getTodayQueue(doctorId: Long, date: LocalDate = LocalDate.now()): List<AppointmentSummaryDto> =
        appointmentRepository.findTodayQueue(doctorId, date)
            .map { appointmentMapper.toSummary(it) }

    fun getById(id: Long, currentUser: User): AppointmentDto =
        appointmentMapper.toDto(findWithPermission(id, currentUser))

    // ═══════════════════════════════════════════
    // ➕ ثبت
    // ═══════════════════════════════════════════

    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun create(currentUser: User, dto: AppointmentDto): AppointmentDto {

        val doctor = userRepository.findById(dto.doctorId!!)
            .orElseThrow { ResourceNotFoundException.of("پزشک", dto.doctorId) }

        if (doctor.role != Role.DOCTOR) {
            throw BusinessException("کاربر انتخابی پزشک نیست")
        }

        val patient = if (currentUser.role == Role.PATIENT) currentUser
        else userRepository.findById(dto.patientId ?: 0L)
            .orElseThrow { ResourceNotFoundException("بیمار یافت نشد") }

        if (patient.role != Role.PATIENT) {
            throw BusinessException("کاربر انتخابی بیمار نیست")
        }

        // بررسی تداخل زمانی
        val endTime = dto.startTime!!.plusMinutes(dto.duration.toLong())
        if (appointmentRepository.hasOverlap(
                doctorId = doctor.id!!,
                date = dto.date!!,
                startTime = dto.startTime,
                endTime = endTime
            )
        ) {
            throw BusinessException("این زمان قبلاً رزرو شده است")
        }

        // بررسی ظرفیت روزانه
        val doctorProfile = doctor.doctorProfile
        if (doctorProfile != null) {
            val todayCount = appointmentRepository.countByDoctorAndDate(doctor, dto.date)
            if (todayCount >= doctorProfile.maxDailyAppointments) {
                throw BusinessException("ظرفیت نوبت‌های این روز تکمیل است")
            }
        }

        // شماره صف
        val queueNumber = appointmentRepository
            .getMaxQueueNumber(doctor.id!!, dto.date) + 1

        val appointment = Appointment(
            doctor = doctor,
            patient = patient,
            date = dto.date,
            startTime = dto.startTime,
            duration = dto.duration,
            status = AppointmentStatus.PENDING,
            sessionType = dto.sessionType,
            queueNumber = queueNumber,
            notes = dto.notes
        )

        val saved = appointmentRepository.save(appointment)

        log.info("✅ نوبت: ${patient.phone} → ${doctor.phone} - ${dto.date} ${dto.startTime}")

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "APPOINTMENT",
            resourceId = saved.id,
            action = "CREATE",
            details = "نوبت بیمار ${patient.username} - شماره صف $queueNumber"
        )

        return appointmentMapper.toDto(saved)
    }

    // ═══════════════════════════════════════════
    // 🔄 تغییر وضعیت
    // ═══════════════════════════════════════════

    @Transactional
    fun confirm(id: Long, currentUser: User): AppointmentDto {
        val appointment = findWithPermission(id, currentUser)
        appointment.status = AppointmentStatus.CONFIRMED
        val saved = appointmentRepository.save(appointment)

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "APPOINTMENT",
            resourceId = id,
            action = "CONFIRM"
        )

        return appointmentMapper.toDto(saved)
    }

    @Transactional
    fun checkIn(id: Long, currentUser: User): AppointmentDto {
        val appointment = findWithPermission(id, currentUser)

        if (appointment.status !in listOf(
                AppointmentStatus.PENDING,
                AppointmentStatus.CONFIRMED
            )
        ) {
            throw BusinessException("این نوبت در وضعیت قابل پذیرش نیست")
        }

        appointment.status = AppointmentStatus.CHECKED_IN
        appointment.checkedInAt = LocalDateTime.now()
        return appointmentMapper.toDto(appointmentRepository.save(appointment))
    }

    @Transactional
    fun startVisit(id: Long, currentUser: User): AppointmentDto {
        val appointment = findWithPermission(id, currentUser)
        appointment.status = AppointmentStatus.IN_PROGRESS
        appointment.startedAt = LocalDateTime.now()
        return appointmentMapper.toDto(appointmentRepository.save(appointment))
    }

    @Transactional
    fun completeVisit(id: Long, currentUser: User): AppointmentDto {
        val appointment = findWithPermission(id, currentUser)
        appointment.status = AppointmentStatus.COMPLETED
        appointment.completedAt = LocalDateTime.now()
        return appointmentMapper.toDto(appointmentRepository.save(appointment))
    }

    @Transactional
    fun cancel(id: Long, reason: String?, currentUser: User): AppointmentDto {
        val appointment = findWithPermission(id, currentUser)

        if (appointment.status == AppointmentStatus.COMPLETED) {
            throw BusinessException("نوبت انجام‌شده قابل لغو نیست")
        }

        appointment.status = AppointmentStatus.CANCELLED
        appointment.notes = (appointment.notes ?: "") + "\n[لغو: ${reason ?: "بدون دلیل"}]"
        val saved = appointmentRepository.save(appointment)

        accessLogService.logAccess(
            user = currentUser,
            resourceType = "APPOINTMENT",
            resourceId = id,
            action = "CANCEL",
            details = "دلیل: ${reason ?: "نامشخص"}"
        )

        return appointmentMapper.toDto(saved)
    }

    @Transactional
    fun markNoShow(id: Long, currentUser: User): AppointmentDto {
        val appointment = findWithPermission(id, currentUser)
        appointment.status = AppointmentStatus.NO_SHOW
        return appointmentMapper.toDto(appointmentRepository.save(appointment))
    }

    // ═══════════════════════════════════════════
    // 🕐 اسلات‌های آزاد
    // ═══════════════════════════════════════════

    fun findAvailableSlots(doctorId: Long, date: LocalDate): List<AvailableSlotDto> {

        val doctor = userRepository.findById(doctorId)
            .orElseThrow { ResourceNotFoundException.of("پزشک", doctorId) }

        val profile = doctor.doctorProfile ?: return emptyList()

        val schedule = profile.weeklySchedules
            .firstOrNull { it.dayOfWeek == date.dayOfWeek }
            ?: return emptyList()

        val booked = appointmentRepository
            .findAllByDoctorAndDateOrderByStartTime(doctor, date)
            .filter { it.status != AppointmentStatus.CANCELLED }
            .map { it.startTime to it.endTime }

        val slots = mutableListOf<AvailableSlotDto>()
        var current = schedule.startTime
        val slotDuration = schedule.slotDuration

        while (current.plusMinutes(slotDuration.toLong()) <= schedule.endTime) {
            val slotEnd = current.plusMinutes(slotDuration.toLong())

            val hasConflict = booked.any { (bookedStart, bookedEnd) ->
                current < bookedEnd && slotEnd > bookedStart
            }

            if (!hasConflict) {
                slots.add(AvailableSlotDto(startTime = current, endTime = slotEnd))
            }

            current = slotEnd
        }

        return slots
    }

    // ═══════════════════════════════════════════
    // 📊 آمار
    // ═══════════════════════════════════════════

    fun getDoctorDailyStats(doctorId: Long, date: LocalDate): Map<String, Any> =
        appointmentRepository.getDoctorDailyStats(doctorId, date)

    // ═══════════════════════════════════════════
    // 🛠 متد کمکی
    // ═══════════════════════════════════════════

    private fun findWithPermission(id: Long, currentUser: User): Appointment {
        val appointment = appointmentRepository.findById(id)
            .orElseThrow { ResourceNotFoundException.of("نوبت", id) }

        val hasAccess = when (currentUser.role) {
            Role.ADMIN, Role.RECEPTIONIST -> true
            Role.DOCTOR -> appointment.doctor.id == currentUser.id
            Role.PATIENT -> appointment.patient.id == currentUser.id
        }

        if (!hasAccess) {
            accessLogService.logAccess(
                user = currentUser,
                resourceType = "APPOINTMENT",
                resourceId = id,
                action = "VIEW",
                success = false,
                details = "دسترسی غیرمجاز"
            )
            throw AccessDeniedException("شما به این نوبت دسترسی ندارید")
        }

        return appointment
    }
}