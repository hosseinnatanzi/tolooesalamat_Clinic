package ir.tolooesalamat.app.repository

import ir.tolooesalamat.app.domain.Appointment
import ir.tolooesalamat.app.domain.User
import ir.tolooesalamat.app.domain.enum.AppointmentStatus
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
    // 🔍 جستجوهای پایه
    // ═══════════════════════════════════════════

    fun findAllByDoctorAndDateOrderByStartTime(
        doctor: User,
        date: LocalDate
    ): List<Appointment>

    fun findAllByDoctorOrderByDateDescStartTimeDesc(doctor: User): List<Appointment>

    fun findAllByPatientOrderByDateDescStartTimeDesc(patient: User): List<Appointment>

    fun existsByDoctorAndDateAndStartTime(
        doctor: User,
        date: LocalDate,
        startTime: LocalTime
    ): Boolean

    fun countByDoctorAndDate(doctor: User, date: LocalDate): Long

    // ═══════════════════════════════════════════
    // 📋 صف انتظار امروز
    // ═══════════════════════════════════════════

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

    // ═══════════════════════════════════════════
    // 🔢 آخرین شماره صف
    // ═══════════════════════════════════════════

    @Query("""
        SELECT COALESCE(MAX(a.queueNumber), 0) FROM Appointment a
        WHERE a.doctor.id = :doctorId AND a.date = :date
    """)
    fun getMaxQueueNumber(
        @Param("doctorId") doctorId: Long,
        @Param("date") date: LocalDate
    ): Int

    // ═══════════════════════════════════════════
    // 🔎 جستجوی پیشرفته
    // ═══════════════════════════════════════════

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
    // ⚠️ بررسی تداخل
    // ═══════════════════════════════════════════

    @Query("""
        SELECT COUNT(a) > 0 FROM Appointment a
        WHERE a.doctor.id = :doctorId
        AND a.date = :date
        AND a.status NOT IN ('CANCELLED', 'NO_SHOW')
        AND a.startTime = :startTime
    """)
    fun hasOverlap(
        @Param("doctorId") doctorId: Long,
        @Param("date") date: LocalDate,
        @Param("startTime") startTime: LocalTime,
        @Param("endTime") endTime: LocalTime
    ): Boolean

    // ═══════════════════════════════════════════
    // 📊 آمار روزانه
    // ═══════════════════════════════════════════

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
}