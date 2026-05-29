package com.springboot.asa.learning.infrastructure.security;

import com.springboot.asa.learning.infrastructure.config.SeedProperties;
import com.springboot.asa.learning.infrastructure.persistence.entity.RoleEntity;
import com.springboot.asa.learning.infrastructure.persistence.entity.UserEntity;
import com.springboot.asa.learning.infrastructure.persistence.repository.JpaRoleRepository;
import com.springboot.asa.learning.infrastructure.persistence.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeedAdminRunner implements ApplicationRunner {

    private final JpaUserRepository userRepository;
    private final JpaRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SeedProperties seedProperties;

    private static final String CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*";

    /** Roles base que siempre deben existir en la tabla roles. */
    private static final String[][] DEFAULT_ROLES = {
            {"STUDENT",        "Beneficiario matriculado en programas de capacitación"},
            {"PROGRAM_LEADER", "Coordinador responsable de un programa específico"},
            {"ADMIN",          "Administrador con acceso a todos los programas"},
            {"SUPER_ADMIN",    "Administrador global con acceso total al sistema"}
    };

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        ensureRolesExist();      // Paso 1: roles en tabla roles
        ensureSuperAdminExists(); // Paso 2: usuario SUPER_ADMIN
    }

    /**
     * Garantiza que los 4 roles base existan en la tabla `roles`.
     * Flyway los crea normalmente (V1__create_roles.sql), pero este método
     * actúa como fallback si la BD está vacía o Flyway aún no corrió.
     */
    private void ensureRolesExist() {
        for (String[] roleData : DEFAULT_ROLES) {
            String name = roleData[0];
            String desc = roleData[1];
            if (roleRepository.findByName(name).isEmpty()) {
                RoleEntity role = new RoleEntity();
                role.setName(name);
                role.setDescription(desc);
                roleRepository.save(role);
                log.info("[Seed] Rol '{}' creado en la BD.", name);
            }
        }
    }

    /**
     * Crea el primer SUPER_ADMIN si no existe ningún usuario con ese rol.
     * Imprime las credenciales en consola nivel WARN una sola vez.
     */
    private void ensureSuperAdminExists() {
        String email = seedProperties.getAdminEmail();

        // Doble verificación: por rol Y por email — evita duplicado si la query de roles falla
        if (userRepository.existsByRoleName("SUPER_ADMIN") || userRepository.existsByEmail(email)) {
            log.info("[Seed] SUPER_ADMIN ya existe. Sin acción necesaria.");
            return;
        }
        String rawPassword = generateSecurePassword(16);

        // El rol SUPER_ADMIN ya existe gracias a ensureRolesExist()
        RoleEntity superAdminRole = roleRepository.findByName("SUPER_ADMIN")
                .orElseThrow(() -> new IllegalStateException(
                        "[Seed] Error crítico: el rol SUPER_ADMIN no existe tras el seed de roles."));

        UserEntity admin = new UserEntity();
        admin.setEmail(email);
        admin.setPasswordHash(passwordEncoder.encode(rawPassword));
        admin.setAuthProvider("INTERNAL");
        admin.setActive(true);
        admin.setAdminEligible(true);
        admin.setMustChangePassword(true);   // fuerza cambio en primer login
        admin.setOnboardingCompleted(true);   // no necesita onboarding
        admin.setRoles(Set.of(superAdminRole));

        userRepository.save(admin);

        log.warn("""
                \n╔══════════════════════════════════════════════════════════════╗
                ║   ASA — SEED ADMIN CREADO. CAMBIA ESTAS CREDENCIALES YA.   ║
                ║                                                              ║
                ║   Email:      {}
                ║   Password:   {}
                ║                                                              ║
                ║   Inicia sesión y cambia la contraseña de inmediato.        ║
                ╚══════════════════════════════════════════════════════════════╝
                """, email, rawPassword);
    }

    private String generateSecurePassword(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
