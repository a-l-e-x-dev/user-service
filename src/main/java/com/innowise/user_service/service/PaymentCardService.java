package com.innowise.user_service.service;

import com.innowise.user_service.entity.PaymentCard;
import com.innowise.user_service.entity.User;
import com.innowise.user_service.exception.BusinessValidationException;
import com.innowise.user_service.exception.ResourceNotFoundException;
import com.innowise.user_service.repository.PaymentCardRepository;
import com.innowise.user_service.repository.specification.AppSpecifications;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public PaymentCard createCard(Long userId, PaymentCard card) {
        long currentCardCount = cardRepository.countByUserId(userId);
        if (currentCardCount >= 5) {
            throw new BusinessValidationException("User already has the maximum of 5 cards allowed.");
        }

        User user = userService.getUserById(userId);
        card.setUser(user);
        return cardRepository.save(card);
    }

    @Transactional(readOnly = true)
    public PaymentCard getCardById(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<PaymentCard> getAllCards(String userName, String userSurname, Pageable pageable) {
        return cardRepository.findAll(AppSpecifications.filterCardsByUser(userName, userSurname), pageable);
    }

    @Transactional(readOnly = true)
    public List<PaymentCard> getCardsByUserId(Long userId) {
        return cardRepository.findAllByUserId(userId);
    }

    @Transactional
    public PaymentCard updateCard(Long id, PaymentCard updatedData) {
        PaymentCard existingCard = getCardById(id);
        existingCard.setHolder(updatedData.getHolder());
        existingCard.setExpirationDate(updatedData.getExpirationDate());
        return cardRepository.save(existingCard);
    }

    @Transactional
    public void changeCardStatus(Long id, boolean active) {
        cardRepository.updateActiveStatusJpql(id, active);
    }

    @Transactional
    public void deleteCard(Long id) {
        cardRepository.deleteById(id);
    }
}