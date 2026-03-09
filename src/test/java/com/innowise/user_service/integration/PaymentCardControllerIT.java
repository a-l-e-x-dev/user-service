package com.innowise.user_service.integration;

import com.innowise.user_service.dto.PaymentCardCreateDto;
import com.innowise.user_service.entity.User;
import com.innowise.user_service.repository.PaymentCardRepository;
import com.innowise.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PaymentCardControllerIT extends BaseIntegrationTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardRepository cardRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        cardRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setName("RANDOMNAME");
        user.setSurname("RANDOMLASTNAME");
        user.setEmail("bob@example.com");
        user.setActive(true);
        savedUser = userRepository.save(user);
    }

    @Test
    void shouldCreatePaymentCard() throws Exception {
        PaymentCardCreateDto cardDto = new PaymentCardCreateDto();
        cardDto.setNumber("1234567812345678"); // Ровно 16 символов
        cardDto.setHolder("RANDOMNAME RANDOMLASTNAME");
        cardDto.setExpirationDate(LocalDate.now().plusYears(1));
        cardDto.setActive(true);

        mockMvc.perform(post("/api/cards/user/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId", is(savedUser.getId().intValue())))
                .andExpect(jsonPath("$.number", is("1234567812345678")));
    }

    @Test
    void shouldFailWhenCardNumberIsInvalid() throws Exception {
        PaymentCardCreateDto cardDto = new PaymentCardCreateDto();
        cardDto.setNumber("123"); // Меньше 16 символов, должна сработать валидация DTO
        cardDto.setHolder("RANDOMNAME RANDOMLASTNAME");
        cardDto.setExpirationDate(LocalDate.now().plusYears(1));

        mockMvc.perform(post("/api/cards/user/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Error")));
    }
}