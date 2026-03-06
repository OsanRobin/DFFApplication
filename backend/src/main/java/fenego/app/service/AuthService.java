package fenego.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthService
{
    private final String baseUrl;
    private final String username;
    private final String password;
    private final String organization;

    public AuthService(
            @Value("${intershop.baseUrl}") String baseUrl,
            @Value("${intershop.username}") String username,
            @Value("${intershop.password}") String password,
            @Value("${intershop.organization}") String organization)
    {
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
        this.organization = organization;
    }

    public boolean login()
    {
        System.out.println("Base URL: " + baseUrl);
        System.out.println("Username: " + username);
        System.out.println("Organization: " + organization);

        return true;
    }
}