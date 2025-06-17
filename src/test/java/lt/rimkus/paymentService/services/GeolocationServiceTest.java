package lt.rimkus.paymentService.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GeolocationService Tests")
class GeolocationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    private GeolocationService geolocationService;
    private final String testGeoServiceUrl = "https://test.geo.service/";

    @BeforeEach
    void setUp() {
        // Given - Mock RestTemplateBuilder behavior
        when(restTemplateBuilder.connectTimeout(any(Duration.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.readTimeout(any(Duration.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        geolocationService = new GeolocationService(restTemplateBuilder, testGeoServiceUrl);
    }

    @Nested
    @DisplayName("resolveCountryByIp method tests")
    class ResolveCountryByIpTests {

        @Test
        @DisplayName("Should return 'Unknown' when IP address is null")
        void shouldReturnUnknownWhenIpIsNull() {
            // Given
            String nullIpAddress = null;

            // When
            String result = geolocationService.resolveCountryByIp(nullIpAddress);

            // Then
            assertThat(result).isEqualTo("Unknown");
            verifyNoInteractions(restTemplate);
        }

        @Test
        @DisplayName("Should return 'Unknown' when IP address is empty")
        void shouldReturnUnknownWhenIpIsEmpty() {
            // Given
            String emptyIpAddress = "";

            // When
            String result = geolocationService.resolveCountryByIp(emptyIpAddress);

            // Then
            assertThat(result).isEqualTo("Unknown");
            verifyNoInteractions(restTemplate);
        }

        @Test
        @DisplayName("Should return 'Unknown' when IP address is whitespace only")
        void shouldReturnUnknownWhenIpIsWhitespaceOnly() {
            // Given
            String whitespaceIpAddress = "   ";

            // When
            String result = geolocationService.resolveCountryByIp(whitespaceIpAddress);

            // Then
            assertThat(result).isEqualTo("Unknown");
            verifyNoInteractions(restTemplate);
        }

        @Test
        @DisplayName("Should return 'Local' when IP is localhost IPv4")
        void shouldReturnLocalWhenIpIsLocalhost() {
            // Given
            String localhostIp = "127.0.0.1";

            // When
            String result = geolocationService.resolveCountryByIp(localhostIp);

            // Then
            assertThat(result).isEqualTo("Local");
            verifyNoInteractions(restTemplate);
        }

        @Test
        @DisplayName("Should return 'Local' when IP is localhost IPv6")
        void shouldReturnLocalWhenIpIsLocalhostIpv6() {
            // Given
            String localhostIpv6 = "::1";

            // When
            String result = geolocationService.resolveCountryByIp(localhostIpv6);

            // Then
            assertThat(result).isEqualTo("Local");
            verifyNoInteractions(restTemplate);
        }

        @Test
        @DisplayName("Should return 'Local' when IP is private network (192.168.x.x)")
        void shouldReturnLocalWhenIpIsPrivateNetwork192() {
            // Given
            String privateIp = "192.168.1.100";

            // When
            String result = geolocationService.resolveCountryByIp(privateIp);

            // Then
            assertThat(result).isEqualTo("Local");
            verifyNoInteractions(restTemplate);
        }

        @Test
        @DisplayName("Should return 'Local' when IP is private network (10.x.x.x)")
        void shouldReturnLocalWhenIpIsPrivateNetwork10() {
            // Given
            String privateIp = "10.0.0.1";

            // When
            String result = geolocationService.resolveCountryByIp(privateIp);

            // Then
            assertThat(result).isEqualTo("Local");
            verifyNoInteractions(restTemplate);
        }

        @Test
        @DisplayName("Should return 'Local' when IP is private network (172.16.x.x)")
        void shouldReturnLocalWhenIpIsPrivateNetwork172() {
            // Given
            String privateIp = "172.16.1.1";

            // When
            String result = geolocationService.resolveCountryByIp(privateIp);

            // Then
            assertThat(result).isEqualTo("Local");
            verifyNoInteractions(restTemplate);
        }

        @Test
        @DisplayName("Should return country name when API call is successful")
        void shouldReturnCountryWhenApiCallIsSuccessful() {
            // Given
            String publicIp = "8.8.8.8";
            String expectedUrl = testGeoServiceUrl + publicIp + ".json";
            String expectedCountry = "United States";

            GeolocationService.GeoResponse mockResponse = new GeolocationService.GeoResponse();
            mockResponse.setCountry(expectedCountry);
            mockResponse.setCountryCode("US");
            mockResponse.setCity("Mountain View");

            when(restTemplate.getForObject(expectedUrl, GeolocationService.GeoResponse.class)).thenReturn(mockResponse);

            // When
            String result = geolocationService.resolveCountryByIp(publicIp);

            // Then
            assertThat(result).isEqualTo(expectedCountry);
            verify(restTemplate).getForObject(expectedUrl, GeolocationService.GeoResponse.class);
        }

        @Test
        @DisplayName("Should return 'Unknown' when API returns null response")
        void shouldReturnUnknownWhenApiReturnsNullResponse() {
            // Given
            String publicIp = "8.8.8.8";
            String expectedUrl = testGeoServiceUrl + publicIp + ".json";

            when(restTemplate.getForObject(expectedUrl, GeolocationService.GeoResponse.class))
                    .thenReturn(null);

            // When
            String result = geolocationService.resolveCountryByIp(publicIp);

            // Then
            assertThat(result).isEqualTo("Unknown");
            verify(restTemplate).getForObject(expectedUrl, GeolocationService.GeoResponse.class);
        }

        @Test
        @DisplayName("Should return 'Unknown' when API returns response with null country")
        void shouldReturnUnknownWhenApiReturnsResponseWithNullCountry() {
            // Given
            String publicIp = "8.8.8.8";
            String expectedUrl = testGeoServiceUrl + publicIp + ".json";

            GeolocationService.GeoResponse mockResponse = new GeolocationService.GeoResponse();
            mockResponse.setCountry(null);
            mockResponse.setCountryCode("US");

            when(restTemplate.getForObject(expectedUrl, GeolocationService.GeoResponse.class)).thenReturn(mockResponse);

            // When
            String result = geolocationService.resolveCountryByIp(publicIp);

            // Then
            assertThat(result).isEqualTo("Unknown");
            verify(restTemplate).getForObject(expectedUrl, GeolocationService.GeoResponse.class);
        }

        @Test
        @DisplayName("Should return 'Unknown' when RestClientException occurs")
        void shouldReturnUnknownWhenRestClientExceptionOccurs() {
            // Given
            String publicIp = "8.8.8.8";
            String expectedUrl = testGeoServiceUrl + publicIp + ".json";

            when(restTemplate.getForObject(expectedUrl, GeolocationService.GeoResponse.class)).thenThrow(new RestClientException("Connection timeout"));

            // When
            String result = geolocationService.resolveCountryByIp(publicIp);

            // Then
            assertThat(result).isEqualTo("Unknown");
            verify(restTemplate).getForObject(expectedUrl, GeolocationService.GeoResponse.class);
        }
    }

    @Nested
    @DisplayName("resolveCountryByIpAsync method tests")
    class ResolveCountryByIpAsyncTests {

        @Test
        @DisplayName("Should return CompletableFuture with country name when IP is valid")
        void shouldReturnCompletableFutureWithCountryWhenIpIsValid() throws ExecutionException, InterruptedException {
            // Given
            String publicIp = "8.8.8.8";
            String expectedUrl = testGeoServiceUrl + publicIp + ".json";
            String expectedCountry = "United States";

            GeolocationService.GeoResponse mockResponse = new GeolocationService.GeoResponse();
            mockResponse.setCountry(expectedCountry);

            when(restTemplate.getForObject(expectedUrl, GeolocationService.GeoResponse.class)).thenReturn(mockResponse);

            // When
            CompletableFuture<String> result = geolocationService.resolveCountryByIpAsync(publicIp);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isDone()).isTrue();
            assertThat(result.get()).isEqualTo(expectedCountry);
        }

        @Test
        @DisplayName("Should return CompletableFuture with 'Local' when IP is private")
        void shouldReturnCompletableFutureWithLocalWhenIpIsPrivate() throws ExecutionException, InterruptedException {
            // Given
            String privateIp = "192.168.1.1";

            // When
            CompletableFuture<String> result = geolocationService.resolveCountryByIpAsync(privateIp);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isDone()).isTrue();
            assertThat(result.get()).isEqualTo("Local");
            verifyNoInteractions(restTemplate);
        }

        @Test
        @DisplayName("Should return CompletableFuture with 'Unknown' when IP is null")
        void shouldReturnCompletableFutureWithUnknownWhenIpIsNull() throws ExecutionException, InterruptedException {
            // Given
            String nullIp = null;

            // When
            CompletableFuture<String> result = geolocationService.resolveCountryByIpAsync(nullIp);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isDone()).isTrue();
            assertThat(result.get()).isEqualTo("Unknown");
            verifyNoInteractions(restTemplate);
        }
    }

    @Nested
    @DisplayName("logCountryAsync method tests")
    class LogCountryAsyncTests {

        @Test
        @DisplayName("Should call resolveCountryByIp when logCountryAsync is invoked")
        void shouldCallResolveCountryByIpWhenLogCountryAsyncIsInvoked() {
            // Given
            String testIp = "8.8.8.8";
            String testContext = "test-payment";
            String expectedUrl = testGeoServiceUrl + testIp + ".json";

            GeolocationService.GeoResponse mockResponse = new GeolocationService.GeoResponse();
            mockResponse.setCountry("United States");

            when(restTemplate.getForObject(expectedUrl, GeolocationService.GeoResponse.class)).thenReturn(mockResponse);

            // When
            geolocationService.logCountryAsync(testIp, testContext);

            // Then
            // This test verifies that the method can be called without throwing exceptions
            assertThatCode(() -> geolocationService.logCountryAsync(testIp, testContext)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle null IP address in logCountryAsync")
        void shouldHandleNullIpAddressInLogCountryAsync() {
            // Given
            String nullIp = null;
            String testContext = "test-payment";

            // When & Then
            assertThatCode(() -> geolocationService.logCountryAsync(nullIp, testContext)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("isLocalOrPrivateIp method tests (via behavior verification)")
    class IsLocalOrPrivateIpTests {

        @Test
        @DisplayName("Should identify various localhost addresses as local")
        void shouldIdentifyLocalhostAddressesAsLocal() {
            // Given & When & Then
            assertThat(geolocationService.resolveCountryByIp("127.0.0.1")).isEqualTo("Local");
            assertThat(geolocationService.resolveCountryByIp("::1")).isEqualTo("Local");
            assertThat(geolocationService.resolveCountryByIp("0:0:0:0:0:0:0:1")).isEqualTo("Local");
        }

        @Test
        @DisplayName("Should identify private network ranges as local")
        void shouldIdentifyPrivateNetworkRangesAsLocal() {
            // Given & When & Then
            // 192.168.x.x range
            assertThat(geolocationService.resolveCountryByIp("192.168.0.1")).isEqualTo("Local");
            assertThat(geolocationService.resolveCountryByIp("192.168.255.255")).isEqualTo("Local");

            // 10.x.x.x range
            assertThat(geolocationService.resolveCountryByIp("10.0.0.1")).isEqualTo("Local");
            assertThat(geolocationService.resolveCountryByIp("10.255.255.255")).isEqualTo("Local");

            // 172.16.x.x to 172.31.x.x range
            assertThat(geolocationService.resolveCountryByIp("172.16.0.1")).isEqualTo("Local");
            assertThat(geolocationService.resolveCountryByIp("172.31.255.255")).isEqualTo("Local");
        }

        @Test
        @DisplayName("Should not identify public IP addresses as local")
        void shouldNotIdentifyPublicIpAddressesAsLocal() {
            // Given
            String publicIp = "8.8.8.8";
            String expectedUrl = testGeoServiceUrl + publicIp + ".json";

            GeolocationService.GeoResponse mockResponse = new GeolocationService.GeoResponse();
            mockResponse.setCountry("United States");

            when(restTemplate.getForObject(expectedUrl, GeolocationService.GeoResponse.class))
                    .thenReturn(mockResponse);

            // When
            String result = geolocationService.resolveCountryByIp(publicIp);

            // Then
            assertThat(result).isNotEqualTo("Local");
            assertThat(result).isEqualTo("United States");
        }
    }

    @Nested
    @DisplayName("GeoResponse DTO tests")
    class GeoResponseTests {

        @Test
        @DisplayName("Should create GeoResponse with all properties")
        void shouldCreateGeoResponseWithAllProperties() {
            // Given
            String expectedCountry = "Lithuania";
            String expectedCountryCode = "LT";
            String expectedCity = "Vilnius";

            // When
            GeolocationService.GeoResponse geoResponse = new GeolocationService.GeoResponse();
            geoResponse.setCountry(expectedCountry);
            geoResponse.setCountryCode(expectedCountryCode);
            geoResponse.setCity(expectedCity);

            // Then
            assertThat(geoResponse.getCountry()).isEqualTo(expectedCountry);
            assertThat(geoResponse.getCountryCode()).isEqualTo(expectedCountryCode);
            assertThat(geoResponse.getCity()).isEqualTo(expectedCity);
        }

        @Test
        @DisplayName("Should handle null values in GeoResponse")
        void shouldHandleNullValuesInGeoResponse() {
            // Given & When
            GeolocationService.GeoResponse geoResponse = new GeolocationService.GeoResponse();

            // Then
            assertThat(geoResponse.getCountry()).isNull();
            assertThat(geoResponse.getCountryCode()).isNull();
            assertThat(geoResponse.getCity()).isNull();
        }
    }

    @Nested
    @DisplayName("Integration-like tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should construct service correctly with RestTemplateBuilder")
        void shouldConstructServiceCorrectlyWithRestTemplateBuilder() {
            // Given
            RestTemplateBuilder realBuilder = new RestTemplateBuilder();
            String testUrl = "https://test-url.com/";

            // When
            GeolocationService service = new GeolocationService(realBuilder, testUrl);

            // Then
            assertThat(service).isNotNull();
            assertThat(ReflectionTestUtils.getField(service, "geoServiceUrl")).isEqualTo(testUrl);
            assertThat(ReflectionTestUtils.getField(service, "restTemplate")).isNotNull();
        }

        @Test
        @DisplayName("Should use default URL when no URL is provided")
        void shouldUseDefaultUrlWhenNoUrlIsProvided() {
            // Given
            RestTemplateBuilder realBuilder = new RestTemplateBuilder();
            String defaultUrl = "https://get.geojs.io/v1/ip/geo/";

            // When
            GeolocationService service = new GeolocationService(realBuilder, defaultUrl);

            // Then
            assertThat(ReflectionTestUtils.getField(service, "geoServiceUrl")).isEqualTo(defaultUrl);
        }
    }
}