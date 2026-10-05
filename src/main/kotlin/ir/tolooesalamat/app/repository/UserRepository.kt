package ir.tolooesalamat.app.repository

import ir.tolooesalamat.app.domain.Role
import ir.tolooesalamat.app.domain.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    // ═══════════════════════════════════════════
    // 🔍 Derived Queries
    // ═══════════════════════════════════════════

    fun findByPhone(phone: String): User?
    fun existsByPhone(phone: String): Boolean

    fun findByUsername(username: String): User?
    fun existsByUsername(username: String): Boolean

    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean

    fun findByNationalId(nationalId: String): User?

    // ═══════════════════════════════════════════
    // 🎯 نقش
    // ═══════════════════════════════════════════

    fun findAllByRole(role: Role): List<User>
    fun findAllByRoleAndEnabledTrue(role: Role): List<User>
    fun findAllByRoleAndEnabledFalse(role: Role): List<User>
    fun countByRole(role: Role): Long
    fun countByRoleAndEnabledTrue(role: Role): Long

    // ═══════════════════════════════════════════
    // 👨‍⚕️ پزشکان
    // ═══════════════════════════════════════════

    @Query("""
        SELECT DISTINCT u FROM User u
        LEFT JOIN FETCH u.doctorProfile dp
        LEFT JOIN FETCH dp.specialty s
        WHERE u.role = 'DOCTOR'
        AND u.enabled = true
        AND s.active = true
        ORDER BY u.lastName, u.firstName
    """)
    fun findAllActiveDoctors(): List<User>

    @Query("""
        SELECT DISTINCT u FROM User u
        JOIN u.doctorProfile dp
        WHERE u.role = 'DOCTOR'
        AND u.enabled = true
        AND dp.specialty.id = :specialtyId
        ORDER BY u.lastName, u.firstName
    """)
    fun findDoctorsBySpecialty(@Param("specialtyId") specialtyId: Long): List<User>

    // ═══════════════════════════════════════════
    // 🧑 بیماران
    // ═══════════════════════════════════════════

    @Query("""
        SELECT u FROM User u
        WHERE u.role = 'PATIENT'
        AND u.enabled = true
        ORDER BY u.createdAt DESC
    """)
    fun findAllActivePatients(): List<User>

    @Query("""
        SELECT u FROM User u
        WHERE u.role = 'PATIENT'
        ORDER BY u.createdAt DESC
    """)
    fun findRecentPatients(pageable: Pageable): Page<User>

    // ═══════════════════════════════════════════
    // 🏥 مطب
    // ═══════════════════════════════════════════

    fun findAllByClinicId(clinicId: Long): List<User>
    fun findAllByClinicIdAndEnabledTrue(clinicId: Long): List<User>
    fun findAllByClinicId(clinicId: Long, pageable: Pageable): Page<User>
    fun findAllByClinicIdAndRole(clinicId: Long, role: Role): List<User>

    // ═══════════════════════════════════════════
    // 📊 آمار
    // ═══════════════════════════════════════════

    fun countByEnabledTrue(): Long
    fun countByEnabledFalse(): Long

    @Query("SELECT COUNT(u) FROM User u WHERE u.isDeleted = false")
    fun countAllActive(): Long
}