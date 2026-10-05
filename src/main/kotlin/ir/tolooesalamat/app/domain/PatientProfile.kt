package ir.tolooesalamat.app.domain

import ir.tolooesalamat.app.config.crypto.converter.EncryptedStringConverter
 import jakarta.persistence.*
import java.time.LocalDate

/**
 * پروفایل بیمار.
 *
 * یک User با نقش PATIENT، دقیقاً یک PatientProfile دارد.
 *
 * شامل:
 *  • شماره پرونده یکتا (P-YYYY-NNNNN)
 *  • اطلاعات پزشکی (رمزنگاری‌شده)
 *  • تماس اضطراری
 *  • پزشک اصلی
 *  • نتایج تست‌های روانشناسی
 */
@Entity
@Table(
    name = "patient_profiles",
    indexes = [
        Index(name = "idx_patient_file", columnList = "file_number", unique = true),
        Index(name = "idx_patient_user", columnList = "user_id", unique = true),
        Index(name = "idx_patient_doctor", columnList = "primary_doctor_id"),
        Index(name = "idx_patient_status", columnList = "file_status")
    ]
)
class PatientProfile(

    // ═══════════════════════════════════════════════════════════
    // 🔗 ارتباط با User (کلید خارجی یکتا)
    // ═══════════════════════════════════════════════════════════

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        unique = true,
        foreignKey = ForeignKey(name = "fk_patient_profile_user")
    )
    var user: User = User(),

    // ═══════════════════════════════════════════════════════════
    // 📋 اطلاعات پرونده
    // ═══════════════════════════════════════════════════════════

    /**
     * شماره پرونده یکتا و اجباری.
     * فرمت: P-YYYY-NNNNN
     * مثال: P-2024-00042
     */
    @Column(
        name = "file_number",
        unique = true,
        nullable = false,
        length = 20
    )
    var fileNumber: String = "",

    @Column(name = "birth_date")
    var birthDate: LocalDate? = null,

    /** 🎯 جنسیت — با enum Gender */
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    var gender: Gender? = null,

    @Column(name = "address", columnDefinition = "TEXT")
    var address: String? = null,

    // ═══════════════════════════════════════════════════════════
    // 📞 تماس اضطراری
    // ═══════════════════════════════════════════════════════════

    /** نام شخص تماس اضطراری */
    @Column(name = "emergency_contact_name", length = 100)
    var emergencyContactName: String? = null,

    /** موبایل تماس اضطراری */
    @Column(name = "emergency_contact_mobile", length = 11)
    var emergencyContactMobile: String? = null,

    /** تلفن ثابت تماس اضطراری (اختیاری) */
    @Column(name = "emergency_contact_landline", length = 15)
    var emergencyContactLandline: String? = null,

    /** نسبت با بیمار (همسر، پدر، مادر، ...) */
    @Column(name = "emergency_contact_relation", length = 50)
    var emergencyContactRelation: String? = null,

    // ═══════════════════════════════════════════════════════════
    // 🩺 اطلاعات پزشکی — 🔒 رمزنگاری‌شده
    // ═══════════════════════════════════════════════════════════

    /**
     * سابقه پزشکی — رمزنگاری‌شده.
     * شامل: بیماری‌های قبلی، سابقه خانوادگی، جراحی‌ها.
     */
    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "medical_history_enc", columnDefinition = "TEXT")
    var medicalHistory: String? = null,

    /**
     * داروهای مصرفی فعلی — رمزنگاری‌شده.
     */
    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "current_medications_enc", columnDefinition = "TEXT")
    var currentMedications: String? = null,

    /**
     * حساسیت دارویی — رمزنگاری‌شده.
     * بسیار حساس — برای جلوگیری از تجویز داروی اشتباه.
     */
    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "allergies_enc", columnDefinition = "TEXT")
    var allergies: String? = null,

    /**
     * سابقه مصرف مواد/الکل — رمزنگاری‌شده.
     */
    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "substance_use_enc", columnDefinition = "TEXT")
    var substanceUse: String? = null,

    // ═══════════════════════════════════════════════════════════
    // 🔗 ارتباطات
    // ═══════════════════════════════════════════════════════════

    /**
     * پزشک اصلی (کسی که بیمار را پذیرفته).
     * اختیاری — ممکن است بیمار هنوز پزشک انتخاب نکرده باشد.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "primary_doctor_id",
        foreignKey = ForeignKey(name = "fk_patient_primary_doctor")
    )
    var primaryDoctor: User? = null,

    /**
     * وضعیت پرونده: ACTIVE / ARCHIVED / CLOSED
     */
    @Column(name = "file_status", nullable = false, length = 20)
    var fileStatus: String = "ACTIVE",

    // ═══════════════════════════════════════════════════════════
    // 🧠 نتایج تست‌های روانشناسی
    // ═══════════════════════════════════════════════════════════

    /**
     * تمام تست‌های روانشناسی این بیمار.
     * برای بررسی طول درمان و مقایسه نتایج.
     */
    @OneToMany(
        mappedBy = "patient",
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY
    )
    var testResults: MutableList<TestResult> = mutableListOf()

) : BaseEntity() {

    // ═══════════════════════════════════════════════════════════
    // 🛠 متدهای کمکی
    // ═══════════════════════════════════════════════════════════

    /** بررسی فعال بودن پرونده */
    @get:Transient
    val isActive: Boolean
        get() = fileStatus == FILE_STATUS_ACTIVE

    /** بررسی آرشیو بودن پرونده */
    @get:Transient
    val isArchived: Boolean
        get() = fileStatus == FILE_STATUS_ARCHIVED

    /** اطلاعات کامل تماس اضطراری */
    @get:Transient
    val emergencyContactInfo: String
        get() {
            val name = emergencyContactName ?: return "—"
            val phones = listOfNotNull(emergencyContactMobile, emergencyContactLandline)
                .joinToString(" / ")
            val relation = emergencyContactRelation?.let { " ($it)" } ?: ""
            return if (phones.isBlank()) "$name$relation" else "$name$relation - $phones"
        }

    /** بررسی داشتن تماس اضطراری */
    @get:Transient
    val hasEmergencyContact: Boolean
        get() = !emergencyContactName.isNullOrBlank() &&
                (!emergencyContactMobile.isNullOrBlank() || !emergencyContactLandline.isNullOrBlank())

    /** سن بیمار بر اساس تاریخ تولد */
    @get:Transient
    val calculatedAge: Int?
        get() = birthDate?.let {
            java.time.Period.between(it, LocalDate.now()).years
        }

    /** تعداد تست‌های انجام‌شده */
    @get:Transient
    val totalTestsPerformed: Int
        get() = testResults.size

    /** آخرین تست انجام‌شده */
    @get:Transient
    val latestTest: TestResult?
        get() = testResults.maxByOrNull { it.testDate }

    /** تغییر وضعیت پرونده */
    fun changeStatus(newStatus: String) {
        require(newStatus in listOf(FILE_STATUS_ACTIVE, FILE_STATUS_ARCHIVED, FILE_STATUS_CLOSED)) {
            "وضعیت پرونده نامعتبر: $newStatus"
        }
        this.fileStatus = newStatus
    }

    /** بررسی تعلق به پزشک خاص */
    fun belongsToDoctor(doctorId: Long): Boolean =
        primaryDoctor?.id == doctorId

    override fun toString(): String =
        "PatientProfile(id=$id, fileNumber='$fileNumber', status='$fileStatus')"

    companion object {
        const val FILE_STATUS_ACTIVE = "ACTIVE"
        const val FILE_STATUS_ARCHIVED = "ARCHIVED"
        const val FILE_STATUS_CLOSED = "CLOSED"
    }
}