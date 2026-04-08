package com.innowise.user_service.service;

import com.innowise.user_service.dto.UserDto;
import com.innowise.user_service.entity.User;
import com.innowise.user_service.exception.BusinessValidationException;
import com.innowise.user_service.exception.ResourceNotFoundException;
import com.innowise.user_service.mapper.UserMapper;
import com.innowise.user_service.repository.UserRepository;
import com.innowise.user_service.repository.specification.AppSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public User createUser(User user) {
        if (user.getPaymentCards() != null && !user.getPaymentCards().isEmpty()) {
            if (user.getPaymentCards().size() > 5) {
                throw new BusinessValidationException("A user can have a maximum of 5 cards.");
            }
            user.getPaymentCards().forEach(card -> card.setUser(user));
        }
        return userRepository.save(user);
    }

    @Cacheable(value = "users", key = "#id")
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @CachePut(value = "users", key = "#id")
    @Transactional
    public User updateUser(Long id, User updatedData) {
        User existingUser = getUserById(id);
        existingUser.setName(updatedData.getName());
        existingUser.setSurname(updatedData.getSurname());
        existingUser.setBirthDate(updatedData.getBirthDate());
        existingUser.setEmail(updatedData.getEmail());
        return userRepository.save(existingUser);
    }

    @CacheEvict(value = "users", key = "#id")
    @Transactional
    public void changeUserStatus(Long id, boolean active) {
        userRepository.updateActiveStatusNative(id, active);
    }

    @CacheEvict(value = "users", key = "#id")
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<User> getAllUsers(String name, String surname, Pageable pageable) {
        return userRepository.findAll(AppSpecifications.filterUsers(name, surname), pageable);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getUsersByIds(Set<Long> ids) {
        return userRepository.findAllById(ids).stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }
}