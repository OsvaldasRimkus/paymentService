package lt.rimkus.paymentService.services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Service
public class GeolocationService {

    private static final Logger logger = LoggerFactory.getLogger(GeolocationService.class);

    private final RestTemplate restTemplate;
    private final String geoServiceUrl;

    public GeolocationService(RestTemplateBuilder restTemplateBuilder, @Value("${app.geo.service.url:https://get.geojs.io/v1/ip/geo/}") String geoServiceUrl) {
        this.restTemplate = restTemplateBuilder
                .connectTimeout(Duration.ofSeconds(3))
                .readTimeout(Duration.ofSeconds(5))
                .build();
        this.geoServiceUrl = geoServiceUrl;
    }

    @Async("geoLocationExecutor")
    public CompletableFuture<String> resolveCountryByIpAsync(String ipAddress) {
        String country = resolveCountryByIp(ipAddress);
        return CompletableFuture.completedFuture(country);
    }

    public String resolveCountryByIp(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            logger.warn("IP address is null or empty");
            return "Unknown";
        }

        // Handle localhost and private IPs
        if (isLocalOrPrivateIp(ipAddress)) {
            logger.debug("Local or private IP detected: {}", ipAddress);
            return "Local";
        }

        try {
            String url = geoServiceUrl + ipAddress + ".json";
            logger.debug("Calling geolocation service: {}", url);

            GeoResponse response = restTemplate.getForObject(url, GeoResponse.class);

            if (response != null && response.getCountry() != null) {
                String country = response.getCountry();
                logger.info("Resolved country for IP {}: {}", ipAddress, country);
                return country;
            } else {
                logger.warn("No country data returned for IP: {}", ipAddress);
                return "Unknown";
            }

        } catch (RestClientException e) {
            logger.error("Error resolving country for IP {}: {}", ipAddress, e.getMessage());
            return "Unknown";
        }
    }

    @Async("geoLocationExecutor")
    public void logCountryAsync(String ipAddress, String additionalContext) {
        String country = resolveCountryByIp(ipAddress);
        logger.info("[ASYNC] Country resolved for IP {}: {} | Context: client action {}",
                ipAddress, country, additionalContext);
    }

    private boolean isLocalOrPrivateIp(String ip) {
        return ip.equals("127.0.0.1") ||
                ip.equals("::1") ||
                ip.equals("0:0:0:0:0:0:0:1") ||
                ip.startsWith("192.168.") ||
                ip.startsWith("10.") ||
                ip.startsWith("172.16.") ||
                ip.startsWith("172.17.") ||
                ip.startsWith("172.18.") ||
                ip.startsWith("172.19.") ||
                ip.startsWith("172.2") ||
                ip.startsWith("172.30.") ||
                ip.startsWith("172.31.");
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeoResponse {
        @JsonProperty("country")
        private String country;

        @JsonProperty("country_code")
        private String countryCode;

        @JsonProperty("city")
        private String city;

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getCountryCode() {
            return countryCode;
        }

        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }
    }
}
