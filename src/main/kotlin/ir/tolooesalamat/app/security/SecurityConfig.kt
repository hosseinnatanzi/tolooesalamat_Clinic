package ir.tolooesalamat.app.security

import ir.tolooesalamat.app.security.jwt.JwtAccessDeniedHandler
import ir.tolooesalamat.app.security.jwt.JwtAuthenticationEntryPoint
import ir.tolooesalamat.app.security.jwt.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val userDetailsService: CustomUserDetailsService,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
    private val jwtAccessDeniedHandler: JwtAccessDeniedHandler
) {

    /**
     * Bcrypt برای هش رمز عبور (cost=12).
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder(12)

    /**
     * Provider احراز هویت.
     *
     * ⚠️ در Spring Security 7:
     *  - Constructor الزاماً userDetailsService می‌گیرد
     *  - setUserDetailsService حذف شده
     *  - setPasswordEncoder هنوز هست (ولی اختیاری)
     */
    @Bean
    fun authenticationProvider(): DaoAuthenticationProvider {
        val provider = DaoAuthenticationProvider(userDetailsService)  // ← Constructor
        provider.setPasswordEncoder(passwordEncoder())                // ← هنوز کار می‌کند
        return provider
    }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager =
        config.authenticationManager

    /**
     * CORS Configuration.
     */
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOriginPatterns = listOf("*")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        configuration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    /**
     * فیلترهای امنیتی.
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .exceptionHandling {
                it.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                it.accessDeniedHandler(jwtAccessDeniedHandler)
            }
            .authorizeHttpRequests { auth ->
                auth
                    // ─────── عمومی ───────
                    .requestMatchers(
                        "/",
                        "/register",
                        "/login",
                        "/error"
                    ).permitAll()

                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico").permitAll()

                    // ─────── Swagger / OpenAPI ───────
                    .requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/api-docs/**"
                    ).permitAll()

                    // ─────── عمومی (فقط GET) ───────
                    .requestMatchers(HttpMethod.GET, "/api/psychological-tests/catalog").permitAll()

                    // ─────── ادمین ───────
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")

                    // ─────── پزشکان ───────
                    .requestMatchers("/api/doctors/**").hasAnyRole("ADMIN", "DOCTOR")

                    // ─────── پرونده پزشکی ───────
                    .requestMatchers("/api/records/**").hasAnyRole("ADMIN", "DOCTOR", "PATIENT")

                    // ─────── تست‌های روانشناسی ───────
                    .requestMatchers(HttpMethod.GET, "/api/psychological-tests/**")
                    .hasAnyRole("ADMIN", "DOCTOR", "PATIENT")
                    .requestMatchers(HttpMethod.POST, "/api/psychological-tests/**")
                    .hasAnyRole("ADMIN", "DOCTOR")
                    .requestMatchers(HttpMethod.PUT, "/api/psychological-tests/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/psychological-tests/**")
                    .hasRole("ADMIN")

                    // ─────── نوبت‌ها ───────
                    .requestMatchers(HttpMethod.GET, "/api/appointments/**")
                    .hasAnyRole("ADMIN", "RECEPTIONIST", "DOCTOR", "PATIENT")
                    .requestMatchers(HttpMethod.POST, "/api/appointments/**")
                    .hasAnyRole("ADMIN", "RECEPTIONIST", "PATIENT")
                    .requestMatchers(HttpMethod.PUT, "/api/appointments/**")
                    .hasAnyRole("ADMIN", "RECEPTIONIST", "DOCTOR")
                    .requestMatchers(HttpMethod.DELETE, "/api/appointments/**")
                    .hasRole("ADMIN")

                    // ─────── بقیه ───────
                    .anyRequest().authenticated()
            }
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}