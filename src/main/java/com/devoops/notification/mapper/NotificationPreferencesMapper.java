package com.devoops.notification.mapper;

import com.devoops.notification.dto.response.NotificationPreferencesResponse;
import com.devoops.notification.entity.NotificationPreferences;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationPreferencesMapper {

    @Mapping(target = "reservationRequestCreated", source = "preferences.reservationRequestCreated")
    @Mapping(target = "reservationCancelled", source = "preferences.reservationCancelled")
    @Mapping(target = "hostRated", source = "preferences.hostRated")
    @Mapping(target = "accommodationRated", source = "preferences.accommodationRated")
    @Mapping(target = "reservationResponse", source = "preferences.reservationResponse")
    NotificationPreferencesResponse toResponse(NotificationPreferences entity);
}
