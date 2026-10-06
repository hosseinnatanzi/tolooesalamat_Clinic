package ir.tolooesalamat.app.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import ir.tolooesalamat.app.crypto.service.EncryptionService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/crypto")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Crypto Health", description = "سلامت رمزنگاری RSA (فقط ادمین)")
class CryptoHealthController(
    private val encryptionService: EncryptionService
) {

    @Operation(summary = "بررسی سلامت رمزنگاری")
    @GetMapping("/health")
    fun cryptoHealth(): ResponseEntity<Map<String, Any>> {
        val testData = "سلام — Test 123"

        return try {
            val encrypted = encryptionService.encrypt(testData)
            val decrypted = encryptionService.decrypt(encrypted)
            val signature = encryptionService.sign(testData)
            val verified = encryptionService.verify(testData, signature)

            ResponseEntity.ok(
                mapOf(
                    "status" to "OK",
                    "encryptionWorking" to (decrypted == testData),
                    "signatureWorking" to verified,
                    "encryptedSample" to (encrypted?.take(50) ?: "N/A"),
                    "algorithm" to "RSA-4096 + AES-256-GCM"
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(500).body(
                mapOf(
                    "status" to "ERROR",
                    "message" to (e.message ?: "Unknown error")
                )
            )
        }
    }

    @Operation(summary = "خروجی Public Key")
    @GetMapping("/public-key")
    fun exportPublicKey(): ResponseEntity<Map<String, String>> =
        ResponseEntity.ok(
            mapOf(
                "algorithm" to "RSA-4096",
                "format" to "Base64",
                "publicKey" to encryptionService.exportPublicKeyBase64()
            )
        )
}