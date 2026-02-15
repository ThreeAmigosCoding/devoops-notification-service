package com.devoops.notification.integration;

import com.devoops.notification.dto.request.NotificationPreferencesUpdateRequest;
import com.devoops.notification.dto.response.NotificationPreferencesResponse;
import com.devoops.notification.entity.NotificationPreferences;
import com.devoops.notification.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Notification Preferences API Integration Tests")
class NotificationPreferencesIntegrationTest extends BaseIntegrationTest {

    private static final String PREFERENCES_PATH = "/api/notification/preferences";

    @Nested
    @DisplayName("GET /preferences")
    class GetPreferences {

        @Test
        @DisplayName("Should return preferences for HOST user")
        void shouldReturnPreferencesForHost() {
            preferencesRepository.save(TestDataFactory.createDefaultPreferences());

            HttpHeaders headers = createHeaders(
                    TestDataFactory.DEFAULT_USER_ID.toString(),
                    "HOST"
            );

            ResponseEntity<NotificationPreferencesResponse> response = get(
                    PREFERENCES_PATH,
                    headers,
                    NotificationPreferencesResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().userId()).isEqualTo(TestDataFactory.DEFAULT_USER_ID);
            assertThat(response.getBody().userEmail()).isEqualTo(TestDataFactory.DEFAULT_USER_EMAIL);
            assertThat(response.getBody().reservationRequestCreated()).isTrue();
            assertThat(response.getBody().reservationCancelled()).isTrue();
            assertThat(response.getBody().hostRated()).isTrue();
            assertThat(response.getBody().accommodationRated()).isTrue();
            assertThat(response.getBody().reservationResponse()).isTrue();
        }

        @Test
        @DisplayName("Should return preferences for GUEST user")
        void shouldReturnPreferencesForGuest() {
            preferencesRepository.save(TestDataFactory.createDefaultPreferences());

            HttpHeaders headers = createHeaders(
                    TestDataFactory.DEFAULT_USER_ID.toString(),
                    "GUEST"
            );

            ResponseEntity<NotificationPreferencesResponse> response = get(
                    PREFERENCES_PATH,
                    headers,
                    NotificationPreferencesResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().userId()).isEqualTo(TestDataFactory.DEFAULT_USER_ID);
        }

