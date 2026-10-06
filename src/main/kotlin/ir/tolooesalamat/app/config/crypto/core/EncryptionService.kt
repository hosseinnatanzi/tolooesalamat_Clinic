package ir.tolooesalamat.app.crypto.service

import ir.tolooesalamat.app.crypto.core.DigitalSignatureService
import ir.tolooesalamat.app.crypto.core.HybridEncryptor
import ir.tolooesalamat.app.crypto.core.RsaKeyManager
import org.springframework.stereotype.Service
import java.util.Base64

@Service
class EncryptionService(
    private val hybridEncryptor: HybridEncryptor,
    private val rsaKeyManager: RsaKeyManager,
    private val signatureService: DigitalSignatureService
) {
    fun encrypt(plainText: String?): String? =
        plainText?.takeIf { it.isNotBlank() }?.let { hybridEncryptor.encrypt(it) }

    fun decrypt(cipherText: String?): String? =
        cipherText?.takeIf { it.isNotBlank() }?.let { hybridEncryptor.decrypt(it) }

    fun sign(data: String): String = signatureService.sign(data)

    fun verify(data: String, signature: String): Boolean =
        signatureService.verify(data, signature)

    fun exportPublicKeyBase64(): String =
        Base64.getEncoder().encodeToString(rsaKeyManager.getPublicKey().encoded)
}