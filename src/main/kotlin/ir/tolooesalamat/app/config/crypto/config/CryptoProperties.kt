package ir.tolooesalamat.app.crypto.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app.crypto")
class CryptoProperties {

    var publicKeyPath: String = "classpath:keys/public.pem"
    var privateKeyPath: String = "classpath:keys/private.pem"
    var privateKeyPassword: String = ""
    var rsaAlgorithm: String = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
    var aesAlgorithm: String = "AES/GCM/NoPadding"
    var aesKeySize: Int = 256
    var ivLength: Int = 12
    var gcmTagLength: Int = 128
    var signatureAlgorithm: String = "SHA256withRSA"
}