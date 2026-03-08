package com.innowise.user_service.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PaymentCardUpdateDto {
    @NotBlank(message = "Holder name is mandatory")
    private String holder;

    @Future(message = "Expiration date must be in the future")
    private LocalDate expirationDate;
}
