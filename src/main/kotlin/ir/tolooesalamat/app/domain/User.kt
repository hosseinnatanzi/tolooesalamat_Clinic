package ir.tolooesalamat.app.domain

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
    @Column(name = "phone", unique = true, nullable = false, length = 11)
    var phone: String = "",

    @Column(name = "landline", length = 15)
    var landline: String? = null,

    @Column(name = "password", nullable = false, length = 100)
    var password: String = "",

    @Column(name = "first_name", nullable = false, length = 50)
    var firstName: String = "",

    @Column(name = "last_name", nullable = false, length = 50)
    var lastName: String = "",

    @Column(name = "age")
    var age: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    var gender: Gender = Gender.FEMALE,

    @Column(name = "username", unique = true, nullable = false, length = 50)
    var username: String = "",

    @Column(name = "email", unique = true, length = 100)
    var email: String? = null,

    @Column(name = "national_id", unique = true, length = 10)
    var nationalId: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    var role: Role = Role.PATIENT,

    @Column(name = "enabled", nullable = false)
    var enabled: Boolean = true,

    // ═══════════ روابط ═══════════

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id", foreignKey = ForeignKey(name = "fk_user_clinic"))
    var clinic: Clinic? = null,

    @OneToOne(
        mappedBy = "user",
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    var doctorProfile: DoctorProfile? = null,

    @OneToOne(
        mappedBy = "user",
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    var patientProfile: PatientProfile? = null,

    // 🆕 روابط تست‌های روانشناسی
    @OneToMany(mappedBy = "doctor", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var performedTests: MutableList<TestResult> = mutableListOf(),

    @OneToMany(mappedBy = "patient", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var testResults: MutableList<TestResult> = mutableListOf()

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

    @get:Transient
    val displayName: String
        get() = fullName.ifBlank { phone }

    @get:Transient
    val allContacts: String
        get() = listOfNotNull(phone, landline).joinToString(" / ")

    fun hasRole(vararg roles: Role): Boolean = role in roles
    fun isFemale(): Boolean = gender == Gender.FEMALE
    fun isMale(): Boolean = gender == Gender.MALE

    override fun toString(): String =
        "User(id=$id, phone='$phone', role=$role, enabled=$enabled)"
}