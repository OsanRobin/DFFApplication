package fenego.app.service;

import fenego.app.dto.IntershopLoginResult;
import fenego.app.dto.LoginRequest;
import fenego.app.intershop.IntershopClient;
import org.springframework.stereotype.Service;

@Service
public class AuthService
{
    private final IntershopClient intershopClient;

    public AuthService(IntershopClient intershopClient)
    {
        this.intershopClient = intershopClient;
    }

    public IntershopLoginResult login(LoginRequest request)
    {
        return intershopClient.loginAdmin(
                request.getUsername(),
                request.getPassword(),
                request.getOrganization()
        );
    }
}