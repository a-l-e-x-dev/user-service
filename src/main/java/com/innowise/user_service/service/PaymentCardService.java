package com.innowise.user_service.service;

import com.innowise.user_service.entity.PaymentCard;
import com.innowise.user_service.entity.User;
import com.innowise.user_service.exception.BusinessValidationException;
import com.innowise.user_service.exception.ResourceNotFoundException;
import com.innowise.user_service.repository.PaymentCardRepository;
import com.innowise.user_service.repository.UserRepository;
import com.innowise.user_service.repository.specification.AppSpecifications;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentCardService {

    private final PaymentCardRepository cardRepository;
    private final UserService userService;
    private final CacheManager cacheManager;
    private final UserRepository userRepository;

    @CacheEvict(value = "users", key = "#userId")
    @Transactional
    public PaymentCard createCard(Long userId, PaymentCard card) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        long currentCardCount = cardRepository.countByUserId(userId);
        if (currentCardCount >= 5) {
            throw new BusinessValidationException("User already has maximum number of cards (5)");
        }

        card.setUser(user);
        return cardRepository.save(card);
    }

    @Transactional
    public PaymentCard updateCard(Long id, PaymentCard updatedData) {
        PaymentCard existingCard = getCardById(id);
        existingCard.setHolder(updatedData.getHolder());
        existingCard.setExpirationDate(updatedData.getExpirationDate());

        PaymentCard savedCard = cardRepository.save(existingCard);
        evictUserCache(savedCard.getUser().getId());
        return savedCard;
    }

    @Transactional
    public void changeCardStatus(Long id, boolean active) {
        PaymentCard card = getCardById(id);
        cardRepository.updateActiveStatusJpql(id, active);
        evictUserCache(card.getUser().getId());
    }

    @Transactional
    public void deleteCard(Long id) {
        PaymentCard card = getCardById(id);
        cardRepository.deleteById(id);
        evictUserCache(card.getUser().getId());
    }

    @Transactional(readOnly = true)
    public PaymentCard getCardById(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
    }

    private void evictUserCache(Long userId) {
        if (cacheManager.getCache("users") != null) {
            cacheManager.getCache("users").evict(userId);
        }
    }

    @Transactional(readOnly = true)
    public Page<PaymentCard> getAllCards(String userName, String userSurname, Pageable pageable) {
        return cardRepository.findAll(AppSpecifications.filterCardsByUser(userName, userSurname), pageable);
    }

    @Transactional(readOnly = true)
    public List<PaymentCard> getCardsByUserId(Long userId) {
        return cardRepository.findAllByUserId(userId);
    }
}