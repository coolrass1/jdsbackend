package com.skk.jdsbackend.service;

import com.skk.jdsbackend.dto.UserResponse;
import com.skk.jdsbackend.entity.User;
import com.skk.jdsbackend.exception.ResourceNotFoundException;
import com.skk.jdsbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

        private final UserRepository userRepository;

        @Transactional(readOnly = true)
        public List<UserResponse> getAllUsers() {
                return userRepository.findAll().stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public UserResponse getUserById(Long id) {
                User user = userRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
                return mapToResponse(user);
        }

        @Transactional
        public void deleteUser(Long id) {
                User user = userRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
                userRepository.delete(user);
        }

        private UserResponse mapToResponse(User user) {
                return new UserResponse(
                                user.getId(),
                                user.getUsername(),
                                user.getEmail(),
                                user.getRoles().stream()
                                                .map(Enum::name)
                                                .collect(Collectors.toList()),
                                null, // No longer tracking assigned clients on User
                                user.getCreatedAt());
        }
}
