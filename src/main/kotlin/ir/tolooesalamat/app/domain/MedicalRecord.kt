package ir.tolooesalamat.app.domain

import ir.tolooesalamat.app.config.crypto.converter.EncryptedStringConverter
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * پرونده پزشکی — یادداشت جلسه درمانی.
 *
 * 🔒 تمام فیلدهای حساس با Hybrid Encryption (RSA + AES) رمزنگاری می‌شوند.
 * 🔏 تشخیص نهایی با امضای دیجیتال پزشک تأیید می‌شود.
 */
@Entity
@Table(
    name = "medical_records",
    indexes = [
        Index(name = "idx_record_patient", columnList = "patient_id"),
        Index(name = "idx_record_doctor", columnList = "doctor_id"),
        Index(name = "idx_record_date", columnList = "session_date"),
        Index(name = "idx_record_patient_date", columnList = "patient_id, session_date")
    ]
)
class MedicalRecord(

    // ═══════════════════════════════════════════
    // 🔗 ارتباطات
    // ═══════════════════════════════════════════

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "patient_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_record_patient")
    )
    var patient: User = User(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "doctor_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_record_doctor")
    )
    var doctor: User = User(),

    // ═══════════════════════════════════════════
    // 📋 اطلاعات جلسه
    // ═══════════════════════════════════════════

    /** شماره جلسه درمانی */
    @Column(name = "session_number", nullable = false)
    var sessionNumber: Int = 1,

    @Column(name = "session_date", nullable = false)
    var sessionDate: LocalDateTime = LocalDateTime.now(),

    // ═══════════════════════════════════════════
    // 🔒 فیلدهای رمزنگاری‌شده
    // ═══════════════════════════════════════════

    /** شکایت اصلی بیمار */
    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "chief_complaint_enc", columnDefinition = "TEXT")
    var chiefComplaint: String? = null,

    /** شرح حال (History of Present Illness) */
    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "present_illness_enc", columnDefinition = "TEXT")
    var presentIllness: String? = null,

    /** ارزیابی روانی (Mental Status Exam) */
    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "mental_status_enc", columnDefinition = "TEXT")
    var mentalStatusExam: String? = null,

    /** تشخیص نهایی — بسیار حساس */
    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "diagnosis_enc", columnDefinition = "TEXT")
    var diagnosis: String? = null,

    /** برنامه درمانی */
    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "treatment_plan_enc", columnDefinition = "TEXT")
    var treatmentPlan: String? = null,

    /** داروهای تجویزی */
    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "medications_enc", columnDefinition = "TEXT")
    var medications: String? = null,

    /** یادداشت‌های جلسه */
    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "session_notes_enc", columnDefinition = "TEXT")
    var sessionNotes: String? = null,

    // ═══════════════════════════════════════════
    // 🔏 امضای دیجیتال
    // ═══════════════════════════════════════════

    /**
     * امضای دیجیتال پزشک روی تشخیص.
     * SHA256withRSA — تأیید اصالت و عدم انکار.
     */
    @Column(name = "diagnosis_signature", columnDefinition = "TEXT")
    var diagnosisSignature: String? = null,

    /** آیا این رکورد تأیید نهایی شده؟ */
    @Column(name = "is_finalized", nullable = false)
    var isFinalized: Boolean = false,

    @Column(name = "finalized_at")
    var finalizedAt: LocalDateTime? = null

) : BaseEntity() {

    override fun toString(): String =
        "MedicalRecord(id=$id, patientId=${patient.id}, doctorId=${doctor.id}, " +
                "session=$sessionNumber, finalized=$isFinalized)"
}