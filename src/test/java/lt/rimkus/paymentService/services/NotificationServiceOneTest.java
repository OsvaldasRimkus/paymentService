package lt.rimkus.paymentService.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceOneTest {

    private static final String SUCCESS = "Success";
    private static final String FAILURE = "Failure";
    private static final String TEST_URL = "https://api.github.com/users/";
    private static final String TEST_USERNAME = "testUser";

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    private NotificationServiceOne notificationService;

    @BeforeEach
    void setUp() {
        // Given: RestTemplateBuilder is configured to return mocked RestTemplate
        when(restTemplateBuilder.connectTimeout(any(Duration.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.readTimeout(any(Duration.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        notificationService = new NotificationServiceOne(restTemplateBuilder, TEST_URL);
    }

    @Test
    void givenValidUsername_whenNotifyServiceAsyncCalled_thenShouldConfigureRestTemplateWithCorrectTimeouts() {
        // Given: Valid username and successful response
        Map<String, Object> responseBody = new HashMap<>();
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(Map.class))).thenReturn(responseEntity);

        // When: notifyServiceAsync is called
        notificationService.notifyServiceAsync(TEST_USERNAME);

        // Then: RestTemplate should be configured with correct timeouts
        verify(restTemplateBuilder).connectTimeout(Duration.ofSeconds(3));
        verify(restTemplateBuilder).readTimeout(Duration.ofSeconds(5));
        verify(restTemplateBuilder).build();
    }

    @Test
    void givenValidUsername_whenNotifyServiceAsyncCalled_thenShouldCallCorrectUrl() {
        // Given: Valid username and successful response
        Map<String, Object> responseBody = new HashMap<>();
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(Map.class))).thenReturn(responseEntity);

        // When: notifyServiceAsync is called
        notificationService.notifyServiceAsync(TEST_USERNAME);

        // Then: Should call the correct URL
        verify(restTemplate).getForEntity(TEST_URL + TEST_USERNAME, Map.class);
    }

    @Test
    void givenSuccessfulResponse_whenNotifyServiceAsyncCalled_thenShouldReturnSuccess() throws ExecutionException, InterruptedException {
        // Given: Successful HTTP response (200 OK)
        Map<String, Object> responseBody = new HashMap<>();
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(Map.class))).thenReturn(responseEntity);

        // When: notifyServiceAsync is called
        CompletableFuture<String> result = notificationService.notifyServiceAsync(TEST_USERNAME);

        // Then: Should return SUCCESS status
        assertEquals(SUCCESS, result.get());
    }

    @Test
    void givenSuccessfulResponseWithCreatedStatus_whenNotifyServiceAsyncCalled_thenShouldReturnSuccess() throws ExecutionException, InterruptedException {
        // Given: Successful HTTP response (201 Created)
        Map<String, Object> responseBody = new HashMap<>();
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.CREATED);
        when(restTemplate.getForEntity(anyString(), eq(Map.class))).thenReturn(responseEntity);

        // When: notifyServiceAsync is called
        CompletableFuture<String> result = notificationService.notifyServiceAsync(TEST_USERNAME);

        // Then: Should return SUCCESS status
        assertEquals(SUCCESS, result.get());
    }

    @Test
    void givenSuccessfulResponseWithAcceptedStatus_whenNotifyServiceAsyncCalled_thenShouldReturnSuccess() throws ExecutionException, InterruptedException {
        // Given: Successful HTTP response (202 Accepted)
        Map<String, Object> responseBody = new HashMap<>();
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.ACCEPTED);
        when(restTemplate.getForEntity(anyString(), eq(Map.class))).thenReturn(responseEntity);

        // When: notifyServiceAsync is called
        CompletableFuture<String> result = notificationService.notifyServiceAsync(TEST_USERNAME);

        // Then: Should return SUCCESS status
        assertEquals(SUCCESS, result.get());
    }

    @Test
    void givenClientErrorResponse_whenNotifyServiceAsyncCalled_thenShouldReturnNull() throws ExecutionException, InterruptedException {
        // Given: Client error response (404 Not Found)
        Map<String, Object> responseBody = new HashMap<>();
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.NOT_FOUND);
        when(restTemplate.getForEntity(anyString(), eq(Map.class))).thenReturn(responseEntity);

        // When: notifyServiceAsync is called
        CompletableFuture<String> result = notificationService.notifyServiceAsync(TEST_USERNAME);

        // Then: Should return null (status not set to SUCCESS)
        assertNull(result.get());
    }

    @Test
    void givenServerErrorResponse_whenNotifyServiceAsyncCalled_thenShouldReturnNull() throws ExecutionException, InterruptedException {
        // Given: Server error response (500 Internal Server Error)
        Map<String, Object> responseBody = new HashMap<>();
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.getForEntity(anyString(), eq(Map.class))).thenReturn(responseEntity);

        // When: notifyServiceAsync is called
        CompletableFuture<String> result = notificationService.notifyServiceAsync(TEST_USERNAME);

        // Then: Should return null (status not set to SUCCESS)
        assertNull(result.get());
    }

    @Test
    void givenRestClientException_whenNotifyServiceAsyncCalled_thenShouldReturnFailure() throws ExecutionException, InterruptedException {
        // Given: RestTemplate throws RestClientException
        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenThrow(new RestClientException("Connection timeout"));

        // When: notifyServiceAsync is called
        CompletableFuture<String> result = notificationService.notifyServiceAsync(TEST_USERNAME);

        // Then: Should return FAILURE status
        assertEquals(FAILURE, result.get());
    }

    @Test
    void givenResourceAccessException_whenNotifyServiceAsyncCalled_thenShouldReturnFailure() throws ExecutionException, InterruptedException {
        // Given: RestTemplate throws ResourceAccessException (subclass of RestClientException)
        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenThrow(new org.springframework.web.client.ResourceAccessException("Read timeout"));

        // When: notifyServiceAsync is called
        CompletableFuture<String> result = notificationService.notifyServiceAsync(TEST_USERNAME);

        // Then: Should return FAILURE status
        assertEquals(FAILURE, result.get());
    }

    @Test
    void givenHttpClientErrorException_whenNotifyServiceAsyncCalled_thenShouldReturnFailure() throws ExecutionException, InterruptedException {
        // Given: RestTemplate throws HttpClientErrorException (subclass of RestClientException)
        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenThrow(new org.springframework.web.client.HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // When: notifyServiceAsync is called
        CompletableFuture<String> result = notificationService.notifyServiceAsync(TEST_USERNAME);

        // Then: Should return FAILURE status
        assertEquals(FAILURE, result.get());
    }

    @Test
    void givenEmptyUsername_whenNotifyServiceAsyncCalled_thenShouldCallUrlWithEmptyUsername() {
        // Given: Empty username and successful response
        Map<String, Object> responseBody = new HashMap<>();
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(Map.class))).thenReturn(responseEntity);

        // When: notifyServiceAsync is called with empty username
        notificationService.notifyServiceAsync("");

        // Then: Should call URL with empty username appended
        verify(restTemplate).getForEntity(TEST_URL, Map.class);
    }

    @Test
    void givenNullUsername_whenNotifyServiceAsyncCalled_thenShouldCallUrlWithNullAppended() {
        // Given: Null username and successful response
        Map<String, Object> responseBody = new HashMap<>();
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(Map.class))).thenReturn(responseEntity);

        // When: notifyServiceAsync is called with null username
        notificationService.notifyServiceAsync(null);

        // Then: Should call URL with "null" appended
        verify(restTemplate).getForEntity(TEST_URL + "null", Map.class);
    }

    @Test
    void givenUsernameWithSpecialCharacters_whenNotifyServiceAsyncCalled_thenShouldCallUrlWithSpecialCharacters() {
        // Given: Username with special characters and successful response
        String specialUsername = "test@user.com";
        Map<String, Object> responseBody = new HashMap<>();
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(Map.class))).thenReturn(responseEntity);

        // When: notifyServiceAsync is called with special character username
        notificationService.notifyServiceAsync(specialUsername);

        // Then: Should call URL with special characters preserved
        verify(restTemplate).getForEntity(TEST_URL + specialUsername, Map.class);
    }

    @Test
    void givenDefaultConstructorValues_whenServiceCreated_thenShouldUseDefaultUrl() {
        // Given: Service created with default URL
        when(restTemplateBuilder.connectTimeout(any(Duration.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.readTimeout(any(Duration.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        // When: Service is created without explicit URL (using default)
        NotificationServiceOne serviceWithDefault = new NotificationServiceOne(restTemplateBuilder, "https://api.github.com/users/");

        Map<String, Object> responseBody = new HashMap<>();
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(Map.class))).thenReturn(responseEntity);

        serviceWithDefault.notifyServiceAsync(TEST_USERNAME);

        // Then: Should use the default GitHub API URL
        verify(restTemplate).getForEntity("https://api.github.com/users/" + TEST_USERNAME, Map.class);
    }
}