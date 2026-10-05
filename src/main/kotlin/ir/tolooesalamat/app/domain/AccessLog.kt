package ir.tolooesalamat.app.domain

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * ثبت دسترسی به داده‌های حساس (Audit Log).
 *
 * ⚠️ این جدول فقط Append-only است (فقط INSERT).
 * برای رعایت الزامات HIPAA/GDPR و بررسی‌های امنیتی ضروری است.
 *
 * ⚠️ از BaseEntity ارث نمی‌برد چون:
 *  - نیازی به created_at/updated_at ندارد (خودش accessed_at دارد)
 *  - نیازی به Soft Delete ندارد (لاگ حذف نمی‌شود)
 */
@Entity
@Table(
    name = "access_logs",
    indexes = [
        Index(name = "idx_log_user", columnList = "username"),
        Index(name = "idx_log_time", columnList = "accessed_at"),
        Index(name = "idx_log_resource", columnList = "resource_type, resource_id")
    ]
)
class AccessLog(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    /** شناسه کاربر (ممکن است بعداً حذف شود، پس فقط ID ذخیره می‌شود) */
    @Column(name = "user_id")
    var userId: Long? = null,

    /** نام کاربری — برای حفظ تاریخچه حتی پس از حذف کاربر */
    @Column(name = "username", nullable = false, length = 50)
    var username: String = "",

    /**
     * نوع منبع دسترسی.
     * مقادیر: USER, APPOINTMENT, MEDICAL_RECORD, PATIENT, DOCTOR
     */
    @Column(name = "resource_type", nullable = false, length = 50)
    var resourceType: String = "",

    @Column(name = "resource_id")
    var resourceId: Long? = null,

    /**
     * نوع عملیات.
     * مقادیر: VIEW, CREATE, UPDATE, DELETE, EXPORT, LOGIN, LOGOUT
     */
    @Column(name = "action", nullable = false, length = 20)
    var action: String = "",

    /** آدرس IP کاربر */
    @Column(name = "ip_address", length = 45)
    var ipAddress: String? = null,

    /** User Agent (مرورگر/دستگاه) */
    @Column(name = "user_agent", length = 500)
    var userAgent: String? = null,

    /** زمان دسترسی */
    @Column(name = "accessed_at", nullable = false, updatable = false)
    var accessedAt: LocalDateTime = LocalDateTime.now(),

    /** آیا دسترسی موفق بود؟ */
    @Column(name = "success", nullable = false)
    var success: Boolean = true,

    /** توضیحات اضافی */
    @Column(name = "details", columnDefinition = "TEXT")
    var details: String? = null

) {

    override fun toString(): String =
        "AccessLog(id=$id, user='$username', action='$action', " +
                "resource='$resourceType:$resourceId', success=$success)"

    companion object {
        const val ACTION_VIEW = "VIEW"
        const val ACTION_CREATE = "CREATE"
        const val ACTION_UPDATE = "UPDATE"
        const val ACTION_DELETE = "DELETE"
        const val ACTION_EXPORT = "EXPORT"
        const val ACTION_LOGIN = "LOGIN"
        const val ACTION_LOGOUT = "LOGOUT"

        const val RESOURCE_USER = "USER"
        const val RESOURCE_APPOINTMENT = "APPOINTMENT"
        const val RESOURCE_MEDICAL_RECORD = "MEDICAL_RECORD"
        const val RESOURCE_PATIENT = "PATIENT"
        const val RESOURCE_DOCTOR = "DOCTOR"
    }
}