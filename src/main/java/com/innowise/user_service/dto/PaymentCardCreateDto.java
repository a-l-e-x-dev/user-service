package com.innowise.user_service.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PaymentCardCreateDto {
    @NotBlank(message = "Card number is mandatory")
    @Size(min = 16, max = 16, message = "Card number must be exactly 16 characters")
    private String number;

    @NotBlank(message = "Holder name is mandatory")
    private String holder;

    @Future(message = "Expiration date must be in the future")
    private LocalDate expirationDate;

    private Boolean active = true;
}
