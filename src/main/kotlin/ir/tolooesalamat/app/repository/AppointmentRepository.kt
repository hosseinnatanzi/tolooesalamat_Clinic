package ir.tolooesalamat.app.repository

import ir.tolooesalamat.app.domain.Appointment
import ir.tolooesalamat.app.domain.AppointmentStatus
import ir.tolooesalamat.app.domain.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalTime

@Repository
interface AppointmentRepository : JpaRepository<Appointment, Long> {

    // ═══════════════════════════════════════════
    // 🔍 جستجو‌های پایه
    // ═══════════════════════════════════════════

    /** نوبت‌های یک پزشک در یک تاریخ */
    fun findAllByDoctorAndDateOrderByStartTime(doctor: User, date: LocalDate): List<Appointment>

    /** نوبت‌های یک بیمار (مرتب‌شده از جدید) */
    fun findAllByPatientOrderByDateDescStartTimeDesc(patient: User): List<Appointment>

    /** نوبت‌های یک پزشک (مرتب‌شده از جدید) */
    fun findAllByDoctorOrderByDateDescStartTimeDesc(doctor: User): List<Appointment>

    /** بررسی وجود نوبت در یک زمان خاص */
    fun existsByDoctorAndDateAndStartTime(
        doctor: User,
        date: LocalDate,
        startTime: LocalTime
    ): Boolean

    // ═══════════════════════════════════════════
    // 📊 شمارش‌ها
    // ═══════════════════════════════════════════

    /** تعداد نوبت‌های یک پزشک در یک تاریخ */
    fun countByDoctorAndDate(doctor: User, date: LocalDate): Long

    /** تعداد نوبت‌های یک پزشک در یک تاریخ و وضعیت */
    fun countByDoctorAndDateAndStatus(
        doctor: User,
        date: LocalDate,
        status: AppointmentStatus
    ): Long

    /** تعداد نوبت‌های یک وضعیت در یک تاریخ */
    fun countByDateAndStatus(date: LocalDate, status: AppointmentStatus): Long

    /** تعداد نوبت‌های یک بیمار */
    fun countByPatient(patient: User): Long

    // ═══════════════════════════════════════════
    // 🎯 لیست‌های کاربردی
    // ═══════════════════════════════════════════

    /** نوبت‌های امروز یک پزشک (برای صف) */
    @Query("""
        SELECT a FROM Appointment a
        JOIN FETCH a.patient p
        WHERE a.doctor.id = :doctorId
        AND a.date = :date
        AND a.status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN', 'IN_PROGRESS')
        ORDER BY a.queueNumber ASC NULLS LAST, a.startTime ASC
    """)
    fun findTodayQueue(
        @Param("doctorId") doctorId: Long,
        @Param("date") date: LocalDate
    ): List<Appointment>

    /** آخرین شماره صف یک پزشک در یک تاریخ */
    @Query("""
        SELECT COALESCE(MAX(a.queueNumber), 0)
        FROM Appointment a
        WHERE a.doctor.id = :doctorId AND a.date = :date
    """)
    fun getMaxQueueNumber(
        @Param("doctorId") doctorId: Long,
        @Param("date") date: LocalDate
    ): Int

    /** نوبت‌های امروز تمام پزشکان (برای داشبورد منشی) */
    @Query("""
        SELECT a FROM Appointment a
        JOIN FETCH a.doctor d
        JOIN FETCH a.patient p
        WHERE a.date = :date
        ORDER BY a.startTime ASC
    """)
    fun findAllByDate(@Param("date") date: LocalDate): List<Appointment>

    // ═══════════════════════════════════════════
    // 🔎 جستجوی پیشرفته
    // ═══════════════════════════════════════════

    /**
     * جستجوی نوبت‌ها با فیلترهای متعدد.
     */
    @Query("""
        SELECT a FROM Appointment a
        JOIN FETCH a.doctor d
        JOIN FETCH a.patient p
        WHERE (:doctorId IS NULL OR d.id = :doctorId)
        AND (:patientId IS NULL OR p.id = :patientId)
        AND (:status IS NULL OR a.status = :status)
        AND (:from IS NULL OR a.date >= :from)
        AND (:to IS NULL OR a.date <= :to)
        ORDER BY a.date DESC, a.startTime ASC
    """)
    fun search(
        @Param("doctorId") doctorId: Long?,
        @Param("patientId") patientId: Long?,
        @Param("status") status: AppointmentStatus?,
        @Param("from") from: LocalDate?,
        @Param("to") to: LocalDate?,
        pageable: Pageable
    ): Page<Appointment>

    // ═══════════════════════════════════════════
    // 📊 آمار داشبورد
    // ═══════════════════════════════════════════

    /**
     * آمار روزانه یک پزشک.
     * برگردان: total, completed, cancelled, pending
     */
    @Query("""
        SELECT new map(
            COUNT(a) as total,
            SUM(CASE WHEN a.status = 'COMPLETED' THEN 1 ELSE 0 END) as completed,
            SUM(CASE WHEN a.status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelled,
            SUM(CASE WHEN a.status = 'PENDING' THEN 1 ELSE 0 END) as pending
        )
        FROM Appointment a
        WHERE a.doctor.id = :doctorId AND a.date = :date
    """)
    fun getDoctorDailyStats(
        @Param("doctorId") doctorId: Long,
        @Param("date") date: LocalDate
    ): Map<String, Any>

    /**
     * نوبت‌های آینده یک بیمار.
     */
    @Query("""
        SELECT a FROM Appointment a
        JOIN FETCH a.doctor d
        WHERE a.patient.id = :patientId
        AND a.date >= :today
        AND a.status IN ('PENDING', 'CONFIRMED')
        ORDER BY a.date ASC, a.startTime ASC
    """)
    fun findUpcomingByPatient(
        @Param("patientId") patientId: Long,
        @Param("today") today: LocalDate
    ): List<Appointment>

    /**
     * نوبت‌های آینده یک پزشک.
     */
    @Query("""
        SELECT a FROM Appointment a
        JOIN FETCH a.patient p
        WHERE a.doctor.id = :doctorId
        AND a.date >= :today
        AND a.status IN ('PENDING', 'CONFIRMED')
        ORDER BY a.date ASC, a.startTime ASC
    """)
    fun findUpcomingByDoctor(
        @Param("doctorId") doctorId: Long,
        @Param("today") today: LocalDate
    ): List<Appointment>

    // ═══════════════════════════════════════════
    // 🔒 بررسی تداخل (Object-Level)
    // ═══════════════════════════════════════════

    /**
     * بررسی تداخل زمانی برای یک پزشک.
     * (نوبت‌های لغو‌شده نادیده گرفته می‌شوند)
     */
    @Query("""
        SELECT COUNT(a) > 0 FROM Appointment a
        WHERE a.doctor.id = :doctorId
        AND a.date = :date
        AND a.status NOT IN ('CANCELLED', 'NO_SHOW')
        AND (
            (a.startTime < :endTime AND a.startTime >= :startTime)
            OR (a.startTime < :startTime 
                AND FUNCTION('TIMESTAMPADD', MINUTE, a.duration, a.startTime) > :startTime)
        )
    """)
    fun hasOverlap(
        @Param("doctorId") doctorId: Long,
        @Param("date") date: LocalDate,
        @Param("startTime") startTime: LocalTime,
        @Param("endTime") endTime: LocalTime
    ): Boolean
}