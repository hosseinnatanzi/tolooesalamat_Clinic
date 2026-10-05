package ir.tolooesalamat.app.domain

import jakarta.persistence.*

/**
 * تعریف یک تست روانشناسی.
 *
 * مثال‌ها:
 *  - MMPI-2 (پرسشنامه شخصیت چندوجهی مینه‌سوتا)
 *  - BDI-II (پرسشنامه افسردگی بک)
 *  - BAI (پرسشنامه اضطراب بک)
 *  - SCL-90-R (پرسشنامه ۹۰ سوالی)
 *  - Raven (آزمون هوش)
 */
@Entity
@Table(
    name = "psychological_tests",
    indexes = [
        Index(name = "idx_test_name", columnList = "name", unique = true),
        Index(name = "idx_test_code", columnList = "code", unique = true),
        Index(name = "idx_test_active", columnList = "active")
    ]
)
class PsychologicalTest(

    // ═══════════════════════════════════════════
    // 📋 اطلاعات تست
    // ═══════════════════════════════════════════

    /** نام کامل تست */
    @Column(name = "name", nullable = false, unique = true, length = 100)
    var name: String = "",

    /**
     * کد اختصاری تست — یکتا و اختیاری.
     * مثال: MMPI2, BDI2, BAI, SCL90, RAVEN
     */
    @Column(name = "code", unique = true, length = 50)
    var code: String? = null,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,

    // ═══════════════════════════════════════════
    // 📊 مشخصات فنی
    // ═══════════════════════════════════════════

    /** تعداد سوالات تست */
    @Column(name = "total_questions")
    var totalQuestions: Int = 0,

    /** زمان تقریبی انجام (دقیقه) */
    @Column(name = "estimated_minutes")
    var estimatedMinutes: Int = 0,

    /**
     * دسته‌بندی تست.
     * مثال: PERSONALITY, DEPRESSION, ANXIETY, INTELLIGENCE
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 30)
    var category: TestCategory? = null,

    // ═══════════════════════════════════════════
    // 🎯 وضعیت
    // ═══════════════════════════════════════════

    @Column(name = "active", nullable = false)
    var active: Boolean = true

) : BaseEntity() {

    override fun toString(): String =
        "PsychologicalTest(id=$id, name='$name', code='$code')"
}

/**
 * دسته‌بندی تست‌های روانشناسی.
 */
enum class TestCategory(val label: String) {
    PERSONALITY("شخصیت"),
    DEPRESSION("افسردگی"),
    ANXIETY("اضطراب"),
    INTELLIGENCE("هوش"),
    MEMORY("حافظه"),
    ATTENTION("توجه و تمرکز"),
    CHILD("کودک و نوجوان"),
    OTHER("سایر");

    companion object {
        fun fromString(value: String?): TestCategory? =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}