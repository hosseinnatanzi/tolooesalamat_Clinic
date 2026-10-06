package ir.tolooesalamat.app.domain

import ir.tolooesalamat.app.crypto.converter.EncryptedStringConverter
import jakarta.persistence.*

@Entity
@Table(name = "clinics")
class Clinic(
    @Column(name = "name", nullable = false, unique = true, length = 100)
    var name: String = "",

    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "address_enc", columnDefinition = "TEXT")
    var address: String? = null,

    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "mobile_enc", columnDefinition = "TEXT")
    var mobile: String? = null,

    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "landline_enc", columnDefinition = "TEXT")
    var landline: String? = null,

    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "email_enc", columnDefinition = "TEXT")
    var email: String? = null,

    @Column(name = "active", nullable = false)
    var active: Boolean = true
) : BaseEntity()