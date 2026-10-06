package ir.tolooesalamat.app.domain

import ir.tolooesalamat.app.crypto.converter.EncryptedStringConverter
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "test_results",
    indexes = [
        Index(name = "idx_result_patient", columnList = "patient_id"),
        Index(name = "idx_result_doctor", columnList = "doctor_id")
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

    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "raw_score_enc", columnDefinition = "TEXT")
    var rawScore: String? = null,

    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "interpretation_enc", columnDefinition = "TEXT")
    var interpretation: String? = null,

    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "diagnosis_enc", columnDefinition = "TEXT")
    var diagnosis: String? = null,

    @Column(name = "doctor_signature", columnDefinition = "TEXT")
    var doctorSignature: String? = null,

    @Column(name = "confidentiality_level", nullable = false, length = 30)
    var confidentialityLevel: String = "HIGHLY_CONFIDENTIAL"
) : BaseEntity()