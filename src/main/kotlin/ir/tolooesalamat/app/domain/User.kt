package ir.tolooesalamat.app.domain

import ir.tolooesalamat.app.crypto.converter.EncryptedStringConverter
import jakarta.persistence.*

@Entity
@Table(
    name = "users",
    indexes = [
        Index(name = "idx_user_phone", columnList = "phone", unique = true),
        Index(name = "idx_user_username", columnList = "username", unique = true),
        Index(name = "idx_user_role", columnList = "role"),
        Index(name = "idx_user_email", columnList = "email")
    ]
)
class User(

    // ─── شناسه‌ها ───
    @Column(name = "phone", unique = true, nullable = false, length = 11)
    var phone: String = "",

    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "landline_enc", columnDefinition = "TEXT")
    var landline: String? = null,

    @Column(name = "username", unique = true, nullable = false, length = 50)
    var username: String = "",

    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "email_enc", columnDefinition = "TEXT")
    var email: String? = null,

    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "national_id_enc", columnDefinition = "TEXT")
    var nationalId: String? = null,

    // ─── احراز هویت ───
    @Column(name = "password", nullable = false, length = 100)
    var password: String = "",

    @Column(name = "enabled", nullable = false)
    var enabled: Boolean = true,

    // ─── اطلاعات شخصی ───
    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "first_name_enc", columnDefinition = "TEXT", nullable = false)
    var firstName: String = "",

    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "last_name_enc", columnDefinition = "TEXT", nullable = false)
    var lastName: String = "",

    @Column(name = "age")
    var age: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    var gender: Gender = Gender.FEMALE,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    var role: Role = Role.PATIENT,

    // ─── روابط ───
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id",
        foreignKey = ForeignKey(name = "fk_user_clinic"))
    var clinic: Clinic? = null,

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY, orphanRemoval = true)
    var doctorProfile: DoctorProfile? = null,

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY, orphanRemoval = true)
    var patientProfile: PatientProfile? = null,

    // نوبت‌ها
    @OneToMany(mappedBy = "doctor", fetch = FetchType.LAZY)
    var doctorAppointments: MutableList<Appointment> = mutableListOf(),

    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY)
    var patientAppointments: MutableList<Appointment> = mutableListOf(),

    // پرونده‌ها
    @OneToMany(mappedBy = "doctor", fetch = FetchType.LAZY)
    var doctorMedicalRecords: MutableList<MedicalRecord> = mutableListOf(),

    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY)
    var patientMedicalRecords: MutableList<MedicalRecord> = mutableListOf(),

    // تست‌ها
    @OneToMany(mappedBy = "doctor", fetch = FetchType.LAZY)
    var doctorTestResults: MutableList<TestResult> = mutableListOf(),

    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY)
    var patientTestResults: MutableList<TestResult> = mutableListOf()

) : BaseEntity() {
    constructor(
        phone: String,
        landline: String?,
        password: String?,
        firstName: String,
        lastName: String,
        age: Int?,
        gender: Gender,
        username: String,
        role: Role,
        enabled: Boolean
    ) : this()

    constructor(
        phone: String,
        landline: String?,
        password: String?,
        firstName: String,
        lastName: String,
        age: Int?,
        gender: Gender,
        username: String,
        email: String?,
        nationalId: String?,
        role: Role,
        enabled: Boolean
    ) : this()

    @get:Transient
    val fullName: String
        get() = "$firstName $lastName".trim()

    fun hasRole(vararg roles: Role): Boolean = role in roles

    override fun toString(): String =
        "User(id=$id, phone='$phone', role=$role)"
}