package com.innowise.user_service.mapper;

import com.innowise.user_service.dto.PaymentCardCreateDto;
import com.innowise.user_service.dto.PaymentCardDto;
import com.innowise.user_service.entity.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {
    @Mapping(source = "user.id", target = "userId")
    PaymentCardDto toDto(PaymentCard entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    PaymentCard toEntity(PaymentCardCreateDto dto);
}