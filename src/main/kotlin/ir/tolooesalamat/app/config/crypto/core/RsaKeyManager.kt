package ir.tolooesalamat.app.crypto.core

import ir.tolooesalamat.app.crypto.config.CryptoProperties
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.EncryptedPrivateKeyInfo
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * مدیر جفت کلید RSA.
 * کلیدها یک بار در startup بارگذاری می‌شوند و در حافظه می‌مانند.
 */
@Component
class RsaKeyManager(
    private val properties: CryptoProperties,
    private val resourceLoader: ResourceLoader
) {

    private val log = LoggerFactory.getLogger(javaClass)

    private lateinit var publicKey: PublicKey
    private lateinit var privateKey: PrivateKey

    @PostConstruct
    fun init() {
        log.info("🔐 بارگذاری کلیدهای RSA...")
        publicKey = loadPublicKey()
        privateKey = loadPrivateKey()
        log.info("✅ کلیدهای RSA با موفقیت بارگذاری شدند")
    }

    /**
     * بارگذاری Public Key از classpath.
     */
    private fun loadPublicKey(): PublicKey {
        val resource = resourceLoader.getResource(properties.publicKeyPath)

        require(resource.exists()) {
            "❌ Public Key یافت نشد: ${properties.publicKeyPath}"
        }

        val pem = resource.inputStream.bufferedReader().use { it.readText() }
        val base64 = pem
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s".toRegex(), "")

        val keyBytes = Base64.getDecoder().decode(base64)
        val spec = X509EncodedKeySpec(keyBytes)

        return KeyFactory.getInstance("RSA").generatePublic(spec)
    }

    /**
     * بارگذاری Private Key رمزنگاری‌شده از classpath.
     */
    private fun loadPrivateKey(): PrivateKey {
        val resource = resourceLoader.getResource(properties.privateKeyPath)

        require(resource.exists()) {
            "❌ Private Key یافت نشد: ${properties.privateKeyPath}"
        }

        require(properties.privateKeyPassword.isNotBlank()) {
            "❌ رمز Private Key تنظیم نشده است"
        }

        val pem = resource.inputStream.bufferedReader().use { it.readText() }
        val base64 = pem
            .replace("-----BEGIN ENCRYPTED PRIVATE KEY-----", "")
            .replace("-----END ENCRYPTED PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")

        val encryptedBytes = Base64.getDecoder().decode(base64)
        val encryptedInfo = EncryptedPrivateKeyInfo(encryptedBytes)

        val pbeSpec = PBEKeySpec(properties.privateKeyPassword.toCharArray())
        val secretFactory = SecretKeyFactory.getInstance(encryptedInfo.algName)
        val secretKey = secretFactory.generateSecret(pbeSpec)

        val keySpec = encryptedInfo.getKeySpec(secretKey)
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec)
    }

    // ═══════════ API عمومی ═══════════

    fun getPublicKey(): PublicKey = publicKey
    fun getPrivateKey(): PrivateKey = privateKey

    fun encryptWithPublicKey(data: ByteArray): ByteArray =
        Cipher.getInstance(properties.rsaAlgorithm).apply {
            init(Cipher.ENCRYPT_MODE, publicKey)
        }.doFinal(data)

    fun decryptWithPrivateKey(encrypted: ByteArray): ByteArray =
        Cipher.getInstance(properties.rsaAlgorithm).apply {
            init(Cipher.DECRYPT_MODE, privateKey)
        }.doFinal(encrypted)

    fun sign(data: ByteArray): ByteArray =
        java.security.Signature.getInstance(properties.signatureAlgorithm).apply {
            initSign(privateKey)
            update(data)
        }.sign()

    fun verify(data: ByteArray, signature: ByteArray): Boolean =
        java.security.Signature.getInstance(properties.signatureAlgorithm).apply {
            initVerify(publicKey)
            update(data)
        }.verify(signature)
}