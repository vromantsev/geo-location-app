package ua.reed.geolocationapp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "geo-ip")
public class AppProperties {
    private String host;
    private int accountId;
    private String licenseKey;
}
