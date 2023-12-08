package ua.reed.geolocationapp.service.impl;

import com.maxmind.geoip2.WebServiceClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.reed.geolocationapp.config.AppProperties;
import ua.reed.geolocationapp.dto.GeoDistanceInfoDto;
import ua.reed.geolocationapp.dto.GeoIPDto;
import ua.reed.geolocationapp.service.DistanceService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimpleDistanceService implements DistanceService {

    private static final String X_FORWARDED_FOR_HEADER = "X-Forwarded-For";
    private static final Double EARTH_RADIUS_KM = 6371.0;

    private final AppProperties appProperties;

    @Override
    public GeoIPDto getLocation(final HttpServletRequest request) {
        var requesterIPAddress = request.getHeader(X_FORWARDED_FOR_HEADER);
        if (requesterIPAddress == null) {
            log.debug("No ip address detected via {} header, resolving remote ip address...", X_FORWARDED_FOR_HEADER);
            requesterIPAddress = request.getRemoteAddr();
        }
        log.debug("Resolved ip address: {}", requesterIPAddress);
        return getLocation(requesterIPAddress);
    }

    @Override
    public GeoIPDto getLocation(final String ipAddress) {
        return getLocationByIdAddress(ipAddress);
    }

    @Override
    public GeoDistanceInfoDto getDistanceInKm(final HttpServletRequest request, final String machineIp) {
        return calculateDistanceInKm(getLocation(request), getLocation(machineIp));
    }

    @SneakyThrows
    private GeoIPDto getLocationByIdAddress(final String ipAddress) {
        Objects.requireNonNull(ipAddress, "Parameter [ipAddress] must not be null!");
        try (var client = new WebServiceClient.Builder(appProperties.getAccountId(), appProperties.getLicenseKey())
                .host(appProperties.getHost())
                .build()) {
            var inetAddress = InetAddress.getByName(ipAddress);
            var cityResponse = client.city(inetAddress);
            var location = cityResponse.getLocation();
            var latitude = location.getLatitude();
            var longitude = location.getLongitude();
            return GeoIPDto.builder()
                    .ipAddress(ipAddress)
                    .latitude(latitude)
                    .longitude(longitude)
                    .build();
        }
    }

    private GeoDistanceInfoDto calculateDistanceInKm(final GeoIPDto requesterLocation, final GeoIPDto machineLocation) {
        var haversineEquationResult = getHaversineEquationResult(requesterLocation, machineLocation);
        var distanceInKm = EARTH_RADIUS_KM * haversineEquationResult;
        return GeoDistanceInfoDto.builder()
                .requesterIp(requesterLocation.ipAddress())
                .machineIp(machineLocation.ipAddress())
                .distanceToPhysicalMachineKm(BigDecimal.valueOf(distanceInKm).setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    private double getHaversineEquationResult(final GeoIPDto requesterLocation, final GeoIPDto machineLocation) {
        var diffLat = Math.toRadians(machineLocation.latitude() - requesterLocation.latitude());
        var diffLong = Math.toRadians(machineLocation.longitude() - requesterLocation.longitude());
        var radiusStartLat = Math.toRadians(requesterLocation.latitude());
        var radiusEndLat = Math.toRadians(machineLocation.latitude());
        var haversineEquation = Math.pow(Math.sin(diffLat / 2), 2) + Math.pow(Math.sin(diffLong / 2), 2) * Math.cos(radiusStartLat) * Math.cos(radiusEndLat);
        return 2 * Math.asin(Math.sqrt(haversineEquation));
    }
}
