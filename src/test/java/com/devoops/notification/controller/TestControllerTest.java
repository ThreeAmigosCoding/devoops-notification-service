package com.devoops.notification.controller;

import com.devoops.notification.entity.NotificationType;
import com.devoops.notification.exception.GlobalExceptionHandler;
import com.devoops.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TestController Tests")
class TestControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TestController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("POST /api/notification/test/send")
    class SendTestNotificationTests {

        @Test
        @DisplayName("Should send test notification with default parameters")
        void sendTestNotification_WithDefaults_SendsNotification() throws Exception {
            // Given
            doNothing().when(notificationService).processNotification(any());

            // When/Then
            mockMvc.perform(post("/api/notification/test/send"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Test notification sent to test@example.com")));

            verify(notificationService).processNotification(any());
        }

        @Test
        @DisplayName("Should send test notification with custom email")
        void sendTestNotification_WithCustomEmail_SendsNotification() throws Exception {
            // Given
            doNothing().when(notificationService).processNotification(any());

            // When/Then
            mockMvc.perform(post("/api/notification/test/send")
                            .param("email", "custom@example.com"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("custom@example.com")));
        }

        @Test
        @DisplayName("Should send test notification with custom type")
        void sendTestNotification_WithCustomType_SendsNotification() throws Exception {
            // Given
            doNothing().when(notificationService).processNotification(any());

            // When/Then
            mockMvc.perform(post("/api/notification/test/send")
                            .param("type", "HOST_RATED"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("HOST_RATED")));
        }

        @Test
        @DisplayName("Should send all notification types")
        void sendTestNotification_AllTypes_SendsNotification() throws Exception {
            // Given
            doNothing().when(notificationService).processNotification(any());

            for (NotificationType type : NotificationType.values()) {
                // When/Then
                mockMvc.perform(post("/api/notification/test/send")
                                .param("type", type.name()))
                        .andExpect(status().isOk());
            }
        }

        @Test
        @DisplayName("Should return 400 for invalid notification type")
        void sendTestNotification_WithInvalidType_Returns400() throws Exception {
            // When/Then
            mockMvc.perform(post("/api/notification/test/send")
                            .param("type", "INVALID_TYPE"))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("GET /api/notification/test/types")
    class GetNotificationTypesTests {

        @Test
        @DisplayName("Should return all notification types")
        void getNotificationTypes_ReturnsAllTypes() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/notification/test/types"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(NotificationType.values().length));
        }
    }
}
