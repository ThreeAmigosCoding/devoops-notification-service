package com.devoops.notification.service;

import com.devoops.notification.dto.request.NotificationPreferencesUpdateRequest;
import com.devoops.notification.dto.response.NotificationPreferencesResponse;
import com.devoops.notification.entity.NotificationPreferences;
import com.devoops.notification.entity.NotificationType;
import com.devoops.notification.entity.Preferences;
import com.devoops.notification.exception.PreferencesNotFoundException;
import com.devoops.notification.mapper.NotificationPreferencesMapper;
import com.devoops.notification.repository.NotificationPreferencesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPreferencesService {

    private final NotificationPreferencesRepository repository;
    private final NotificationPreferencesMapper mapper;

    public NotificationPreferencesResponse getPreferences(UUID userId) {
        NotificationPreferences preferences = findByUserIdOrThrow(userId);
        return mapper.toResponse(preferences);
    }

    public NotificationPreferencesResponse initializePreferences(UUID userId, String userEmail) {
        if (repository.existsByUserId(userId)) {
            log.info("Preferences already exist for user {}, returning existing", userId);
            return getPreferences(userId);
        }

        NotificationPreferences preferences = new NotificationPreferences(userId, userEmail);
        NotificationPreferences saved = repository.save(preferences);
        log.info("Initialized notification preferences for user {}", userId);
        return mapper.toResponse(saved);
    }

    public NotificationPreferencesResponse updatePreferences(UUID userId, NotificationPreferencesUpdateRequest request) {
        NotificationPreferences preferences = findByUserIdOrThrow(userId);
        Preferences prefs = preferences.getPreferences();

        if (request.reservationRequestCreated() != null) {
            prefs.setReservationRequestCreated(request.reservationRequestCreated());
        }
        if (request.reservationCancelled() != null) {
            prefs.setReservationCancelled(request.reservationCancelled());
        }
        if (request.hostRated() != null) {
            prefs.setHostRated(request.hostRated());
        }
        if (request.accommodationRated() != null) {
            prefs.setAccommodationRated(request.accommodationRated());
        }
        if (request.reservationResponse() != null) {
            prefs.setReservationResponse(request.reservationResponse());
        }

        NotificationPreferences saved = repository.save(preferences);
        log.info("Updated notification preferences for user {}", userId);
        return mapper.toResponse(saved);
    }

    public boolean isNotificationEnabled(UUID userId, NotificationType type) {
        return repository.findByUserId(userId)
                .map(prefs -> prefs.getPreferences().isEnabled(type))
                .orElse(false);
    }

    public NotificationPreferences getOrCreatePreferences(UUID userId, String userEmail) {
        return repository.findByUserId(userId)
                .orElseGet(() -> {
                    NotificationPreferences newPrefs = new NotificationPreferences(userId, userEmail);
                    return repository.save(newPrefs);
                });
    }

    private NotificationPreferences findByUserIdOrThrow(UUID userId) {
        return repository.findByUserId(userId)
                .orElseThrow(() -> new PreferencesNotFoundException(userId));
    }
}
