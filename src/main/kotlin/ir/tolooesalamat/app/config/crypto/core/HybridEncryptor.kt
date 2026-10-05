package ir.tolooesalamat.app.crypto.core

import ir.tolooesalamat.app.crypto.config.CryptoProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * رمزنگاری ترکیبی (Hybrid): RSA-4096 + AES-256-GCM.
 *
 * فرمت خروجی (Base64):
 *   [RSA-Encrypted-AES-Key (512 bytes)] + [IV (12 bytes)] + [AES-Encrypted-Data]
 */
@Component
class HybridEncryptor(
    private val rsaKeyManager: RsaKeyManager,
    private val properties: CryptoProperties
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val secureRandom = SecureRandom()

    companion object {
        private const val RSA_BLOCK_SIZE = 512  // RSA-4096 = 512 bytes
    }

    /**
     * رمزنگاری متن ساده.
     */
    fun encrypt(plainText: String): String {
        if (plainText.isBlank()) return ""

        // 1. تولید AES Key تصادفی
        val aesKey = KeyGenerator.getInstance("AES").apply {
            init(properties.aesKeySize, secureRandom)
        }.generateKey()

        // 2. IV تصادفی
        val iv = ByteArray(properties.ivLength).also { secureRandom.nextBytes(it) }

        // 3. رمزنگاری داده با AES-GCM
        val encryptedData = Cipher.getInstance(properties.aesAlgorithm).apply {
            init(Cipher.ENCRYPT_MODE, aesKey, GCMParameterSpec(properties.gcmTagLength, iv))
        }.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // 4. رمزنگاری AES Key با RSA Public Key
        val encryptedAesKey = rsaKeyManager.encryptWithPublicKey(aesKey.encoded)

        // 5. ترکیب: [RSA Key] + [IV] + [Data]
        val combined = encryptedAesKey + iv + encryptedData

        return Base64.getEncoder().encodeToString(combined)
    }

    /**
     * رمزگشایی متن رمزنگاری‌شده.
     */
    fun decrypt(encryptedBase64: String): String {
        if (encryptedBase64.isBlank()) return ""

        return try {
            val combined = Base64.getDecoder().decode(encryptedBase64)

            require(combined.size > RSA_BLOCK_SIZE + properties.ivLength) {
                "داده رمزنگاری‌شده نامعتبر است"
            }

            val encryptedAesKey = combined.copyOfRange(0, RSA_BLOCK_SIZE)
            val iv = combined.copyOfRange(RSA_BLOCK_SIZE, RSA_BLOCK_SIZE + properties.ivLength)
            val encryptedData = combined.copyOfRange(
                RSA_BLOCK_SIZE + properties.ivLength,
                combined.size
            )

            val aesKeyBytes = rsaKeyManager.decryptWithPrivateKey(encryptedAesKey)
            val aesKey: SecretKey = SecretKeySpec(aesKeyBytes, "AES")

            val decrypted = Cipher.getInstance(properties.aesAlgorithm).apply {
                init(Cipher.DECRYPT_MODE, aesKey, GCMParameterSpec(properties.gcmTagLength, iv))
            }.doFinal(encryptedData)

            String(decrypted, Charsets.UTF_8)
        } catch (ex: Exception) {
            log.error("خطا در رمزگشایی: ${ex.message}")
            throw IllegalStateException("خطا در رمزگشایی داده: ${ex.message}", ex)
        }
    }
}