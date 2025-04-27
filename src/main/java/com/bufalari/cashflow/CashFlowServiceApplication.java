// Path: src/main/java/com/bufalari/cashflow/CashFlowServiceApplication.java
package com.bufalari.cashflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients; // <<<--- IMPORT & ENABLE
// import org.springframework.data.jpa.repository.config.EnableJpaAuditing; // If using config class

/**
 * Main application class for the Cash Flow Service.
 * Classe principal da aplicação para o Serviço de Fluxo de Caixa.
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.bufalari.cashflow.client") // <<<--- ENABLE FEIGN
// @EnableJpaAuditing // If using config class
public class CashFlowServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CashFlowServiceApplication.class, args);
	}

}