package com.innowise.user_service.service;

import com.innowise.user_service.entity.User;
import com.innowise.user_service.exception.BusinessValidationException;
import com.innowise.user_service.exception.ResourceNotFoundException;
import com.innowise.user_service.repository.UserRepository;
import com.innowise.user_service.repository.specification.AppSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

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

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<User> getAllUsers(String name, String surname, Pageable pageable) {
        return userRepository.findAll(AppSpecifications.filterUsers(name, surname), pageable);
    }

    @Transactional
    public User updateUser(Long id, User updatedData) {
        User existingUser = getUserById(id);
        existingUser.setName(updatedData.getName());
        existingUser.setSurname(updatedData.getSurname());
        existingUser.setBirthDate(updatedData.getBirthDate());
        existingUser.setEmail(updatedData.getEmail());
        return userRepository.save(existingUser);
    }

    @Transactional
    public void changeUserStatus(Long id, boolean active) {
        userRepository.updateActiveStatusNative(id, active);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}