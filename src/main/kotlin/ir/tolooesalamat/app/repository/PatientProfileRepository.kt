package ir.tolooesalamat.app.repository

import ir.tolooesalamat.app.domain.PatientProfile
import ir.tolooesalamat.app.domain.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface PatientProfileRepository : JpaRepository<PatientProfile, Long> {

    // ═══════════════════════════════════════════
    // 🔍 جستجوهای پایه
    // ═══════════════════════════════════════════

    /** پیدا کردن با User ID */
    fun findByUserId(userId: Long): PatientProfile?

    /** پیدا کردن با شماره پرونده */
    fun findByFileNumber(fileNumber: String): PatientProfile?

    /** بررسی وجود شماره پرونده */
    fun existsByFileNumber(fileNumber: String): Boolean

    // ═══════════════════════════════════════════
    // 📋 بر اساس پزشک
    // ═══════════════════════════════════════════

    /** بیماران یک پزشک خاص — مرتب‌شده بر اساس تاریخ */
    fun findAllByPrimaryDoctorOrderByCreatedAtDesc(doctor: User): List<PatientProfile>

    /** بیماران یک پزشک با Pagination */
    fun findAllByPrimaryDoctor(doctor: User, pageable: Pageable): Page<PatientProfile>

    // ═══════════════════════════════════════════
    // 🔎 جستجوی پیشرفته
    // ═══════════════════════════════════════════

    /**
     * جستجوی بیماران با فیلترهای متعدد.
     *
     * جستجو در: نام، نام خانوادگی، موبایل، شماره پرونده
     */
    @Query("""
        SELECT pp FROM PatientProfile pp
        JOIN FETCH pp.user u
        WHERE u.enabled = true
        AND (
            :keyword IS NULL
            OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR u.phone LIKE CONCAT('%', :keyword, '%')
            OR pp.fileNumber LIKE CONCAT('%', :keyword, '%')
        )
        AND (:doctorId IS NULL OR pp.primaryDoctor.id = :doctorId)
        AND (:fileStatus IS NULL OR pp.fileStatus = :fileStatus)
        ORDER BY pp.createdAt DESC
    """)
    fun searchPatients(
        @Param("keyword") keyword: String?,
        @Param("doctorId") doctorId: Long?,
        @Param("fileStatus") fileStatus: String?,
        pageable: Pageable
    ): Page<PatientProfile>

    // ═══════════════════════════════════════════
    // 📊 تولید شماره پرونده
    // ═══════════════════════════════════════════

    @Query("""
        SELECT COALESCE(MAX(CAST(SUBSTRING(pp.fileNumber, 4) AS integer)), 0)
        FROM PatientProfile pp
        WHERE pp.fileNumber LIKE CONCAT(:prefix, '%')
    """)
    fun getMaxFileNumber(@Param("prefix") prefix: String): Int

    // ═══════════════════════════════════════════
    // 📊 آمار
    // ═══════════════════════════════════════════

    fun countByPrimaryDoctor(doctor: User): Long
    fun countByFileStatus(fileStatus: String): Long
}