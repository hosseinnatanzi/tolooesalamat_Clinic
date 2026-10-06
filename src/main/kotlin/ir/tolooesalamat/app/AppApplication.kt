package ir.tolooesalamat.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan   // ← این خط حیاتی است

class AppApplication

fun main(args: Array<String>) {
    runApplication<AppApplication>(*args)
}
