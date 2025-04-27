// Path: src/main/java/com/bufalari/cashflow/config/JpaAuditingConfig.java
package com.bufalari.cashflow.config;

import com.bufalari.cashflow.auditing.AuditorAwareImpl; // Import implementation
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProviderCashflow") // Use unique bean name
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProviderCashflow() { // Bean name matches ref
        return new AuditorAwareImpl();
    }
}