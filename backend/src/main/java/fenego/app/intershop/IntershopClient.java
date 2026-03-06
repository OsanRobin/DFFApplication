package fenego.app.intershop;

import fenego.app.dto.IntershopLoginResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Service
public class IntershopClient
{
    private final RestTemplate restTemplate;

    @Value("${intershop.validation-url}")
    private String validationUrl;

    @Value("${intershop.accept-header}")
    private String acceptHeader;

    public IntershopClient(RestTemplate restTemplate)
    {
        this.restTemplate = restTemplate;
    }

    public IntershopLoginResult loginAdmin(String username, String password, String organization)
    {
        try
        {
            System.out.println("validationUrl = " + validationUrl);
            System.out.println("acceptHeader = " + acceptHeader);
            System.out.println("username = " + username);
            System.out.println("organization = " + organization);

            String basicAuth = Base64.getEncoder()
                    .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + basicAuth);
            headers.set("Accept", acceptHeader);
            headers.set("UserOrganization", organization);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    validationUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            System.out.println("Intershop status = " + response.getStatusCode().value());
            System.out.println("Intershop auth token header = " + response.getHeaders().getFirst("authentication-token"));

            String authenticationToken = response.getHeaders().getFirst("authentication-token");

            if (authenticationToken == null || authenticationToken.isBlank())
            {
                throw new RuntimeException("Intershop validation succeeded but no authentication-token was returned");
            }

            return new IntershopLoginResult(username, organization, authenticationToken);
        }
        catch (HttpStatusCodeException ex)
        {
            int status = ex.getStatusCode().value();

            if (status == 401 || status == 403)
            {
                throw new RuntimeException("Invalid username, password or organization");
            }

            throw new RuntimeException("Intershop validation failed: " + status + " - " + ex.getResponseBodyAsString(), ex);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw new RuntimeException("Intershop validation failed: " + ex.getClass().getName() + " - " + ex.getMessage(), ex);
        }
    }
}