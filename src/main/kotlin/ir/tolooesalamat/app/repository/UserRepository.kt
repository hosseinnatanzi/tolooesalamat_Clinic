package ir.tolooesalamat.app.repository

import ir.tolooesalamat.app.domain.Role
import ir.tolooesalamat.app.domain.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserRepository: JpaRepository<User, Long> {
    fun findByPhone(phone :String) :User?
    fun existsByPhone(phone :String) :Boolean
    fun findByUsername(username:String) :User?
    fun existsByUsername(username:String) :Boolean
    fun findByEmail(email:String) :User?
    fun existsByEmail(email:String) :Boolean
    fun findByNationalId(nationalId: String):User?

    fun findAllByRole(role: Role): List<User>
    fun findAllByRoleandEnabledTrue(role: Role): List<User>
    ///doctor role///
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

    /** پزشکان یک تخصص خاص */
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

    /** تمام بیماران فعال */
    @Query("""
        SELECT u FROM User u
        WHERE u.role = 'PATIENT'
        AND u.enabled = true
        ORDER BY u.createdAt DESC
    """)
    fun findAllActivePatients(): List<User>

    // ═══════════════════════════════════════════
    // 🔎 جستجوی پیشرفته
    // ═══════════════════════════════════════════

    /**
     * جستجو با کلمات کلیدی.
     * جستجو در: نام، نام خانوادگی، موبایل، ایمیل
     */
    @Query("""
        SELECT u FROM User u
        WHERE (:role IS NULL OR u.role = :role)
        AND (:enabled IS NULL OR u.enabled = :enabled)
        AND (
            :keyword IS NULL
            OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR u.phone LIKE CONCAT('%', :keyword, '%')
            OR u.email LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR u.nationalId LIKE CONCAT('%', :keyword, '%')
        )
        ORDER BY u.createdAt DESC
    """)
    fun searchUsers(
        @Param("role") role: Role?,
        @Param("enabled") enabled: Boolean?,
        @Param("keyword") keyword: String?,
        pageable: Pageable
    ): Page<User>

    // ═══════════════════════════════════════════
    // 📊 آمار
    // ═══════════════════════════════════════════

    /** تعداد کاربران فعال */
    fun countByEnabledTrue(): Long

    /** آخرین کاربران ثبت‌نام‌شده */
    @Query("""
        SELECT u FROM User u
        WHERE u.role = 'PATIENT'
        ORDER BY u.createdAt DESC
    """)
    fun findRecentPatients(pageable: Pageable): Page<User>

    // ═══════════════════════════════════════════
    // 🏥 بر اساس مطب
    // ═══════════════════════════════════════════

    /** کاربران یک مطب خاص */
    fun findAllByClinicId(clinicId: Long): List<User>

    /** کاربران فعال یک مطب */
    fun findAllByClinicIdAndEnabledTrue(clinicId: Long): List<User>


}