        @Test
        @DisplayName("Should return 401 when X-User-Role header is missing")
        void shouldReturn401WhenRoleHeaderMissing() {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Id", TestDataFactory.DEFAULT_USER_ID.toString());

            ResponseEntity<String> response = get(PREFERENCES_PATH, headers, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should return 403 for invalid role")
        void shouldReturn403ForInvalidRole() {
            HttpHeaders headers = createHeaders(
                    TestDataFactory.DEFAULT_USER_ID.toString(),
                    "ADMIN"
            );

            ResponseEntity<String> response = get(PREFERENCES_PATH, headers, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("Should return 404 when preferences not found")
        void shouldReturn404WhenNotFound() {
            UUID nonExistentUserId = UUID.randomUUID();

            HttpHeaders headers = createHeaders(nonExistentUserId.toString(), "HOST");

            ResponseEntity<String> response = get(PREFERENCES_PATH, headers, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).contains(nonExistentUserId.toString());
        }
    }

    @Nested
    @DisplayName("PUT /preferences")
    class UpdatePreferences {

        @Test
        @DisplayName("Should update all preferences")
        void shouldUpdateAllPreferences() {
            preferencesRepository.save(TestDataFactory.createDefaultPreferences());

            NotificationPreferencesUpdateRequest request = new NotificationPreferencesUpdateRequest(
                    false, false, false, false, false
            );

            HttpHeaders headers = createHeaders(
                    TestDataFactory.DEFAULT_USER_ID.toString(),
                    "HOST"
            );

            ResponseEntity<NotificationPreferencesResponse> response = put(
                    PREFERENCES_PATH,
                    request,
                    headers,
                    NotificationPreferencesResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().reservationRequestCreated()).isFalse();
            assertThat(response.getBody().reservationCancelled()).isFalse();
            assertThat(response.getBody().hostRated()).isFalse();
            assertThat(response.getBody().accommodationRated()).isFalse();
            assertThat(response.getBody().reservationResponse()).isFalse();

            NotificationPreferences updated = preferencesRepository
                    .findByUserId(TestDataFactory.DEFAULT_USER_ID)
                    .orElseThrow();

            assertThat(updated.getPreferences().isReservationRequestCreated()).isFalse();
            assertThat(updated.getPreferences().isReservationCancelled()).isFalse();
            assertThat(updated.getPreferences().isHostRated()).isFalse();
            assertThat(updated.getPreferences().isAccommodationRated()).isFalse();
            assertThat(updated.getPreferences().isReservationResponse()).isFalse();
        }

        @Test
        @DisplayName("Should partially update preferences")
        void shouldPartiallyUpdatePreferences() {
            preferencesRepository.save(TestDataFactory.createDefaultPreferences());

            NotificationPreferencesUpdateRequest request = new NotificationPreferencesUpdateRequest(
                    false, null, null, null, false
            );

            HttpHeaders headers = createHeaders(
                    TestDataFactory.DEFAULT_USER_ID.toString(),
                    "GUEST"
            );

            ResponseEntity<NotificationPreferencesResponse> response = put(
                    PREFERENCES_PATH,
                    request,
                    headers,
                    NotificationPreferencesResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().reservationRequestCreated()).isFalse();
            assertThat(response.getBody().reservationCancelled()).isTrue();
            assertThat(response.getBody().hostRated()).isTrue();
            assertThat(response.getBody().accommodationRated()).isTrue();
            assertThat(response.getBody().reservationResponse()).isFalse();
        }

        @Test
        @DisplayName("Should return 401 when unauthorized")
        void shouldReturn401WhenUnauthorized() {
            NotificationPreferencesUpdateRequest request = new NotificationPreferencesUpdateRequest(
                    false, false, false, false, false
            );

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Id", TestDataFactory.DEFAULT_USER_ID.toString());
            headers.set("Content-Type", "application/json");

            ResponseEntity<String> response = put(PREFERENCES_PATH, request, headers, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should return 404 when preferences not found")
        void shouldReturn404WhenNotFound() {
            UUID nonExistentUserId = UUID.randomUUID();

            NotificationPreferencesUpdateRequest request = new NotificationPreferencesUpdateRequest(
                    false, false, false, false, false
            );

            HttpHeaders headers = createHeaders(nonExistentUserId.toString(), "HOST");

            ResponseEntity<String> response = put(PREFERENCES_PATH, request, headers, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Should verify persistence after update")
        void shouldVerifyPersistenceAfterUpdate() {
            preferencesRepository.save(TestDataFactory.createDefaultPreferences());

            NotificationPreferencesUpdateRequest request = new NotificationPreferencesUpdateRequest(
                    false, true, false, true, false
            );

            HttpHeaders headers = createHeaders(
                    TestDataFactory.DEFAULT_USER_ID.toString(),
                    "HOST"
            );

            ResponseEntity<NotificationPreferencesResponse> response = put(
                    PREFERENCES_PATH,
                    request,
                    headers,
                    NotificationPreferencesResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            NotificationPreferences persisted = preferencesRepository
                    .findByUserId(TestDataFactory.DEFAULT_USER_ID)
                    .orElseThrow();

            assertThat(persisted.getPreferences().isReservationRequestCreated()).isFalse();
            assertThat(persisted.getPreferences().isReservationCancelled()).isTrue();
            assertThat(persisted.getPreferences().isHostRated()).isFalse();
            assertThat(persisted.getPreferences().isAccommodationRated()).isTrue();
            assertThat(persisted.getPreferences().isReservationResponse()).isFalse();
        }
    }
}
