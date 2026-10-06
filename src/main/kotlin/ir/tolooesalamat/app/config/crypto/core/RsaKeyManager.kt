package ir.tolooesalamat.app.crypto.core

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.stereotype.Component
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.EncryptedPrivateKeyInfo
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
@Component
class RsaKeyManager(
    @Value("\${app.crypto.public-key-path}") private val publicKeyPath: String,
    @Value("\${app.crypto.private-key-path}") private val privateKeyPath: String,
    @Value("\${app.crypto.private-key-password:}") private val privateKeyPassword: String,
    @Value("\${app.crypto.rsa-algorithm:RSA/ECB/OAEPWithSHA-256AndMGF1Padding}")
    private val rsaAlgorithm: String,
    @Value("\${app.crypto.signature-algorithm:SHA256withRSA}")
    private val signatureAlgorithm: String
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private lateinit var _publicKey: PublicKey
    private lateinit var _privateKey: PrivateKey

    @PostConstruct
    fun init() {
        log.info("🔐 بارگذاری کلیدهای RSA...")
        _publicKey = loadPublicKey(publicKeyPath)
        _privateKey = loadPrivateKey(privateKeyPath, privateKeyPassword)
        log.info("✅ کلیدهای RSA با موفقیت بارگذاری شدند")
    }

    // ═══════════════════════════════════════════
    // 🔓 API عمومی
    // ═══════════════════════════════════════════

    fun getPublicKey(): PublicKey = _publicKey
    fun getPrivateKey(): PrivateKey = _privateKey

    fun encryptWithPublicKey(data: ByteArray): ByteArray =
        Cipher.getInstance(rsaAlgorithm).apply {
            init(Cipher.ENCRYPT_MODE, _publicKey)
        }.doFinal(data)

    fun decryptWithPrivateKey(encryptedData: ByteArray): ByteArray =
        Cipher.getInstance(rsaAlgorithm).apply {
            init(Cipher.DECRYPT_MODE, _privateKey)
        }.doFinal(encryptedData)

    fun sign(data: ByteArray): ByteArray =
        Signature.getInstance(signatureAlgorithm).apply {
            initSign(_privateKey)
            update(data)
        }.sign()

    fun verify(data: ByteArray, signatureBytes: ByteArray): Boolean =
        Signature.getInstance(signatureAlgorithm).apply {
            initVerify(_publicKey)
            update(data)
        }.verify(signatureBytes)

    // ═══════════════════════════════════════════
    // 🔑 بارگذاری کلیدها
    // ═══════════════════════════════════════════

    private fun loadPublicKey(path: String): PublicKey {
        val pem = readPem(path)
        val keyBytes = Base64.getDecoder().decode(pem)
        val keySpec = X509EncodedKeySpec(keyBytes)
        return KeyFactory.getInstance("RSA").generatePublic(keySpec)
    }

    /**
     * بارگذاری Private Key — پشتیبانی از هر دو فرمت:
     *  1. PKCS#8 Encrypted (با رمز)
     *  2. PKCS#8 Plain (بدون رمز)
     */
    private fun loadPrivateKey(path: String, password: String): PrivateKey {
        val pemContent = readPem(path)

        // تلاش ۱: رمزنگاری‌شده
        if (password.isNotBlank()) {
            try {
                log.debug("🔐 تلاش برای بارگذاری Private Key رمزنگاری‌شده...")
                return loadEncryptedPrivateKey(pemContent, password)
            } catch (e: Exception) {
                log.debug("⚠️  بارگذاری رمزنگاری‌شده ناموفق: ${e.message}")
                log.debug("🔄 تلاش برای بارگذاری Private Key بدون رمز...")
            }
        }

        // تلاش ۲: بدون رمز (PKCS#8 Plain)
        return loadPlainPrivateKey(pemContent)
    }

    private fun loadEncryptedPrivateKey(pemContent: String, password: String): PrivateKey {
        val encryptedBytes = Base64.getDecoder().decode(pemContent)
        val encryptedPrivateKeyInfo = EncryptedPrivateKeyInfo(encryptedBytes)

        val keyFactory = SecretKeyFactory.getInstance(encryptedPrivateKeyInfo.algName)
        val keySpec = PBEKeySpec(password.toCharArray())
        val secretKey = keyFactory.generateSecret(keySpec)

        val decryptedKeySpec = encryptedPrivateKeyInfo.getKeySpec(secretKey)
        return KeyFactory.getInstance("RSA").generatePrivate(decryptedKeySpec)
    }

    private fun loadPlainPrivateKey(pemContent: String): PrivateKey {
        val keyBytes = Base64.getDecoder().decode(pemContent)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec)
    }

    private fun readPem(path: String): String {
        val resource = DefaultResourceLoader().getResource(path)
        return resource.inputStream.bufferedReader().use { reader ->
            reader.lineSequence()
                .filter { line -> !line.startsWith("-----") && line.isNotBlank() }
                .joinToString("")
        }
    }
}
