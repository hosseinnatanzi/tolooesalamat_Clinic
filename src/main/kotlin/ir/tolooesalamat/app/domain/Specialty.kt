package ir.tolooesalamat.app.domain

import jakarta.persistence.*

/**
 * تخصص پزشکی (Master Data).
 *
 * مثال:
 *  - روان‌پزشکی
 *  - روان‌شناسی بالینی
 *  - مشاوره خانواده
 *  - روان‌شناسی کودک
 */
@Entity
@Table(
    name = "specialties",
    indexes = [
        Index(name = "idx_specialty_name", columnList = "name", unique = true),
        Index(name = "idx_specialty_code", columnList = "code", unique = true),
        Index(name = "idx_specialty_active", columnList = "active")
    ]
)
class Specialty(

    /** نام تخصص — یکتا و اجباری */
    @Column(
        name = "name",
        nullable = false,
        unique = true,
        length = 100
    )
    var name: String = "",

    /**
     * کد اختصاری تخصص — یکتا و اختیاری.
     * مثال: PSYCHIATRY, CLINICAL_PSY, FAMILY, CHILD
     */
    @Column(
        name = "code",
        unique = true,
        length = 50
    )
    var code: String? = null,

    @Column(
        name = "description",
        columnDefinition = "TEXT"
    )
    var description: String? = null,

    /**
     * وضعیت فعال بودن تخصص.
     * - true  → قابل استفاده در فرم‌ها
     * - false → غیرفعال شده (تاریخ حفظ می‌شود)
     */
    @Column(name = "active", nullable = false)
    var active: Boolean = true

) : BaseEntity() {

    override fun toString(): String =
        "Specialty(id=$id, name='$name', code='$code', active=$active)"
}
