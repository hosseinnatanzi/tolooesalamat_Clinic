package ir.tolooesalamat.app.config.crypto.converter



import ir.tolooesalamat.app.config.crypto.core.HybridEncryptor
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.springframework.stereotype.Component

/**
 * Converter خودکار JPA برای رمزنگاری/رمزگشایی فیلدهای String.
 *
 * استفاده در Entity:
 * @Convert(converter = EncryptedStringConverter::class)
 * @Column(name = "diagnosis_enc", columnDefinition = "TEXT")
 * var diagnosis: String? = null
 */
@Component
@Converter
class EncryptedStringConverter(
    private val hybridEncryptor: HybridEncryptor
) : AttributeConverter<String?, String?> {

    override fun convertToDatabaseColumn(attribute: String?): String? {
        if (attribute.isNullOrBlank()) return attribute
        return hybridEncryptor.encrypt(attribute)
    }

    override fun convertToEntityAttribute(dbData: String?): String? {
        if (dbData.isNullOrBlank()) return dbData
        return try {
            hybridEncryptor.decrypt(dbData)
        } catch (e: Exception) {
            // اگر رمزگشایی نشد، احتمالاً داده قدیمی (plain) است
            dbData
        }
    }
}