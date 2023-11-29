package ua.reed.geolocationapp.dto;

import lombok.Builder;

@Builder
public record GeoIPDto(String ipAddress, String country, String city, Double latitude, Double longitude) {
}
