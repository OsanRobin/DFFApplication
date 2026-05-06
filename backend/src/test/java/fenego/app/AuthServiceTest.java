package fenego.app;

import fenego.app.dto.IntershopLoginResult;
import fenego.app.dto.LoginRequest;
import fenego.app.intershop.IntershopClient;
import fenego.app.service.AuthService;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private final IntershopClient intershopClient = mock(IntershopClient.class);
    private final AuthService authService = new AuthService(intershopClient);

    @Test
    void login_callsIntershopClientAndReturnsResult() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("secret");
        request.setOrganization("Fenego");

        IntershopLoginResult expected = new IntershopLoginResult(
                "admin",
                "Fenego",
                "token-123",
                List.of("admin")
        );

        when(intershopClient.loginAdmin("admin", "secret", "Fenego"))
                .thenReturn(expected);

        IntershopLoginResult result = authService.login(request);

        assertEquals(expected, result);

        verify(intershopClient).loginAdmin("admin", "secret", "Fenego");
    }
}