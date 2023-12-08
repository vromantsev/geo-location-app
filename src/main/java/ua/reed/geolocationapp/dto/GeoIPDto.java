package ua.reed.geolocationapp.dto;

import lombok.Builder;

@Builder
public record GeoIPDto(String ipAddress, Double latitude, Double longitude) {
}
