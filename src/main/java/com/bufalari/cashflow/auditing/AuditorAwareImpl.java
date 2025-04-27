// Path: src/main/java/com/bufalari/cashflow/auditing/AuditorAwareImpl.java
package com.bufalari.cashflow.auditing;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
// import org.springframework.stereotype.Component; // REMOVE

import java.util.Optional;

// NO @Component annotation
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            // <<<--- ADJUST HERE / AJUSTE AQUI ---<<<
            return Optional.of("system_cashflow"); // System user specific to this service
        }
        // ... (rest of the logic as before)
        Object principal = authentication.getPrincipal();
         String username;
         if (principal instanceof User) {
             username = ((User) principal).getUsername();
         } else if (principal instanceof String) {
             username = (String) principal;
         } else {
             return Optional.of("unknown_user");
         }
         return Optional.of(username);
    }
}