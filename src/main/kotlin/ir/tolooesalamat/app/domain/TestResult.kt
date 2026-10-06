package ir.tolooesalamat.app.domain

import ir.tolooesalamat.app.crypto.converter.EncryptedStringConverter
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "test_results",
    indexes = [
        Index(name = "idx_result_patient", columnList = "patient_id"),
        Index(name = "idx_result_doctor", columnList = "doctor_id"),
        Index(name = "idx_result_test", columnList = "test_id"),
        Index(name = "idx_result_date", columnList = "test_date")
    ]
)
class TestResult(

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false,
        foreignKey = ForeignKey(name = "fk_result_patient"))
    var patient: User = User(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false,
        foreignKey = ForeignKey(name = "fk_result_doctor"))
    var doctor: User = User(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_id", nullable = false,
        foreignKey = ForeignKey(name = "fk_result_test"))
    var test: PsychologicalTest = PsychologicalTest(),

    @Column(name = "test_date", nullable = false)
    var testDate: LocalDateTime = LocalDateTime.now(),

    // ─── 🔒 فیلدهای رمزنگاری‌شده ───
    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "raw_score_enc", columnDefinition = "TEXT")
    var rawScore: String? = null,

    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "interpretation_enc", columnDefinition = "TEXT")
    var interpretation: String? = null,

    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "diagnosis_enc", columnDefinition = "TEXT")
    var diagnosis: String? = null,

    // 🆕 این دو فیلد گم‌شده بودند:
    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "raw_data_enc", columnDefinition = "TEXT")
    var rawData: String? = null,

    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "recommendations_enc", columnDefinition = "TEXT")
    var recommendations: String? = null,

    // ─── 🔏 امضای دیجیتال ───
    @Column(name = "doctor_signature", columnDefinition = "TEXT")
    var doctorSignature: String? = null,

    @Column(name = "confidentiality_level", nullable = false, length = 30)
    var confidentialityLevel: String = LEVEL_HIGHLY_CONFIDENTIAL

) : BaseEntity() {

    @get:Transient
    val isFinalized: Boolean
        get() = diagnosis != null && doctorSignature != null

    override fun toString(): String =
        "TestResult(id=$id, patientId=${patient.id})"

    companion object {
        // 🆕 این ثابت‌ها گم‌شده بودند:
        const val LEVEL_NORMAL = "NORMAL"
        const val LEVEL_CONFIDENTIAL = "CONFIDENTIAL"
        const val LEVEL_HIGHLY_CONFIDENTIAL = "HIGHLY_CONFIDENTIAL"
    }
}