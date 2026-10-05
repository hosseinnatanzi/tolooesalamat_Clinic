package ir.tolooesalamat.app.repository

import ir.tolooesalamat.app.domain.DoctorProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface DoctorProfileRepository : JpaRepository<DoctorProfile, Long> {

    // ═══════════════════════════════════════════
    // 🔍 جستجو
    // ═══════════════════════════════════════════

    /** پیدا کردن پروفایل با User ID */
    fun findByUserId(userId: Long): DoctorProfile?

    /** پیدا کردن با کد نظام پزشکی */
    fun findByMedicalCode(medicalCode: String): DoctorProfile?

    /** بررسی وجود کد نظام پزشکی */
    fun existsByMedicalCode(medicalCode: String): Boolean

    // ═══════════════════════════════════════════
    // 🎯 لیست پزشکان
    // ═══════════════════════════════════════════

    /** تمام پزشکان فعال */
    @Query("""
        SELECT dp FROM DoctorProfile dp
        JOIN FETCH dp.user u
        JOIN FETCH dp.specialty s
        WHERE u.enabled = true
        AND s.active = true
        ORDER BY u.lastName, u.firstName
    """)
    fun findAllActive(): List<DoctorProfile>

    /** پزشکان یک تخصص خاص */
    @Query("""
        SELECT dp FROM DoctorProfile dp
        JOIN FETCH dp.user u
        JOIN FETCH dp.specialty s
        WHERE s.id = :specialtyId
        AND u.enabled = true
        AND s.active = true
        ORDER BY u.lastName, u.firstName
    """)
    fun findActiveBySpecialtyId(@Param("specialtyId") specialtyId: Long): List<DoctorProfile>

    /** شمارش پزشکان یک تخصص */
    fun countBySpecialtyId(specialtyId: Long): Long
}