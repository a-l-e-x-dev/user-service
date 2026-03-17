package com.innowise.user_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.user_service.entity.User;
import com.innowise.user_service.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.security.Key;
import java.time.LocalDate;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class UserControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        User user = new User();
        user.setName("John");
        user.setSurname("Doe");
        user.setEmail("john.doe@example.com");
        user.setActive(true);
        user.setBirthDate(LocalDate.of(1990, 1, 1));

        testUser = userRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    private String generateTestToken(Long userId, String role) {
        String secret = "NDQ1Mjc1Mzg1OTU1NjQ1MzY1NDQxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTA=";
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        Key key = Keys.hmacShaKeyFor(keyBytes);

        String token = Jwts.builder()
                .setSubject(testUser.getEmail())
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return "Bearer " + token;
    }

    @Test
    void shouldGetMyProfile() throws Exception {
        String token = generateTestToken(testUser.getId(), "ROLE_USER");

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void shouldUpdateMyProfile() throws Exception {
        String token = generateTestToken(testUser.getId(), "ROLE_USER");

        User updateDto = new User();
        updateDto.setName("Jane");
        updateDto.setSurname("Smith");
        updateDto.setEmail("jane.smith@example.com");
        updateDto.setActive(true);
        updateDto.setBirthDate(LocalDate.of(1995, 5, 5));

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isOk()) // Ожидаем 200 OK
                .andExpect(jsonPath("$.name").value("Jane")); // Проверяем, что имя обновилось
    }

    @Test
    void shouldReturn401WhenRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andDo(print())
                .andExpect(status().isUnauthorized()); // Ожидаем 401
    }

    @Test
    void shouldGetAnyUserAsAdmin() throws Exception {
        String adminToken = generateTestToken(999L, "ROLE_ADMIN");
        mockMvc.perform(get("/api/users/" + testUser.getId())
                        .header("Authorization", adminToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }
}