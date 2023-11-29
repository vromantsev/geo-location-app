package ua.reed.geolocationapp.service.impl;

import com.maxmind.geoip2.WebServiceClient;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.reed.geolocationapp.config.AppProperties;
import ua.reed.geolocationapp.dto.GeoDistanceInfoDto;
import ua.reed.geolocationapp.dto.GeoIPDto;
import ua.reed.geolocationapp.service.DistanceService;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequesterIPDistanceService implements DistanceService {

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
    public GeoDistanceInfoDto getDistanceToPhysicalMachineInKilometers(final HttpServletRequest request, final String machineIp) {
        return calculateDistanceImKm(getLocation(request), getLocation(machineIp));
    }

    private GeoIPDto getLocationByIdAddress(final String ipAddress) {
        Objects.requireNonNull(ipAddress, "Parameter [ipAddress] must not be null!");
        try (var client = new WebServiceClient.Builder(appProperties.getAccountId(), appProperties.getLicenseKey())
                .host(appProperties.getHost())
                .build()
        ) {
            var inetAddress = InetAddress.getByName(ipAddress);
            var cityResponse = client.city(inetAddress);
            log.debug(cityResponse.toJson());
            var city = cityResponse.getCity();
            var country = cityResponse.getCountry();
            var location = cityResponse.getLocation();
            var latitude = location.getLatitude();
            var longitude = location.getLongitude();
            return GeoIPDto.builder()
                    .ipAddress(ipAddress)
                    .country(country.getName())
                    .city(city.getName())
                    .latitude(latitude)
                    .longitude(longitude)
                    .build();
        } catch (IOException | GeoIp2Exception e) {
            throw new RuntimeException(e);
        }
    }

    private GeoDistanceInfoDto calculateDistanceImKm(final GeoIPDto requesterLocation, final GeoIPDto machineLocation) {
        double diffLat = Math.toRadians(machineLocation.latitude() - requesterLocation.latitude());
        double diffLong = Math.toRadians(machineLocation.longitude() - requesterLocation.longitude());
        double radiusStartLat = Math.toRadians(requesterLocation.latitude());
        double radiusEndLat = Math.toRadians(machineLocation.latitude());
        double a = Math.pow(Math.sin(diffLat / 2), 2) + Math.pow(Math.sin(diffLong / 2), 2) * Math.cos(radiusStartLat) * Math.cos(radiusEndLat);
        double b = 2 * Math.asin(Math.sqrt(a));
        double distanceInKm = EARTH_RADIUS_KM * b;
        return GeoDistanceInfoDto.builder()
                .requesterIp(requesterLocation.ipAddress())
                .requesterCountry(requesterLocation.country())
                .requesterCity(requesterLocation.city())
                .machineIp(machineLocation.ipAddress())
                .machineLocationCountry(machineLocation.country())
                .machineLocationCity(machineLocation.city())
                .distanceToPhysicalMachineKm(BigDecimal.valueOf(distanceInKm).setScale(2, RoundingMode.HALF_UP))
                .build();
    }
}
