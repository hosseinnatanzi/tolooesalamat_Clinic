package ir.tolooesalamat.app.repository

import ir.tolooesalamat.app.domain.TestResult
import ir.tolooesalamat.app.domain.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface TestResultRepository : JpaRepository<TestResult, Long> {

    // ═══════════════════════════════════════════
    // 🔍 جستجو‌های پایه
    // ═══════════════════════════════════════════

    /** تمام تست‌های یک بیمار (جدید → قدیم) */
    fun findAllByPatientOrderByTestDateDesc(patient: User): List<TestResult>

    /** تمام تست‌های ثبت‌شده توسط یک پزشک */
    fun findAllByDoctorOrderByTestDateDesc(doctor: User): List<TestResult>

    /** تست‌های یک بیمار با یک نوع تست خاص */
    fun findAllByPatientAndTestId(patient: User, testId: Long): List<TestResult>

    /** تست‌های یک بیمار با Pagination */
    fun findAllByPatient(patient: User, pageable: Pageable): Page<TestResult>

    // ═══════════════════════════════════════════
    // 📊 شمارش‌ها
    // ═══════════════════════════════════════════

    /** تعداد تست‌های یک بیمار */
    fun countByPatient(patient: User): Long

    /** تعداد تست‌های یک پزشک */
    fun countByDoctor(doctor: User): Long

    /** تعداد تست‌های یک بیمار با یک تست خاص */
    fun countByPatientAndTestId(patient: User, testId: Long): Long

    /** تعداد تست‌های یک پزشک در یک بازه */
    @Query("""
        SELECT COUNT(r) FROM TestResult r
        WHERE r.doctor.id = :doctorId
        AND r.testDate BETWEEN :from AND :to
    """)
    fun countByDoctorInRange(
        @Param("doctorId") doctorId: Long,
        @Param("from") from: LocalDateTime,
        @Param("to") to: LocalDateTime
    ): Long

    // ═══════════════════════════════════════════
    // 📊 بررسی طول درمان
    // ═══════════════════════════════════════════

    /**
     * تست‌های یک بیمار در یک بازه زمانی.
     * برای بررسی روند طول درمان.
     */
    @Query("""
        SELECT r FROM TestResult r
        JOIN FETCH r.test t
        JOIN FETCH r.doctor d
        WHERE r.patient.id = :patientId
        AND r.testDate BETWEEN :from AND :to
        ORDER BY r.testDate DESC
    """)
    fun findPatientResultsInRange(
        @Param("patientId") patientId: Long,
        @Param("from") from: LocalDateTime,
        @Param("to") to: LocalDateTime
    ): List<TestResult>

    /**
     * آخرین N نتیجه یک بیمار.
     * برای بررسی سریع روند.
     */
    @Query("""
        SELECT r FROM TestResult r
        JOIN FETCH r.test t
        JOIN FETCH r.doctor d
        WHERE r.patient.id = :patientId
        ORDER BY r.testDate DESC
    """)
    fun findLatestResults(
        @Param("patientId") patientId: Long,
        pageable: Pageable
    ): Page<TestResult>

    // ═══════════════════════════════════════════
    // 🔎 جستجوی پیشرفته
    // ═══════════════════════════════════════════

    /**
     * جستجوی تست‌ها با فیلترهای متعدد.
     */
    @Query("""
        SELECT r FROM TestResult r
        JOIN FETCH r.patient p
        JOIN FETCH r.doctor d
        JOIN FETCH r.test t
        WHERE (:patientId IS NULL OR p.id = :patientId)
        AND (:doctorId IS NULL OR d.id = :doctorId)
        AND (:testId IS NULL OR t.id = :testId)
        AND (:from IS NULL OR r.testDate >= :from)
        AND (:to IS NULL OR r.testDate <= :to)
        ORDER BY r.testDate DESC
    """)
    fun search(
        @Param("patientId") patientId: Long?,
        @Param("doctorId") doctorId: Long?,
        @Param("testId") testId: Long?,
        @Param("from") from: LocalDateTime?,
        @Param("to") to: LocalDateTime?,
        pageable: Pageable
    ): Page<TestResult>

    // ═══════════════════════════════════════════
    // 🔏 نهایی‌شده‌ها
    // ═══════════════════════════════════════════

    /** تست‌های نهایی‌شده یک بیمار */
    @Query("""
        SELECT r FROM TestResult r
        WHERE r.patient.id = :patientId
        AND r.doctorSignature IS NOT NULL
        AND r.diagnosis IS NOT NULL
        ORDER BY r.testDate DESC
    """)
    fun findFinalizedByPatient(@Param("patientId") patientId: Long): List<TestResult>

    /** تست‌های با سطح محرمانگی بالا */
    @Query("""
        SELECT r FROM TestResult r
        JOIN FETCH r.patient p
        JOIN FETCH r.test t
        WHERE r.confidentialityLevel = 'HIGHLY_CONFIDENTIAL'
        AND r.patient.id = :patientId
        ORDER BY r.testDate DESC
    """)
    fun findHighlyConfidentialByPatient(@Param("patientId") patientId: Long): List<TestResult>
}