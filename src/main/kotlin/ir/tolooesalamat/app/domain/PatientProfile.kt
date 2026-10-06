package ir.tolooesalamat.app.domain

import ir.tolooesalamat.app.crypto.converter.EncryptedStringConverter
import ir.tolooesalamat.app.domain.enum.Gender
 import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(
    name = "patient_profiles",
    indexes = [
        Index(name = "idx_patient_file", columnList = "file_number", unique = true),
        Index(name = "idx_patient_user", columnList = "user_id", unique = true),
        Index(name = "idx_patient_doctor", columnList = "primary_doctor_id")
    ]
)
class PatientProfile(

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true,
        foreignKey = ForeignKey(name = "fk_patient_profile_user"))
    var user: User = User(),

    @Column(name = "file_number", unique = true, nullable = false, length = 20)
    var fileNumber: String = "",

    @Column(name = "birth_date")
    var birthDate: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    var gender: Gender? = null,

    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "address_enc", columnDefinition = "TEXT")
    var address: String? = null,

    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "emergency_contact_name_enc", columnDefinition = "TEXT")
    var emergencyContactName: String? = null,

    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "emergency_contact_mobile_enc", columnDefinition = "TEXT")
    var emergencyContactMobile: String? = null,

    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "emergency_contact_landline_enc", columnDefinition = "TEXT")
    var emergencyContactLandline: String? = null,

    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "emergency_contact_relation_enc", columnDefinition = "TEXT")
    var emergencyContactRelation: String? = null,

    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "medical_history_enc", columnDefinition = "TEXT")
    var medicalHistory: String? = null,

    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "current_medications_enc", columnDefinition = "TEXT")
    var currentMedications: String? = null,

    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "allergies_enc", columnDefinition = "TEXT")
    var allergies: String? = null,

    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "substance_use_enc", columnDefinition = "TEXT")
    var substanceUse: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_doctor_id",
        foreignKey = ForeignKey(name = "fk_patient_primary_doctor"))
    var primaryDoctor: User? = null,

    @Column(name = "file_status", nullable = false, length = 20)
    var fileStatus: String = "ACTIVE"

) : BaseEntity() {

    @get:Transient
    val isActive: Boolean get() = fileStatus == "ACTIVE"

    override fun toString(): String =
        "PatientProfile(id=$id, fileNumber='$fileNumber')"
}