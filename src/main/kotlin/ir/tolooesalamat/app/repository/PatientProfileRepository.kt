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
    // 🔍 جستجو
    // ═══════════════════════════════════════════

    /** پیدا کردن با User ID */
    fun findByUserId(userId: Long): PatientProfile?

    /** پیدا کردن با شماره پرونده */
    fun findByFileNumber(fileNumber: String): PatientProfile?

    /** بررسی وجود شماره پرونده */
    fun existsByFileNumber(fileNumber: String): Boolean

    // ═══════════════════════════════════════════
    // 🎯 لیست بیماران
    // ═══════════════════════════════════════════

    /** بیماران یک پزشک خاص */
    fun findAllByPrimaryDoctorOrderByCreatedAtDesc(primaryDoctor: User): List<PatientProfile>

    /** بیماران یک پزشک با Pagination */
    fun findAllByPrimaryDoctor(primaryDoctor: User, pageable: Pageable): Page<PatientProfile>

    /** بیماران فعال (وضعیت ACTIVE) */
    @Query("""
        SELECT pp FROM PatientProfile pp
        JOIN FETCH pp.user u
        WHERE pp.fileStatus = 'ACTIVE'
        AND u.enabled = true
        ORDER BY pp.createdAt DESC
    """)
    fun findAllActive(): List<PatientProfile>

    /** تمام بیماران یک وضعیت */
    fun findAllByFileStatus(fileStatus: String): List<PatientProfile>

    // ═══════════════════════════════════════════
    // 🔎 جستجوی پیشرفته
    // ═══════════════════════════════════════════

    /**
     * جستجوی بیماران با کلمات کلیدی.
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

    /**
     * گرفتن آخرین شماره پرونده برای تولید شماره بعدی.
     * مثال: P-2024-00042 → 42
     */
    @Query("""
        SELECT COALESCE(MAX(CAST(SUBSTRING(pp.fileNumber, 4) AS integer)), 0)
        FROM PatientProfile pp
        WHERE pp.fileNumber LIKE CONCAT(:prefix, '%')
    """)
    fun getMaxFileNumber(@Param("prefix") prefix: String): Int

    // ═══════════════════════════════════════════
    // 📊 آمار
    // ═══════════════════════════════════════════

    /** شمارش بیماران یک پزشک */
    fun countByPrimaryDoctor(primaryDoctor: User): Long

    /** شمارش بیماران فعال */
    fun countByFileStatus(fileStatus: String): Long

    /** شمارش بیماران یک پزشک با یک وضعیت */
    fun countByPrimaryDoctorAndFileStatus(primaryDoctor: User, fileStatus: String): Long
}