package com.ecommerce.user.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.models.User;
import com.ecommerce.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldReturnUserById() {
        User user = new User();
        user.setId("user123");
        user.setEmail("test@example.com");

        when(userRepository.findById("user123")).thenReturn(Optional.of(user));

        Optional<UserResponse> found = userService.fetchUser("user123");

        assertEquals("test@example.com", found.get().getEmail());
        verify(userRepository).findById("user123");
    }
}
