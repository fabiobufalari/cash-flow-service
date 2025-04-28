package com.bufalari.cashflow.config;

import com.bufalari.cashflow.auditing.AuditorAwareImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProviderCashflow") // Ref é "auditorProviderCashflow"
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProviderCashflow() { // Nome do bean é "auditorProviderCashflow"
        return new AuditorAwareImpl(); // Localizado em 'auditing' - OK
    }
}