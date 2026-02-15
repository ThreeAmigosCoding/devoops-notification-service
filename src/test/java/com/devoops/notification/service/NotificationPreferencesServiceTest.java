package com.devoops.notification.service;

import com.devoops.notification.dto.request.NotificationPreferencesUpdateRequest;
import com.devoops.notification.dto.response.NotificationPreferencesResponse;
import com.devoops.notification.entity.NotificationPreferences;
import com.devoops.notification.entity.NotificationType;
import com.devoops.notification.entity.Preferences;
import com.devoops.notification.exception.PreferencesNotFoundException;
import com.devoops.notification.mapper.NotificationPreferencesMapper;
import com.devoops.notification.repository.NotificationPreferencesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationPreferencesService Tests")
class NotificationPreferencesServiceTest {

    @Mock
    private NotificationPreferencesRepository repository;

    @Mock
    private NotificationPreferencesMapper mapper;

    @InjectMocks
    private NotificationPreferencesService service;

    private UUID testUserId;
    private String testUserEmail;
    private NotificationPreferences testPreferences;
    private NotificationPreferencesResponse testResponse;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUserEmail = "test@example.com";

        Preferences prefs = new Preferences();
        prefs.setReservationRequestCreated(true);
        prefs.setReservationCancelled(true);
        prefs.setHostRated(true);
        prefs.setAccommodationRated(true);
        prefs.setReservationResponse(true);

        testPreferences = new NotificationPreferences(testUserId, testUserEmail);
        testPreferences.setPreferences(prefs);

