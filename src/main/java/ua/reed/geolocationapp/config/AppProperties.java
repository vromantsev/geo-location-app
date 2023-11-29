package ua.reed.geolocationapp.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class AppProperties {

    @Value("${geo-ip.host}")
    private String host;

    @Value("${geo-ip.account.id}")
    private int accountId;

    @Value("${geo-ip.account.license-key}")
    private String licenseKey;

}
