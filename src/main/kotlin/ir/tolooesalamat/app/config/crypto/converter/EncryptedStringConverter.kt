package ir.tolooesalamat.app.crypto.converter

import ir.tolooesalamat.app.crypto.core.HybridEncryptor
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.springframework.stereotype.Component

/**
 * Converter خودکار JPA برای رمزنگاری/رمزگشایی فیلدهای String.
 *
 * استفاده:
 * @Convert(converter = EncryptedStringConverter::class)
 * @Column(name = "field_enc", columnDefinition = "TEXT")
 * var field: String? = null
 */
@Component
@Converter
class EncryptedStringConverter(
    private val hybridEncryptor: HybridEncryptor
) : AttributeConverter<String?, String?> {

    override fun convertToDatabaseColumn(attribute: String?): String? {
        if (attribute.isNullOrBlank()) return attribute
        return try {
            hybridEncryptor.encrypt(attribute)
        } catch (e: Exception) {
            // اگر رمزنگاری شکست خورد، مقدار خام را ذخیره کن
            attribute
        }
    }

    override fun convertToEntityAttribute(dbData: String?): String? {
        if (dbData.isNullOrBlank()) return dbData
        return try {
            hybridEncryptor.decrypt(dbData)
        } catch (e: Exception) {
            // اگر رمزگشایی شکست خورد، احتمالاً داده قدیمی (plain) است
            dbData
        }
    }
}