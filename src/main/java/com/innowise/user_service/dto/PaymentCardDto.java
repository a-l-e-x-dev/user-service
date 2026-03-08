package com.innowise.user_service.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PaymentCardDto {
    private Long id;
    private Long userId;
    private String number;
    private String holder;
    private LocalDate expirationDate;
    private Boolean active;
}

