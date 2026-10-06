package ir.tolooesalamat.app.crypto.core

import ir.tolooesalamat.app.crypto.config.CryptoProperties
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@Component
class HybridEncryptor(
    private val rsaKeyManager: RsaKeyManager,
    private val properties: CryptoProperties
) {
    private val secureRandom = SecureRandom()

    companion object {
        private const val RSA_BLOCK_SIZE = 512  // RSA-4096
    }

    fun encrypt(plainText: String): String {
        if (plainText.isBlank()) return ""

        // 1. AES Key
        val aesKey = KeyGenerator.getInstance("AES").apply {
            init(properties.aesKeySize, secureRandom)
        }.generateKey()

        // 2. IV
        val iv = ByteArray(properties.ivLength).also { secureRandom.nextBytes(it) }

        // 3. AES-GCM
        val encryptedData = Cipher.getInstance(properties.aesAlgorithm).apply {
            init(Cipher.ENCRYPT_MODE, aesKey, GCMParameterSpec(properties.gcmTagLength, iv))
        }.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // 4. RSA روی AES Key
        val encryptedAesKey = rsaKeyManager.encryptWithPublicKey(aesKey.encoded)

        // 5. ترکیب
        val combined = encryptedAesKey + iv + encryptedData
        return Base64.getEncoder().encodeToString(combined)
    }

    fun decrypt(encryptedBase64: String): String {
        if (encryptedBase64.isBlank()) return ""

        val combined = Base64.getDecoder().decode(encryptedBase64)
        require(combined.size > RSA_BLOCK_SIZE + properties.ivLength)

        val encryptedAesKey = combined.copyOfRange(0, RSA_BLOCK_SIZE)
        val iv = combined.copyOfRange(RSA_BLOCK_SIZE, RSA_BLOCK_SIZE + properties.ivLength)
        val encryptedData = combined.copyOfRange(RSA_BLOCK_SIZE + properties.ivLength, combined.size)

        val aesKeyBytes = rsaKeyManager.decryptWithPrivateKey(encryptedAesKey)
        val aesKey: SecretKey = SecretKeySpec(aesKeyBytes, "AES")

        val decrypted = Cipher.getInstance(properties.aesAlgorithm).apply {
            init(Cipher.DECRYPT_MODE, aesKey, GCMParameterSpec(properties.gcmTagLength, iv))
        }.doFinal(encryptedData)

        return String(decrypted, Charsets.UTF_8)
    }
}