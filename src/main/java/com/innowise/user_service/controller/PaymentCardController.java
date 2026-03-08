package com.innowise.user_service.controller;

import com.innowise.user_service.dto.PaymentCardCreateDto;
import com.innowise.user_service.dto.PaymentCardDto;
import com.innowise.user_service.dto.PaymentCardUpdateDto;
import com.innowise.user_service.entity.PaymentCard;
import com.innowise.user_service.mapper.PaymentCardMapper;
import com.innowise.user_service.service.PaymentCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class PaymentCardController {

    private final PaymentCardService cardService;
    private final PaymentCardMapper cardMapper;

    @PostMapping("/user/{userId}")
    public ResponseEntity<PaymentCardDto> createCard(@PathVariable Long userId, @Valid @RequestBody PaymentCardCreateDto createDto) {
        PaymentCard card = cardMapper.toEntity(createDto);
        PaymentCardDto createdCard = cardMapper.toDto(cardService.createCard(userId, card));
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCard);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentCardDto> getCardById(@PathVariable Long id) {
        PaymentCardDto cardDto = cardMapper.toDto(cardService.getCardById(id));
        return ResponseEntity.ok(cardDto);
    }

    @GetMapping
    public ResponseEntity<Page<PaymentCardDto>> getAllCards(
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String userSurname,
            Pageable pageable) {
        Page<PaymentCardDto> cards = cardService.getAllCards(userName, userSurname, pageable).map(cardMapper::toDto);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentCardDto>> getCardsByUserId(@PathVariable Long userId) {
        List<PaymentCardDto> cards = cardService.getCardsByUserId(userId).stream()
                .map(cardMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(cards);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentCardDto> updateCard(@PathVariable Long id, @Valid @RequestBody PaymentCardUpdateDto updateDto) {
        PaymentCard updateData = new PaymentCard();
        updateData.setHolder(updateDto.getHolder());
        updateData.setExpirationDate(updateDto.getExpirationDate());

        PaymentCardDto updatedCard = cardMapper.toDto(cardService.updateCard(id, updateData));
        return ResponseEntity.ok(updatedCard);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> changeStatus(@PathVariable Long id, @RequestParam boolean active) {
        cardService.changeCardStatus(id, active);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}