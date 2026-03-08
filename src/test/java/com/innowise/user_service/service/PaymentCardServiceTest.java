package com.innowise.user_service.service;

import com.innowise.user_service.entity.PaymentCard;
import com.innowise.user_service.entity.User;
import com.innowise.user_service.exception.BusinessValidationException;
import com.innowise.user_service.exception.ResourceNotFoundException;
import com.innowise.user_service.repository.PaymentCardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCardServiceTest {

    @Mock
    private PaymentCardRepository cardRepository;

    @Mock
    private UserService userService;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private PaymentCardService cardService;

    private User testUser;
    private PaymentCard testCard;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);

        testCard = new PaymentCard();
        testCard.setId(10L);
        testCard.setNumber("1234567812345678");
        testCard.setHolder("JOHN DOE");
        testCard.setExpirationDate(LocalDate.now().plusYears(2));
        testCard.setActive(true);
        testCard.setUser(testUser);
    }

    @Test
    void createCard_Success() {
        when(cardRepository.countByUserId(1L)).thenReturn(2L);
        when(userService.getUserById(1L)).thenReturn(testUser);
        when(cardRepository.save(any(PaymentCard.class))).thenReturn(testCard);

        PaymentCard createdCard = cardService.createCard(1L, testCard);

        assertNotNull(createdCard);
        assertEquals(1L, createdCard.getUser().getId());
        verify(cardRepository, times(1)).save(testCard);
    }

    @Test
    void createCard_ThrowsException_WhenUserHas5Cards() {
        when(cardRepository.countByUserId(1L)).thenReturn(5L);

        assertThrows(BusinessValidationException.class, () -> cardService.createCard(1L, testCard));
        verify(cardRepository, never()).save(any(PaymentCard.class));
    }

    @Test
    void getCardById_Success() {
        when(cardRepository.findById(10L)).thenReturn(Optional.of(testCard));

        PaymentCard foundCard = cardService.getCardById(10L);

        assertNotNull(foundCard);
        assertEquals(10L, foundCard.getId());
    }

    @Test
    void getCardById_NotFound_ThrowsException() {
        when(cardRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cardService.getCardById(10L));
    }

    @Test
    void updateCard_Success() {
        PaymentCard updatedData = new PaymentCard();
        updatedData.setHolder("NEW HOLDER");

        when(cardRepository.findById(10L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(PaymentCard.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cacheManager.getCache("users")).thenReturn(cache);

        PaymentCard result = cardService.updateCard(10L, updatedData);

        assertEquals("NEW HOLDER", result.getHolder());
        verify(cache, times(1)).evict(1L); // Проверяем, что кэш юзера сбросился
        verify(cardRepository, times(1)).save(testCard);
    }

    @Test
    void changeCardStatus_Success() {
        when(cardRepository.findById(10L)).thenReturn(Optional.of(testCard));
        doNothing().when(cardRepository).updateActiveStatusJpql(10L, false);
        when(cacheManager.getCache("users")).thenReturn(cache);

        cardService.changeCardStatus(10L, false);

        verify(cardRepository, times(1)).updateActiveStatusJpql(10L, false);
        verify(cache, times(1)).evict(1L);
    }

    @Test
    void deleteCard_Success() {
        when(cardRepository.findById(10L)).thenReturn(Optional.of(testCard));
        doNothing().when(cardRepository).deleteById(10L);
        when(cacheManager.getCache("users")).thenReturn(cache);

        cardService.deleteCard(10L);

        verify(cardRepository, times(1)).deleteById(10L);
        verify(cache, times(1)).evict(1L);
    }

    @Test
    void getAllCards_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PaymentCard> cardPage = new PageImpl<>(List.of(testCard));

        when(cardRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(cardPage);

        Page<PaymentCard> result = cardService.getAllCards("John", null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getCardsByUserId_ReturnsList() {
        when(cardRepository.findAllByUserId(1L)).thenReturn(List.of(testCard));

        List<PaymentCard> result = cardService.getCardsByUserId(1L);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }
}