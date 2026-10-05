package ir.tolooesalamat.app.domain

import ir.tolooesalamat.app.config.crypto.converter.EncryptedStringConverter
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * نتیجه انجام یک تست روانشناسی توسط بیمار.
 *
 * ⚠️ این Entity شامل داده‌های فوق‌حساس است:
 *  - فقط پزشک معالج و بیمار حق دسترسی دارند
 *  - تمام دسترسی‌ها در AccessLog ثبت می‌شود
 *  - فیلدهای حساس با Hybrid Encryption (RSA + AES) رمزنگاری می‌شوند
 */
@Entity
@Table(
    name = "test_results",
    indexes = [
        Index(name = "idx_result_patient", columnList = "patient_id"),
        Index(name = "idx_result_doctor", columnList = "doctor_id"),
        Index(name = "idx_result_test", columnList = "test_id"),
        Index(name = "idx_result_date", columnList = "test_date"),
        Index(name = "idx_result_patient_date", columnList = "patient_id, test_date")
    ]
)
class TestResult(

    // ═══════════════════════════════════════════
    // 🔗 ارتباطات
    // ═══════════════════════════════════════════

    /** بیمار مورد ارزیابی */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "patient_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_result_patient")
    )
    var patient: User = User(),

    /** پزشک ارزیاب */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "doctor_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_result_doctor")
    )
    var doctor: User = User(),

    /** نوع تست انجام‌شده */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "test_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_result_test")
    )
    var test: PsychologicalTest = PsychologicalTest(),

    // ═══════════════════════════════════════════
    // 📅 زمان انجام
    // ═══════════════════════════════════════════

    @Column(name = "test_date", nullable = false)
    var testDate: LocalDateTime = LocalDateTime.now(),

    // ═══════════════════════════════════════════
    // 🔒 فیلدهای رمزنگاری‌شده
    // ═══════════════════════════════════════════

    /**
     * امتیاز خام (Raw Score).
     * در برخی تست‌ها محرمانه است.
     */
    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "raw_score_enc", columnDefinition = "TEXT")
    var rawScore: String? = null,

    /**
     * تفسیر نتایج.
     * بسیار حساس — فقط پزشک معالج می‌تواند بنویسد.
     */
    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "interpretation_enc", columnDefinition = "TEXT")
    var interpretation: String? = null,

    /**
     * تشخیص نهایی پزشک بر اساس تست.
     * بسیار حساس.
     */
    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "diagnosis_enc", columnDefinition = "TEXT")
    var diagnosis: String? = null,

    /**
     * داده‌های خام JSON (پاسخ‌ها به سوالات).
     * رمزنگاری‌شده ذخیره می‌شود.
     */
    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "raw_data_enc", columnDefinition = "TEXT")
    var rawData: String? = null,

    /**
     * توصیه‌های درمانی بر اساس نتیجه تست.
     */
    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "recommendations_enc", columnDefinition = "TEXT")
    var recommendations: String? = null,

    // ═══════════════════════════════════════════
    // 🔏 امضای دیجیتال
    // ═══════════════════════════════════════════

    /**
     * امضای دیجیتال پزشک روی نتایج.
     * SHA256withRSA — تأیید اصالت و عدم انکار.
     */
    @Column(name = "doctor_signature", columnDefinition = "TEXT")
    var doctorSignature: String? = null,

    // ═══════════════════════════════════════════
    // 🔐 سطح محرمانگی
    // ═══════════════════════════════════════════

    /**
     * سطح محرمانگی: NORMAL, CONFIDENTIAL, HIGHLY_CONFIDENTIAL
     * پیش‌فرض: HIGHLY_CONFIDENTIAL
     */
    @Column(name = "confidentiality_level", nullable = false, length = 30)
    var confidentialityLevel: String = "HIGHLY_CONFIDENTIAL"

) : BaseEntity() {

    /** بررسی نهایی بودن */
    @get:Transient
    val isFinalized: Boolean
        get() = diagnosis != null && doctorSignature != null

    override fun toString(): String =
        "TestResult(id=$id, patientId=${patient.id}, testId=${test.id}, " +
                "date=$testDate, confidentiality='$confidentialityLevel')"

    companion object {
        const val LEVEL_NORMAL = "NORMAL"
        const val LEVEL_CONFIDENTIAL = "CONFIDENTIAL"
        const val LEVEL_HIGHLY_CONFIDENTIAL = "HIGHLY_CONFIDENTIAL"
    }
}