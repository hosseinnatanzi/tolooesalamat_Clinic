package ir.tolooesalamat.app.config.crypto.core


import ir.tolooesalamat.app.crypto.core.DigitalSignatureService
 import ir.tolooesalamat.app.crypto.core.RsaKeyManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.Base64

/**
 * سرویس یکپارچه رمزنگاری.
 *
 * لایه‌ای بالاتر از HybridEncryptor و DigitalSignatureService
 * که API ساده‌ای برای استفاده در سایر بخش‌های برنامه فراهم می‌کند.
 */
@Service
class EncryptionService(
    private val hybridEncryptor: HybridEncryptor,
    private val rsaKeyManager: RsaKeyManager,
    private val signatureService: DigitalSignatureService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    // ═══════════════════════════════════════════
    // 🔒 رمزنگاری / رمزگشایی
    // ═══════════════════════════════════════════

    /**
     * رمزنگاری متن ساده.
     * اگر ورودی null یا خالی باشد، همان را برمی‌گرداند.
     */
    fun encrypt(plainText: String?): String? {
        if (plainText.isNullOrBlank()) return plainText

        return try {
            hybridEncryptor.encrypt(plainText)
        } catch (ex: Exception) {
            log.error("خطا در رمزنگاری: ${ex.message}")
            throw IllegalStateException("خطا در رمزنگاری داده", ex)
        }
    }

    /**
     * رمزگشایی متن رمزنگاری‌شده.
     * اگر ورودی null یا خالی باشد، همان را برمی‌گرداند.
     */
    fun decrypt(cipherText: String?): String? {
        if (cipherText.isNullOrBlank()) return cipherText

        return try {
            hybridEncryptor.decrypt(cipherText)
        } catch (ex: Exception) {
            log.error("خطا در رمزگشایی: ${ex.message}")
            throw IllegalStateException("خطا در رمزگشایی داده", ex)
        }
    }

    // ═══════════════════════════════════════════
    // ✍️ امضا / بررسی امضا
    // ═══════════════════════════════════════════

    /**
     * امضای داده با Private Key.
     */
    fun sign(data: String): String = signatureService.sign(data)

    /**
     * بررسی امضا با Public Key.
     */
    fun verify(data: String, signature: String): Boolean =
        signatureService.verify(data, signature)

    // ═══════════════════════════════════════════
    // 🌐 Export / Import
    // ═══════════════════════════════════════════

    /**
     * خروجی Public Key به صورت Base64.
     * برای اشتراک با سرویس‌های خارجی.
     */
    fun exportPublicKeyBase64(): String =
        Base64.getEncoder().encodeToString(rsaKeyManager.getPublicKey().encoded)

    /**
     * بررسی صحت سیستم رمزنگاری.
     * برای Health Check.
     */
    fun healthCheck(): Boolean {
        return try {
            val test = "سلام — Test 123"
            val encrypted = encrypt(test)
            val decrypted = decrypt(encrypted)
            val signature = sign(test)
            val verified = verify(test, signature)

            decrypted == test && verified
        } catch (ex: Exception) {
            log.error("Health check failed: ${ex.message}")
            false
        }
    }
}