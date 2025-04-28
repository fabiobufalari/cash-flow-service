package com.bufalari.cashflow.client; // Pacote correto

import com.bufalari.cashflow.dto.UserDetailsDTO; // Pacote correto
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// <<< AJUSTE NO NOME E URL >>>
@FeignClient(name = "auth-service-client-cashflow", url = "${auth.service.url}")
public interface AuthServiceClient {

    // <<< AJUSTE NO PATH >>>
    @GetMapping("/users/username/{username}")
    UserDetailsDTO getUserByUsername(@PathVariable("username") String username);

    // <<< AJUSTE NO PATH >>>
    @GetMapping("/users/{id}")
    UserDetailsDTO getUserById(@PathVariable("id") String userId);
}