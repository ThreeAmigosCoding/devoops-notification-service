package com.devoops.notification.controller;

import com.devoops.notification.config.UserContext;
import com.devoops.notification.config.UserContextResolver;
import com.devoops.notification.dto.request.NotificationPreferencesUpdateRequest;
import com.devoops.notification.dto.response.NotificationPreferencesResponse;
import com.devoops.notification.exception.GlobalExceptionHandler;
import com.devoops.notification.exception.PreferencesNotFoundException;
import com.devoops.notification.service.NotificationPreferencesService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationPreferencesController Tests")
class NotificationPreferencesControllerTest {

    @Mock
    private NotificationPreferencesService preferencesService;

    @InjectMocks
    private NotificationPreferencesController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UUID testUserId;
    private String testUserEmail;
    private NotificationPreferencesResponse testResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        testUserId = UUID.randomUUID();
        testUserEmail = "test@example.com";

        testResponse = new NotificationPreferencesResponse(
                testUserId,
                testUserEmail,
                true, true, true, true, true
        );

        // Create a custom argument resolver that provides UserContext
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new TestUserContextResolver(testUserId))
                .build();
    }

    @Nested
    @DisplayName("GET /api/notification/preferences")
    class GetPreferencesTests {

        @Test
        @DisplayName("Should return preferences when user exists")
        void getPreferences_WhenUserExists_ReturnsPreferences() throws Exception {
            // Given
            when(preferencesService.getPreferences(testUserId)).thenReturn(testResponse);

            // When/Then
            mockMvc.perform(get("/api/notification/preferences")
                            .header("X-User-Id", testUserId.toString())
                            .header("X-User-Role", "HOST"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.userId").value(testUserId.toString()))
                    .andExpect(jsonPath("$.userEmail").value(testUserEmail))
                    .andExpect(jsonPath("$.reservationRequestCreated").value(true))
                    .andExpect(jsonPath("$.reservationCancelled").value(true))
                    .andExpect(jsonPath("$.hostRated").value(true))
                    .andExpect(jsonPath("$.accommodationRated").value(true))
                    .andExpect(jsonPath("$.reservationResponse").value(true));
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        void getPreferences_WhenUserNotFound_Returns404() throws Exception {
            // Given
            when(preferencesService.getPreferences(testUserId))
                    .thenThrow(new PreferencesNotFoundException(testUserId));

            // When/Then
            mockMvc.perform(get("/api/notification/preferences")
                            .header("X-User-Id", testUserId.toString())
                            .header("X-User-Role", "HOST"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Preferences Not Found"));
        }
    }

    @Nested
    @DisplayName("PUT /api/notification/preferences")
    class UpdatePreferencesTests {

        @Test
        @DisplayName("Should update preferences successfully")
        void updatePreferences_WithValidRequest_ReturnsUpdated() throws Exception {
            // Given
            NotificationPreferencesUpdateRequest request = new NotificationPreferencesUpdateRequest(
                    false, false, true, true, true
            );
            NotificationPreferencesResponse updatedResponse = new NotificationPreferencesResponse(
                    testUserId, testUserEmail, false, false, true, true, true
            );
            when(preferencesService.updatePreferences(eq(testUserId), any(NotificationPreferencesUpdateRequest.class)))
                    .thenReturn(updatedResponse);

            // When/Then
            mockMvc.perform(put("/api/notification/preferences")
                            .header("X-User-Id", testUserId.toString())
                            .header("X-User-Role", "GUEST")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.reservationRequestCreated").value(false))
                    .andExpect(jsonPath("$.reservationCancelled").value(false));
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        void updatePreferences_WhenUserNotFound_Returns404() throws Exception {
            // Given
            NotificationPreferencesUpdateRequest request = new NotificationPreferencesUpdateRequest(
                    false, false, false, false, false
            );
            when(preferencesService.updatePreferences(eq(testUserId), any(NotificationPreferencesUpdateRequest.class)))
                    .thenThrow(new PreferencesNotFoundException(testUserId));

            // When/Then
            mockMvc.perform(put("/api/notification/preferences")
                            .header("X-User-Id", testUserId.toString())
                            .header("X-User-Role", "HOST")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    /**
     * Test helper to provide UserContext in MockMvc tests
     */
    private static class TestUserContextResolver implements org.springframework.web.method.support.HandlerMethodArgumentResolver {
        private final UUID userId;

        TestUserContextResolver(UUID userId) {
            this.userId = userId;
        }

        @Override
        public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
            return parameter.getParameterType().equals(UserContext.class);
        }

        @Override
        public Object resolveArgument(org.springframework.core.MethodParameter parameter,
                                      org.springframework.web.method.support.ModelAndViewContainer mavContainer,
                                      org.springframework.web.context.request.NativeWebRequest webRequest,
                                      org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
            String role = webRequest.getHeader("X-User-Role");
            return new UserContext(userId, role != null ? role : "HOST");
        }
    }
}
