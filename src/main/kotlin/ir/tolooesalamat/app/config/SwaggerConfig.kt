package ir.tolooesalamat.app.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * پیکربندی Swagger/OpenAPI.
 *
 * دسترسی‌ها:
 *  - UI:  http://localhost:8080/swagger-ui.html
 *  - JSON: http://localhost:8080/v3/api-docs
 */
@Configuration
class SwaggerConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Clinic API — کلینیک روان‌پزشکی")
                    .version("1.0.0")
                    .description(
                        """
                        API مدیریت کلینیک روان‌پزشکی چند-پزشکی.
              
              
                        """.trimIndent()
                    )
                    .contact(
                        Contact()
                            .name("Clinic Team")
                            .email("support@tolooesalamat.ir")
                    )
                    .license(
                        License()
                            .name("Proprietary")
                    )
            )
            .servers(
                listOf(
                    Server()
                        .url("http://localhost:8080")
                        .description("Local Development Server")
                )
            )
            // 🎯 پیکربندی JWT Bearer
            .components(
                Components()
                    .addSecuritySchemes(
                        "bearer-jwt",
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("توکن JWT خود را وارد کنید (بدون 'Bearer')")
                    )
            )
            .addSecurityItem(
                SecurityRequirement().addList("bearer-jwt")
            )
    }
}