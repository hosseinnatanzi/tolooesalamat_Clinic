package ir.tolooesalamat.app.repository

import ir.tolooesalamat.app.domain.AccessLog
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface AccessLogRepository : JpaRepository<AccessLog, Long> {

    // ═══════════════════════════════════════════
    // 🔍 جستجو‌های پایه
    // ═══════════════════════════════════════════

    /** لاگ‌های یک کاربر (جدید → قدیم) */
    fun findAllByUsernameOrderByAccessedAtDesc(username: String): List<AccessLog>

    /** لاگ‌های یک کاربر با Pagination */
    fun findAllByUsername(username: String, pageable: Pageable): Page<AccessLog>

    /** لاگ‌های یک منبع خاص */
    fun findAllByResourceTypeAndResourceId(
        resourceType: String,
        resourceId: Long
    ): List<AccessLog>

    /** لاگ‌های یک عملیات */
    fun findAllByAction(action: String, pageable: Pageable): Page<AccessLog>

    // ═══════════════════════════════════════════
    // 🔎 جستجوی پیشرفته
    // ═══════════════════════════════════════════

    /**
     * جستجوی لاگ‌ها با فیلترهای متعدد.
     */
    @Query("""
        SELECT al FROM AccessLog al
        WHERE (:username IS NULL OR al.username = :username)
        AND (:resourceType IS NULL OR al.resourceType = :resourceType)
        AND (:resourceId IS NULL OR al.resourceId = :resourceId)
        AND (:action IS NULL OR al.action = :action)
        AND (:success IS NULL OR al.success = :success)
        AND (:from IS NULL OR al.accessedAt >= :from)
        AND (:to IS NULL OR al.accessedAt <= :to)
        ORDER BY al.accessedAt DESC
    """)
    fun searchLogs(
        @Param("username") username: String?,
        @Param("resourceType") resourceType: String?,
        @Param("resourceId") resourceId: Long?,
        @Param("action") action: String?,
        @Param("success") success: Boolean?,
        @Param("from") from: LocalDateTime?,
        @Param("to") to: LocalDateTime?,
        pageable: Pageable
    ): Page<AccessLog>

    // ═══════════════════════════════════════════
    // 🚨 لاگ‌های امنیتی
    // ═══════════════════════════════════════════

    /** لاگ‌های ناموفق (برای بررسی نفوذ) */
    @Query("""
        SELECT al FROM AccessLog al
        WHERE al.success = false
        AND al.accessedAt >= :since
        ORDER BY al.accessedAt DESC
    """)
    fun findFailedAccesses(
        @Param("since") since: LocalDateTime,
        pageable: Pageable
    ): Page<AccessLog>

    /** تلاش‌های ورود ناموفق یک کاربر */
    @Query("""
        SELECT al FROM AccessLog al
        WHERE al.username = :username
        AND al.action = 'LOGIN'
        AND al.success = false
        AND al.accessedAt >= :since
        ORDER BY al.accessedAt DESC
    """)
    fun findFailedLogins(
        @Param("username") username: String,
        @Param("since") since: LocalDateTime
    ): List<AccessLog>

    /** شمارش تلاش‌های ورود ناموفق در یک بازه */
    @Query("""
        SELECT COUNT(al) FROM AccessLog al
        WHERE al.username = :username
        AND al.action = 'LOGIN'
        AND al.success = false
        AND al.accessedAt >= :since
    """)
    fun countFailedLogins(
        @Param("username") username: String,
        @Param("since") since: LocalDateTime
    ): Long

    // ═══════════════════════════════════════════
    // 📊 آمار
    // ═══════════════════════════════════════════

    /** تعداد لاگ‌ها در یک بازه */
    fun countByAccessedAtBetween(from: LocalDateTime, to: LocalDateTime): Long

    /** آخرین لاگ‌های یک منبع */
    @Query("""
        SELECT al FROM AccessLog al
        WHERE al.resourceType = :resourceType
        AND al.resourceId = :resourceId
        ORDER BY al.accessedAt DESC
    """)
    fun findLatestByResource(
        @Param("resourceType") resourceType: String,
        @Param("resourceId") resourceId: Long,
        pageable: Pageable
    ): Page<AccessLog>

    /** پاک‌سازی لاگ‌های قدیمی (نگهداری ۹۰ روز) */
    @Query("DELETE FROM AccessLog al WHERE al.accessedAt < :before")
    @org.springframework.data.jpa.repository.Modifying
    fun deleteOldLogs(@Param("before") before: LocalDateTime): Int
}