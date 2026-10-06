package ir.tolooesalamat.app.domain

import ir.tolooesalamat.app.crypto.converter.EncryptedStringConverter
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "medical_records",
    indexes = [
        Index(name = "idx_record_patient", columnList = "patient_id"),
        Index(name = "idx_record_doctor", columnList = "doctor_id")
    ]
)
class MedicalRecord(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false,
        foreignKey = ForeignKey(name = "fk_record_patient"))
    var patient: User = User(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false,
        foreignKey = ForeignKey(name = "fk_record_doctor"))
    var doctor: User = User(),

    @Column(name = "session_number", nullable = false)
    var sessionNumber: Int = 1,

    @Column(name = "session_date", nullable = false)
    var sessionDate: LocalDateTime = LocalDateTime.now(),

    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "chief_complaint_enc", columnDefinition = "TEXT")
    var chiefComplaint: String? = null,

    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "present_illness_enc", columnDefinition = "TEXT")
    var presentIllness: String? = null,

    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "mental_status_enc", columnDefinition = "TEXT")
    var mentalStatusExam: String? = null,

    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "diagnosis_enc", columnDefinition = "TEXT")
    var diagnosis: String? = null,

    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "treatment_plan_enc", columnDefinition = "TEXT")
    var treatmentPlan: String? = null,

    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "medications_enc", columnDefinition = "TEXT")
    var medications: String? = null,

    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "session_notes_enc", columnDefinition = "TEXT")
    var sessionNotes: String? = null,

    @Column(name = "diagnosis_signature", columnDefinition = "TEXT")
    var diagnosisSignature: String? = null,

    @Column(name = "is_finalized", nullable = false)
    var isFinalized: Boolean = false,
    @Column(name = "finalized_at")
    var finalizedAt: LocalDateTime? = null
) : BaseEntity()