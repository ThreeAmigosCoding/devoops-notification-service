package com.devoops.notification.service;

import com.devoops.notification.dto.message.UserCreatedMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserEventConsumerService Tests")
class UserEventConsumerServiceTest {

    @Mock
    private NotificationPreferencesService preferencesService;

    @InjectMocks
    private UserEventConsumerService consumerService;

    private UUID testUserId;
    private String testUserEmail;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUserEmail = "newuser@example.com";
    }

    @Nested
    @DisplayName("handleUserCreated")
    class HandleUserCreatedTests {

        @Test
        @DisplayName("Should initialize preferences for new user")
        void handleUserCreated_InitializesPreferences() {
            // Given
            UserCreatedMessage message = UserCreatedMessage.builder()
                    .userId(testUserId)
                    .userEmail(testUserEmail)
                    .build();

            // When
            consumerService.handleUserCreated(message);

            // Then
            verify(preferencesService).initializePreferences(testUserId, testUserEmail);
        }
    }
}
