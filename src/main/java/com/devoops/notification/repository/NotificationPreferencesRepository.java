package com.devoops.notification.repository;

import com.devoops.notification.entity.NotificationPreferences;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationPreferencesRepository extends MongoRepository<NotificationPreferences, String> {

    Optional<NotificationPreferences> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}
