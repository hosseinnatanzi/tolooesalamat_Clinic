package ir.tolooesalamat.app.crypto.core

import ir.tolooesalamat.app.crypto.config.CryptoProperties
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.core.io.ResourceLoader
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
    private val properties: CryptoProperties,
    private val resourceLoader: ResourceLoader
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private lateinit var publicKey: PublicKey
    private lateinit var privateKey: PrivateKey

    @PostConstruct
    fun init() {
        log.info("🔐 بارگذاری کلیدهای RSA...")
        publicKey = loadPublicKey(properties.publicKeyPath)
        privateKey = loadPrivateKey(properties.privateKeyPath, properties.privateKeyPassword)
        log.info("✅ کلیدهای RSA با موفقیت بارگذاری شدند")
    }

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
        Signature.getInstance(properties.signatureAlgorithm).apply {
            initSign(privateKey)
            update(data)
        }.sign()

    fun verify(data: ByteArray, signature: ByteArray): Boolean =
        Signature.getInstance(properties.signatureAlgorithm).apply {
            initVerify(publicKey)
            update(data)
        }.verify(signature)

    // ═══════════════════════════════════════════
    // 🔑 بارگذاری
    // ═══════════════════════════════════════════

    private fun loadPublicKey(path: String): PublicKey {
        val pem = readPem(path)
        val keyBytes = Base64.getDecoder().decode(pem)
        val keySpec = X509EncodedKeySpec(keyBytes)
        return KeyFactory.getInstance("RSA").generatePublic(keySpec)
    }

    private fun loadPrivateKey(path: String, password: String): PrivateKey {
        val pemContent = readPem(path)

        // تلاش ۱: رمزنگاری‌شده
        if (password.isNotBlank()) {
            try {
                return loadEncryptedPrivateKey(pemContent, password)
            } catch (e: Exception) {
                log.debug("⚠️  بارگذاری رمزنگاری‌شده ناموفق: ${e.message}")
            }
        }

        // تلاش ۲: بدون رمز
        return loadPlainPrivateKey(pemContent)
    }

    private fun loadEncryptedPrivateKey(pemContent: String, password: String): PrivateKey {
        val encryptedBytes = Base64.getDecoder().decode(pemContent)
        val encryptedInfo = EncryptedPrivateKeyInfo(encryptedBytes)
        val secretFactory = SecretKeyFactory.getInstance(encryptedInfo.algName)
        val secretKey = secretFactory.generateSecret(PBEKeySpec(password.toCharArray()))
        val keySpec = encryptedInfo.getKeySpec(secretKey)
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec)
    }

    private fun loadPlainPrivateKey(pemContent: String): PrivateKey {
        val keyBytes = Base64.getDecoder().decode(pemContent)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec)
    }

    private fun readPem(path: String): String {
        val resource = resourceLoader.getResource(path)
        return resource.inputStream.bufferedReader().use { reader ->
            reader.lineSequence()
                .filter { line -> !line.startsWith("-----") && line.isNotBlank() }
                .joinToString("")
        }
    }
}