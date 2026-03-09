package com.innowise.user_service.integration;

import com.innowise.user_service.dto.UserCreateDto;
import com.innowise.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerIT extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateAndFetchUser() throws Exception {
        UserCreateDto userDto = new UserCreateDto();
        userDto.setName("Alice");
        userDto.setSurname("Smith");
        userDto.setEmail("alice.smith@example.com");
        userDto.setBirthDate(LocalDate.of(1995, 8, 20));
        userDto.setActive(true);

        String responseJson = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Alice")))
                .andExpect(jsonPath("$.email", is("alice.smith@example.com")))
                .andReturn().getResponse().getContentAsString();

        Long userId = objectMapper.readTree(responseJson).get("id").asLong();

        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId.intValue())))
                .andExpect(jsonPath("$.surname", is("Smith")));
    }

    @Test
    void shouldReturnBadRequestWhenValidationFails() throws Exception {
        UserCreateDto invalidUser = new UserCreateDto();
        invalidUser.setName("");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Error")));
    }
}