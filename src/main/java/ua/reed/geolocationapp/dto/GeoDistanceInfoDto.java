package ua.reed.geolocationapp.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record GeoDistanceInfoDto(String requesterIp,
                                 String requesterCountry,
                                 String requesterCity,
                                 String machineIp,
                                 String machineLocationCountry,
                                 String machineLocationCity,
                                 BigDecimal distanceToPhysicalMachineKm) {
}
