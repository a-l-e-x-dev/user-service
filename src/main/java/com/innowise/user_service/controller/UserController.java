package com.innowise.user_service.controller;

import com.innowise.user_service.dto.UserCreateDto;
import com.innowise.user_service.dto.UserDto;
import com.innowise.user_service.dto.UserUpdateDto;
import com.innowise.user_service.entity.User;
import com.innowise.user_service.mapper.UserMapper;
import com.innowise.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserCreateDto createDto) {
        User user = userMapper.toEntity(createDto);
        UserDto createdUser = userMapper.toDto(userService.createUser(user));
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto userDto = userMapper.toDto(userService.getUserById(id));
        return ResponseEntity.ok(userDto);
    }

    @GetMapping
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            Pageable pageable) {
        Page<UserDto> users = userService.getAllUsers(name, surname, pageable).map(userMapper::toDto);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDto updateDto) {
        User updateData = new User();
        updateData.setName(updateDto.getName());
        updateData.setSurname(updateDto.getSurname());
        updateData.setBirthDate(updateDto.getBirthDate());
        updateData.setEmail(updateDto.getEmail());

        UserDto updatedUser = userMapper.toDto(userService.updateUser(id, updateData));
        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> changeStatus(@PathVariable Long id, @RequestParam boolean active) {
        userService.changeUserStatus(id, active);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}