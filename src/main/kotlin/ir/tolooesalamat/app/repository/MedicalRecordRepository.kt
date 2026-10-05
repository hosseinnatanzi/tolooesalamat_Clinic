package ir.tolooesalamat.app.repository

import ir.tolooesalamat.app.domain.MedicalRecord
import ir.tolooesalamat.app.domain.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface MedicalRecordRepository : JpaRepository<MedicalRecord, Long> {

    // ═══════════════════════════════════════════
    // 🔍 جستجو‌های پایه
    // ═══════════════════════════════════════════

    /** تمام پرونده‌های یک بیمار (جدید → قدیم) */
    fun findAllByPatientOrderBySessionDateDesc(patient: User): List<MedicalRecord>

    /** تمام پرونده‌های یک پزشک (جدید → قدیم) */
    fun findAllByDoctorOrderBySessionDateDesc(doctor: User): List<MedicalRecord>

    /** پرونده‌های یک بیمار با Pagination */
    fun findAllByPatient(patient: User, pageable: Pageable): Page<MedicalRecord>

    // ═══════════════════════════════════════════
    // 📊 شمارش‌ها
    // ═══════════════════════════════════════════

    /** تعداد جلسات یک بیمار */
    fun countByPatient(patient: User): Long

    /** تعداد جلسات یک پزشک */
    fun countByDoctor(doctor: User): Long

    /** تعداد جلسات یک بیمار با یک پزشک خاص */
    fun countByPatientAndDoctor(patient: User, doctor: User): Long

    // ═══════════════════════════════════════════
    // 🎯 آخرین جلسه
    // ═══════════════════════════════════════════

    /** آخرین جلسه یک بیمار */
    @Query("""
        SELECT mr FROM MedicalRecord mr
        JOIN FETCH mr.doctor d
        WHERE mr.patient.id = :patientId
        ORDER BY mr.sessionDate DESC
    """)
    fun findLatestByPatient(
        @Param("patientId") patientId: Long,
        pageable: Pageable
    ): Page<MedicalRecord>

    /** آخرین شماره جلسه یک بیمار */
    @Query("""
        SELECT COALESCE(MAX(mr.sessionNumber), 0)
        FROM MedicalRecord mr
        WHERE mr.patient.id = :patientId
    """)
    fun getMaxSessionNumber(@Param("patientId") patientId: Long): Int

    // ═══════════════════════════════════════════
    // 🔎 جستجوی پیشرفته
    // ═══════════════════════════════════════════

    /**
     * جستجوی پرونده‌ها با فیلترهای متعدد.
     */
    @Query("""
        SELECT mr FROM MedicalRecord mr
        JOIN FETCH mr.patient p
        JOIN FETCH mr.doctor d
        WHERE (:patientId IS NULL OR p.id = :patientId)
        AND (:doctorId IS NULL OR d.id = :doctorId)
        AND (:from IS NULL OR mr.sessionDate >= :from)
        AND (:to IS NULL OR mr.sessionDate <= :to)
        ORDER BY mr.sessionDate DESC
    """)
    fun search(
        @Param("patientId") patientId: Long?,
        @Param("doctorId") doctorId: Long?,
        @Param("from") from: LocalDateTime?,
        @Param("to") to: LocalDateTime?,
        pageable: Pageable
    ): Page<MedicalRecord>

    // ═══════════════════════════════════════════
    // 📊 بررسی طول درمان
    // ═══════════════════════════════════════════

    /**
     * پرونده‌های یک بیمار در یک بازه زمانی.
     * برای بررسی روند طول درمان.
     */
    @Query("""
        SELECT mr FROM MedicalRecord mr
        JOIN FETCH mr.doctor d
        WHERE mr.patient.id = :patientId
        AND mr.sessionDate BETWEEN :from AND :to
        ORDER BY mr.sessionDate ASC
    """)
    fun findByPatientInRange(
        @Param("patientId") patientId: Long,
        @Param("from") from: LocalDateTime,
        @Param("to") to: LocalDateTime
    ): List<MedicalRecord>

    /** تمام جلسات تأیید‌شده یک بیمار */
    @Query("""
        SELECT mr FROM MedicalRecord mr
        WHERE mr.patient.id = :patientId
        AND mr.isFinalized = true
        ORDER BY mr.sessionDate DESC
    """)
    fun findFinalizedByPatient(@Param("patientId") patientId: Long): List<MedicalRecord>
}