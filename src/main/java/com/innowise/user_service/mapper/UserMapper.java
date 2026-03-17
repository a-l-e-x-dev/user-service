package com.innowise.user_service.mapper;

import com.innowise.user_service.dto.UserCreateDto;
import com.innowise.user_service.dto.UserDto;
import com.innowise.user_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = PaymentCardMapper.class)
public interface UserMapper {
    UserDto toDto(User entity);

    @Mapping(target = "id", ignore = true)
    User toEntity(UserCreateDto dto);
}