package com.springboot.asa.learning.presentation.controller;

import com.springboot.asa.learning.infrastructure.persistence.repository.JpaInvitationRepository;
import com.springboot.asa.learning.infrastructure.persistence.repository.JpaProgramRepository;
import com.springboot.asa.learning.infrastructure.persistence.repository.JpaUserRepository;
import com.springboot.asa.learning.presentation.dto.response.InvitationValidationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final JpaInvitationRepository invitationRepo;
    private final JpaProgramRepository programRepo;
    private final JpaUserRepository userRepo;

    @GetMapping("/validate/{token}")
    public ResponseEntity<InvitationValidationResponse> validateInvitation(
            @PathVariable String token) {

        var invitation = invitationRepo.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Invitación no encontrada"));

        String programName = programRepo.findById(invitation.getProgramId())
                .map(p -> p.getName()).orElse("Programa");

        boolean userExists = userRepo.existsByEmail(invitation.getEmail());

        return ResponseEntity.ok(new InvitationValidationResponse(
                invitation.getEmail(),   // email completo — el invitado ya lo conoce
                programName,
                invitation.getProgramId(),
                invitation.getStatus(),
                userExists,
                invitation.getExpiresAt()
        ));
    }
}
