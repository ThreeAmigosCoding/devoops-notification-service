package com.devoops.notification.controller;

import com.devoops.notification.config.RequireRole;
import com.devoops.notification.config.UserContext;
import com.devoops.notification.dto.request.NotificationPreferencesUpdateRequest;
import com.devoops.notification.dto.response.NotificationPreferencesResponse;
import com.devoops.notification.service.NotificationPreferencesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/notification/preferences")
@RequiredArgsConstructor
public class NotificationPreferencesController {

    private final NotificationPreferencesService preferencesService;

    @GetMapping
    @RequireRole({"HOST", "GUEST"})
    public ResponseEntity<NotificationPreferencesResponse> getPreferences(UserContext userContext) {
        log.debug("Getting notification preferences for user {}", userContext.userId());
        return ResponseEntity.ok(preferencesService.getPreferences(userContext.userId()));
    }

    @PutMapping
    @RequireRole({"HOST", "GUEST"})
    public ResponseEntity<NotificationPreferencesResponse> updatePreferences(
            UserContext userContext,
            @RequestBody @Valid NotificationPreferencesUpdateRequest request) {
        log.debug("Updating notification preferences for user {}", userContext.userId());
        return ResponseEntity.ok(preferencesService.updatePreferences(userContext.userId(), request));
    }
}
