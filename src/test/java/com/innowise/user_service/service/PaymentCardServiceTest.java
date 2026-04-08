package com.innowise.user_service.service;

import com.innowise.user_service.entity.PaymentCard;
import com.innowise.user_service.entity.User;
import com.innowise.user_service.exception.BusinessValidationException;
import com.innowise.user_service.repository.PaymentCardRepository;
import com.innowise.user_service.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @InjectMocks
    private PaymentCardService paymentCardService;

    private User mockUser;
    private PaymentCard mockDto;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("John");

        mockDto = new PaymentCard();
        mockDto.setNumber("1234567812345678");
        mockDto.setHolder("JOHN DOE");
    }

    @Test
    void createCard_Success_WhenUserHasLessThan5Cards() {
        Long userId = 1L;
        when(userRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(mockUser));
        when(paymentCardRepository.countByUserId(userId)).thenReturn(4L);
        when(paymentCardRepository.save(any(PaymentCard.class))).thenAnswer(invocation -> {
            PaymentCard card = invocation.getArgument(0);
            card.setId(100L);
            return card;
        });

        PaymentCard createdCard = paymentCardService.createCard(userId, mockDto);

        assertNotNull(createdCard);
        assertEquals(mockUser, createdCard.getUser());
        assertEquals("1234567812345678", createdCard.getNumber());

        verify(paymentCardRepository, times(1)).save(any(PaymentCard.class));
    }

    @Test
    void createCard_ThrowsException_WhenUserHas5Cards() {
        Long userId = 1L;
        when(userRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(mockUser));

        when(paymentCardRepository.countByUserId(userId)).thenReturn(5L);

        BusinessValidationException exception = assertThrows(BusinessValidationException.class, () -> {
            paymentCardService.createCard(userId, mockDto);
        });

        assertNotNull(exception.getMessage());

        verify(paymentCardRepository, never()).save(any(PaymentCard.class));
    }

    @Test
    void createCard_ThrowsException_WhenUserNotFound() {
        Long userId = 999L;
        when(userRepository.findByIdForUpdate(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            paymentCardService.createCard(userId, mockDto);
        });

        verify(paymentCardRepository, never()).countByUserId(any());
        verify(paymentCardRepository, never()).save(any());
    }
}