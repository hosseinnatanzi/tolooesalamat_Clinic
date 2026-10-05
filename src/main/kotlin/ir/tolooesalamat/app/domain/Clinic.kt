package ir.tolooesalamat.app.domain

import jakarta.persistence.*

/**
 * مطب / کلینیک.
 *
 * در صورت داشتن چند شعبه، هر User به یک Clinic متصل می‌شود.
 */
@Entity
@Table(
    name = "clinics",
    indexes = [
        Index(name = "idx_clinic_name", columnList = "name", unique = true),
        Index(name = "idx_clinic_mobile", columnList = "mobile", unique = true)
    ]
)
class Clinic(

    /** نام مطب — یکتا و اجباری */
    @Column(
        name = "name",
        nullable = false,
        unique = true,
        length = 100
    )
    var name: String = "",

    @Column(
        name = "address",
        columnDefinition = "TEXT"
    )
    var address: String? = null,

    // ═══════════════════════════════════════════
    // 📱 شماره موبایل — یکتا (برای پیامک)
    // ═══════════════════════════════════════════

    /**
     * شماره موبایل مطب.
     * فرمت: 09XXXXXXXXX (۱۱ رقم)
     * برای ارسال پیامک و احراز هویت.
     */
    @Column(
        name = "mobile",
        unique = true,
        length = 11
    )
    var mobile: String? = null,

    // ═══════════════════════════════════════════
    // ☎️ تلفن ثابت — می‌تواند تکراری باشد
    // ═══════════════════════════════════════════

    /**
     * تلفن ثابت مطب.
     * فرمت: 021XXXXXXXX یا 021XXXXXXX
     * برای تماس مشتریان.
     */
    @Column(
        name = "landline",
        length = 15
    )
    var landline: String? = null,

    // ═══════════════════════════════════════════

    @Column(
        name = "email",
        length = 100
    )
    var email: String? = null,

    @Column(name = "active", nullable = false)
    var active: Boolean = true

) : BaseEntity() {

    @get:Transient
    val contactInfo: String
        get() = listOfNotNull(mobile, landline).joinToString(" / ").ifBlank { "—" }

    override fun toString(): String =
        "Clinic(id=$id, name='$name')"
}