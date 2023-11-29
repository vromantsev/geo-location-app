package ua.reed.geolocationapp.service;

import jakarta.servlet.http.HttpServletRequest;
import ua.reed.geolocationapp.dto.GeoDistanceInfoDto;
import ua.reed.geolocationapp.dto.GeoIPDto;

public interface DistanceService {

    GeoIPDto getLocation(final HttpServletRequest request);

    GeoIPDto getLocation(final String ipAddress);

    GeoDistanceInfoDto getDistanceToPhysicalMachineInKilometers(final HttpServletRequest request, final String machineIp);

}
