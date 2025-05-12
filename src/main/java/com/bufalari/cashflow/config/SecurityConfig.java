package com.bufalari.cashflow.config;

import com.bufalari.cashflow.secutity.JwtAuthenticationFilter; // Import correto
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for the Cash Flow Service.
 * Configuração de segurança para o Serviço de Fluxo de Caixa.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Habilita @PreAuthorize etc.
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    // Endpoints públicos (Swagger, Actuator)
    private static final String[] PUBLIC_MATCHERS = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/actuator/**" // Expor apenas endpoints seguros do actuator se necessário
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Desabilitar CSRF para APIs stateless
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Aplicar configuração CORS
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PUBLIC_MATCHERS).permitAll() // Permitir acesso público
                        // --- REGRAS ESPECÍFICAS PARA CASHFLOW ---
                        // Lançamentos Manuais
                        .requestMatchers(HttpMethod.POST, "/api/cashflow/manual-entries").hasAnyRole("ADMIN", "ACCOUNTANT")
                        .requestMatchers(HttpMethod.GET, "/api/cashflow/manual-entries/**").hasAnyRole("ADMIN", "ACCOUNTANT", "FINANCIAL_VIEWER", "MANAGER") // GET por ID e talvez GET all (se existir)
                        .requestMatchers(HttpMethod.DELETE, "/api/cashflow/manual-entries/**").hasRole("ADMIN")
                        // Relatórios
                        .requestMatchers(HttpMethod.GET, "/api/cashflow/balance/current").hasAnyRole("ADMIN", "MANAGER", "ACCOUNTANT", "FINANCIAL_VIEWER")
                        .requestMatchers(HttpMethod.GET, "/api/cashflow/statement").hasAnyRole("ADMIN", "MANAGER", "ACCOUNTANT", "FINANCIAL_VIEWER")
                        .requestMatchers(HttpMethod.GET, "/api/cashflow/forecast").hasAnyRole("ADMIN", "MANAGER", "ACCOUNTANT", "FINANCIAL_VIEWER")
                        // Qualquer outra requisição precisa estar autenticada
                        .anyRequest().authenticated()
                )
                // Usar política STATELESS, pois usamos JWT
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Adicionar o filtro JWT antes do filtro de autenticação padrão
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Restringir origens em produção! Ex: "http://meufrontend.com"
        configuration.setAllowedOrigins(List.of("*")); // <<< CUIDADO EM PRODUÇÃO
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With", "Origin"));
        // Expor headers necessários para o frontend (ex: Location em respostas 201)
        configuration.setExposedHeaders(List.of("Authorization", "Location"));
        configuration.setAllowCredentials(false); // Geralmente false para JWT stateless
        configuration.setMaxAge(3600L); // Cache de preflight por 1 hora

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Aplicar a todos os paths
        return source;
    }
}