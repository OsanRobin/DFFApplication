package fenego.app;



import fenego.app.dto.IntershopLoginResult;
import fenego.app.intershop.IntershopClient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class IntershopClientTest {

    private RestTemplate restTemplate;
    private IntershopClient intershopClient;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        intershopClient = new IntershopClient(restTemplate);

        ReflectionTestUtils.setField(intershopClient, "validationUrl", "https://intershop.test/validate");
        ReflectionTestUtils.setField(intershopClient, "acceptHeader", "application/json");
    }

  @Test
void loginAdmin_returnsLoginResult_whenTokenIsReturned() {
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.set("authentication-token", "token-123");

    ResponseEntity<String> response = new ResponseEntity<>(
            "",
            responseHeaders,
            HttpStatus.OK
    );

    when(restTemplate.exchange(
            eq("https://intershop.test/validate"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(String.class)
    )).thenReturn(response);

    IntershopLoginResult result = intershopClient.loginAdmin(
            "admin",
            "secret",
            "Fenego"
    );

    IntershopLoginResult expected = new IntershopLoginResult(
            "admin",
            "Fenego",
            "token-123",
            List.of("admin")
    );

    assertThat(result)
            .usingRecursiveComparison()
            .isEqualTo(expected);
}
    @Test
    void loginAdmin_sendsCorrectHeaders() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("authentication-token", "token-123");

        ResponseEntity<String> response = new ResponseEntity<>(
                "",
                responseHeaders,
                HttpStatus.OK
        );

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(response);

        intershopClient.loginAdmin("admin", "secret", "Fenego");

        verify(restTemplate).exchange(
                eq("https://intershop.test/validate"),
                eq(HttpMethod.GET),
                argThat(entity -> {
                    HttpHeaders headers = entity.getHeaders();

                    String expectedAuth = "Basic " +
                            java.util.Base64.getEncoder()
                                    .encodeToString("admin:secret".getBytes(StandardCharsets.UTF_8));

                    return expectedAuth.equals(headers.getFirst("Authorization"))
                            && "application/json".equals(headers.getFirst("Accept"))
                            && "Fenego".equals(headers.getFirst("UserOrganization"));
                }),
                eq(String.class)
        );
    }

    @Test
    void loginAdmin_throwsException_whenNoTokenReturned() {
        ResponseEntity<String> response = new ResponseEntity<>(
                "",
                new HttpHeaders(),
                HttpStatus.OK
        );

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(response);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                intershopClient.loginAdmin("admin", "secret", "Fenego")
        );

        assertTrue(exception.getMessage().contains("no authentication-token"));
    }

    @Test
    void loginAdmin_throwsReadableException_whenUnauthorized() {
        HttpClientErrorException unauthorized =
                HttpClientErrorException.create(
                        HttpStatus.UNAUTHORIZED,
                        "Unauthorized",
                        HttpHeaders.EMPTY,
                        new byte[0],
                        StandardCharsets.UTF_8
                );

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(unauthorized);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                intershopClient.loginAdmin("wrong", "wrong", "Fenego")
        );

        assertTrue(exception.getMessage().contains("Invalid username"));
    }
}