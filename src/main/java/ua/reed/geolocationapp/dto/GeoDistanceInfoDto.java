package ua.reed.geolocationapp.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record GeoDistanceInfoDto(String requesterIp,
                                 String machineIp,
                                 BigDecimal distanceToPhysicalMachineKm) {
}
