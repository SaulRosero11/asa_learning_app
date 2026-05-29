package com.springboot.asa.learning.infrastructure.security;

import com.springboot.asa.learning.infrastructure.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DomainEligibilityService {

    private final AuthProperties authProperties;

    public boolean isEligible(String email) {
        if (email == null || !email.contains("@")) return false;
        String domain = email.substring(email.indexOf('@') + 1).toLowerCase();
        return domain.equals(authProperties.getAllowedAdminDomain().toLowerCase());
    }
}
