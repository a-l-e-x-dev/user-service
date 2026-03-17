package com.innowise.user_service.controller;

import com.innowise.user_service.entity.PaymentCard;
import com.innowise.user_service.service.PaymentCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class PaymentCardController {

    private final PaymentCardService cardService;

    //user endpoints
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<List<PaymentCard>> getMyCards(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(cardService.getCardsByUserId(userId));
    }

    @PostMapping("/my")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<PaymentCard> createMyCard(@RequestBody PaymentCard card, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.createCard(userId, card));
    }

    @DeleteMapping("/my/{cardId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Void> deleteMyCard(@PathVariable Long cardId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        PaymentCard card = cardService.getCardById(cardId);

        // Защита: проверяем, что карта действительно принадлежит этому пользователю
        if (!card.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to delete this card");
        }

        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }

    //admin endpoints
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Page<PaymentCard>> getAllCards(
            @RequestParam(required = false) String holder,
            @RequestParam(required = false) String number,
            Pageable pageable) {
        return ResponseEntity.ok(cardService.getAllCards(holder, number, pageable));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<PaymentCard>> getCardsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(cardService.getCardsByUserId(userId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<PaymentCard> updateCard(@PathVariable Long id, @RequestBody PaymentCard updatedData) {
        return ResponseEntity.ok(cardService.updateCard(id, updatedData));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> changeCardStatus(@PathVariable Long id, @RequestParam boolean active) {
        cardService.changeCardStatus(id, active);
        return ResponseEntity.ok().build();
    }
}