        testResponse = new NotificationPreferencesResponse(
                testUserId,
                testUserEmail,
                true, true, true, true, true
        );
    }

    @Nested
    @DisplayName("getPreferences")
    class GetPreferencesTests {

        @Test
        @DisplayName("Should return preferences when user exists")
        void getPreferences_WhenUserExists_ReturnsPreferences() {
            // Given
            when(repository.findByUserId(testUserId)).thenReturn(Optional.of(testPreferences));
            when(mapper.toResponse(testPreferences)).thenReturn(testResponse);

            // When
            NotificationPreferencesResponse result = service.getPreferences(testUserId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.userId()).isEqualTo(testUserId);
            assertThat(result.userEmail()).isEqualTo(testUserEmail);
            assertThat(result.reservationRequestCreated()).isTrue();
            verify(repository).findByUserId(testUserId);
            verify(mapper).toResponse(testPreferences);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void getPreferences_WhenUserNotFound_ThrowsException() {
            // Given
            when(repository.findByUserId(testUserId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> service.getPreferences(testUserId))
                    .isInstanceOf(PreferencesNotFoundException.class)
                    .hasMessageContaining(testUserId.toString());
            verify(repository).findByUserId(testUserId);
            verify(mapper, never()).toResponse(any());
        }
    }

    @Nested
    @DisplayName("initializePreferences")
    class InitializePreferencesTests {

        @Test
        @DisplayName("Should create new preferences when user does not exist")
        void initializePreferences_WhenUserNotExists_CreatesNew() {
            // Given
            when(repository.existsByUserId(testUserId)).thenReturn(false);
            when(repository.save(any(NotificationPreferences.class))).thenReturn(testPreferences);
            when(mapper.toResponse(testPreferences)).thenReturn(testResponse);

            // When
            NotificationPreferencesResponse result = service.initializePreferences(testUserId, testUserEmail);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.userId()).isEqualTo(testUserId);
            verify(repository).existsByUserId(testUserId);
            verify(repository).save(any(NotificationPreferences.class));
            verify(mapper).toResponse(any());
        }

        @Test
        @DisplayName("Should return existing preferences when user already exists")
        void initializePreferences_WhenUserExists_ReturnsExisting() {
            // Given
            when(repository.existsByUserId(testUserId)).thenReturn(true);
            when(repository.findByUserId(testUserId)).thenReturn(Optional.of(testPreferences));
            when(mapper.toResponse(testPreferences)).thenReturn(testResponse);

            // When
            NotificationPreferencesResponse result = service.initializePreferences(testUserId, testUserEmail);

            // Then
            assertThat(result).isNotNull();
            verify(repository).existsByUserId(testUserId);
            verify(repository, never()).save(any());
            verify(repository).findByUserId(testUserId);
        }
    }

    @Nested
    @DisplayName("updatePreferences")
    class UpdatePreferencesTests {

        @Test
        @DisplayName("Should update all preferences when all fields provided")
        void updatePreferences_WithAllFields_UpdatesAll() {
            // Given
            NotificationPreferencesUpdateRequest request = new NotificationPreferencesUpdateRequest(
                    false, false, false, false, false
            );
            when(repository.findByUserId(testUserId)).thenReturn(Optional.of(testPreferences));
            when(repository.save(testPreferences)).thenReturn(testPreferences);
            when(mapper.toResponse(testPreferences)).thenReturn(new NotificationPreferencesResponse(
                    testUserId, testUserEmail, false, false, false, false, false
            ));

            // When
            NotificationPreferencesResponse result = service.updatePreferences(testUserId, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(testPreferences.getPreferences().isReservationRequestCreated()).isFalse();
            assertThat(testPreferences.getPreferences().isReservationCancelled()).isFalse();
            verify(repository).save(testPreferences);
        }

        @Test
        @DisplayName("Should update only provided fields")
        void updatePreferences_WithPartialFields_UpdatesOnlyProvided() {
            // Given
            NotificationPreferencesUpdateRequest request = new NotificationPreferencesUpdateRequest(
                    false, null, null, null, null
            );
            when(repository.findByUserId(testUserId)).thenReturn(Optional.of(testPreferences));
            when(repository.save(testPreferences)).thenReturn(testPreferences);
            when(mapper.toResponse(testPreferences)).thenReturn(testResponse);

            // When
            service.updatePreferences(testUserId, request);

            // Then
            assertThat(testPreferences.getPreferences().isReservationRequestCreated()).isFalse();
            assertThat(testPreferences.getPreferences().isReservationCancelled()).isTrue(); // unchanged
            verify(repository).save(testPreferences);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void updatePreferences_WhenUserNotFound_ThrowsException() {
            // Given
            NotificationPreferencesUpdateRequest request = new NotificationPreferencesUpdateRequest(
                    false, false, false, false, false
            );
            when(repository.findByUserId(testUserId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> service.updatePreferences(testUserId, request))
                    .isInstanceOf(PreferencesNotFoundException.class);
            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("isNotificationEnabled")
    class IsNotificationEnabledTests {

        @Test
        @DisplayName("Should return true when notification type is enabled")
        void isNotificationEnabled_WhenEnabled_ReturnsTrue() {
            // Given
            when(repository.findByUserId(testUserId)).thenReturn(Optional.of(testPreferences));

            // When
            boolean result = service.isNotificationEnabled(testUserId, NotificationType.RESERVATION_REQUEST_CREATED);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when notification type is disabled")
        void isNotificationEnabled_WhenDisabled_ReturnsFalse() {
            // Given
            testPreferences.getPreferences().setReservationRequestCreated(false);
            when(repository.findByUserId(testUserId)).thenReturn(Optional.of(testPreferences));

            // When
            boolean result = service.isNotificationEnabled(testUserId, NotificationType.RESERVATION_REQUEST_CREATED);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when user not found")
        void isNotificationEnabled_WhenUserNotFound_ReturnsFalse() {
            // Given
            when(repository.findByUserId(testUserId)).thenReturn(Optional.empty());

            // When
            boolean result = service.isNotificationEnabled(testUserId, NotificationType.RESERVATION_REQUEST_CREATED);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getOrCreatePreferences")
    class GetOrCreatePreferencesTests {

        @Test
        @DisplayName("Should return existing preferences when found")
        void getOrCreatePreferences_WhenExists_ReturnsExisting() {
            // Given
            when(repository.findByUserId(testUserId)).thenReturn(Optional.of(testPreferences));

            // When
            NotificationPreferences result = service.getOrCreatePreferences(testUserId, testUserEmail);

            // Then
            assertThat(result).isEqualTo(testPreferences);
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Should create new preferences when not found")
        void getOrCreatePreferences_WhenNotExists_CreatesNew() {
            // Given
            when(repository.findByUserId(testUserId)).thenReturn(Optional.empty());
            when(repository.save(any(NotificationPreferences.class))).thenAnswer(i -> i.getArgument(0));

            // When
            NotificationPreferences result = service.getOrCreatePreferences(testUserId, testUserEmail);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(testUserId);
            assertThat(result.getUserEmail()).isEqualTo(testUserEmail);
            verify(repository).save(any(NotificationPreferences.class));
        }
    }
}
