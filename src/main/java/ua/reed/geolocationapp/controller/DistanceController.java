package ua.reed.geolocationapp.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.reed.geolocationapp.dto.GeoDistanceInfoDto;
import ua.reed.geolocationapp.dto.GeoIPDto;
import ua.reed.geolocationapp.service.DistanceService;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class DistanceController {

    private final DistanceService distanceService;

    @GetMapping
    public ResponseEntity<GeoIPDto> getLocationFromRequest(final HttpServletRequest request) {
        return ResponseEntity
                .ok()
                .body(distanceService.getLocation(request));
    }

    @GetMapping("/address")
    public ResponseEntity<GeoIPDto> getLocationByIpAddress(@RequestParam("ip") final String ipAddress) {
        return ResponseEntity
                .ok()
                .body(distanceService.getLocation(ipAddress));
    }

    @GetMapping("/how-far")
    public ResponseEntity<GeoDistanceInfoDto> getDistance(final HttpServletRequest request,
                                                          @RequestParam("machineIp") final String machineIp) {
        return ResponseEntity
                .ok()
                .body(distanceService.getDistanceToPhysicalMachineInKilometers(request, machineIp));
    }
}
