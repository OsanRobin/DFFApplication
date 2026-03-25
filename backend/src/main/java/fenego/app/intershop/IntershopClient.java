package fenego.app.intershop;

import fenego.app.dto.CustomerAttributeListResponse;
import fenego.app.dto.CustomerDetailResponse;
import fenego.app.dto.CustomerListResponse;
import fenego.app.dto.CustomerUserListResponse;
import fenego.app.dto.IntershopLoginResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

@Service
public class IntershopClient
{
    private final RestTemplate restTemplate;

    @Value("${intershop.validation-url}")
    private String validationUrl;

    @Value("${intershop.accept-header}")
    private String acceptHeader;

    @Value("${intershop.customer-accept-header}")
    private String customerAcceptHeader;

    @Value("${intershop.customers-url}")
    private String customersUrl;

    public IntershopClient(RestTemplate restTemplate)
    {
        this.restTemplate = restTemplate;
    }

    public IntershopLoginResult loginAdmin(String username, String password, String organization)
    {
        try
        {
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

            String authenticationToken = response.getHeaders().getFirst("authentication-token");

            if (!StringUtils.hasText(authenticationToken))
            {
                throw new RuntimeException("Intershop validation succeeded but no authentication-token was returned");
            }

            return new IntershopLoginResult(username, organization, authenticationToken);
        }
        catch (HttpStatusCodeException ex)
        {
            throw new ResponseStatusException(
                    ex.getStatusCode(),
                    "Intershop validation failed: " + ex.getResponseBodyAsString(),
                    ex
            );
        }
        catch (Exception ex)
        {
            throw new RuntimeException(
                    "Intershop validation failed: " + ex.getClass().getName() + " - " + ex.getMessage(),
                    ex
            );
        }
    }

    public CustomerListResponse getCustomers(String authenticationToken, int offset, int limit, String customerNo, String email)
    {
        try
        {
            String url = UriComponentsBuilder
                    .fromUriString(customersUrl)
                    .queryParam("offset", offset)
                    .queryParam("limit", limit)
                    .queryParamIfPresent("customerNo",
                            StringUtils.hasText(customerNo) ? Optional.of(customerNo) : Optional.empty())
                    .queryParamIfPresent("email",
                            StringUtils.hasText(email) ? Optional.of(email) : Optional.empty())
                    .toUriString();

            HttpEntity<Void> entity = new HttpEntity<>(new HttpHeaders());

            ResponseEntity<CustomerListResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    CustomerListResponse.class
            );

            if (response.getBody() == null)
            {
                throw new RuntimeException("Intershop returned an empty customer response");
            }

            return response.getBody();
        }
        catch (HttpStatusCodeException ex)
        {
            System.out.println("CUSTOMERS STATUS = " + ex.getStatusCode().value());
            System.out.println("CUSTOMERS BODY = " + ex.getResponseBodyAsString());

            throw new ResponseStatusException(
                    ex.getStatusCode(),
                    "Fetching customers failed: " + ex.getResponseBodyAsString(),
                    ex
            );
        }
    }

    public CustomerDetailResponse getCustomerById(String authenticationToken, String organization, String customerId)
    {
        try
        {
            String url = UriComponentsBuilder
                    .fromUriString(customersUrl + "/{customerId}")
                    .buildAndExpand(customerId)
                    .toUriString();

            HttpEntity<Void> entity = new HttpEntity<>(createCustomerHeaders(authenticationToken, organization));

            ResponseEntity<CustomerDetailResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    CustomerDetailResponse.class
            );

            if (response.getBody() == null)
            {
                throw new RuntimeException("Intershop returned an empty customer detail response");
            }

            return response.getBody();
        }
        catch (HttpStatusCodeException ex)
        {
            System.out.println("DETAIL URL = " + customersUrl + "/" + customerId);
            System.out.println("DETAIL CUSTOMER ID = " + customerId);
            System.out.println("DETAIL STATUS = " + ex.getStatusCode().value());
            System.out.println("DETAIL BODY = " + ex.getResponseBodyAsString());

            throw new ResponseStatusException(
                    ex.getStatusCode(),
                    "Fetching customer detail failed: " + ex.getResponseBodyAsString(),
                    ex
            );
        }
    }

    public CustomerUserListResponse getCustomerUsers(String authenticationToken, String organization, String customerId)
    {
        try
        {
            String url = UriComponentsBuilder
                    .fromUriString(customersUrl + "/{customerId}/users")
                    .buildAndExpand(customerId)
                    .toUriString();

            HttpEntity<Void> entity = new HttpEntity<>(createCustomerHeaders(authenticationToken, organization));

            ResponseEntity<CustomerUserListResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    CustomerUserListResponse.class
            );

            if (response.getBody() == null)
            {
                throw new RuntimeException("Intershop returned an empty customer users response");
            }

            return response.getBody();
        }
        catch (HttpStatusCodeException ex)
        {
            System.out.println("USERS URL = " + customersUrl + "/" + customerId + "/users");
            System.out.println("USERS CUSTOMER ID = " + customerId);
            System.out.println("USERS STATUS = " + ex.getStatusCode().value());
            System.out.println("USERS BODY = " + ex.getResponseBodyAsString());

            throw new ResponseStatusException(
                    ex.getStatusCode(),
                    "Fetching customer users failed: " + ex.getResponseBodyAsString(),
                    ex
            );
        }
    }

    public CustomerAttributeListResponse getCustomerAttributes(String authenticationToken, String organization, String customerId)
    {
        try
        {
            String url = UriComponentsBuilder
                    .fromUriString(customersUrl + "/{customerId}/attributes")
                    .buildAndExpand(customerId)
                    .toUriString();

            HttpEntity<Void> entity = new HttpEntity<>(createCustomerHeaders(authenticationToken, organization));

            ResponseEntity<CustomerAttributeListResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    CustomerAttributeListResponse.class
            );

            if (response.getBody() == null)
            {
                throw new RuntimeException("Intershop returned an empty customer attributes response");
            }

            return response.getBody();
        }
        catch (HttpStatusCodeException ex)
        {
            System.out.println("ATTRIBUTES URL = " + customersUrl + "/" + customerId + "/attributes");
            System.out.println("ATTRIBUTES CUSTOMER ID = " + customerId);
            System.out.println("ATTRIBUTES STATUS = " + ex.getStatusCode().value());
            System.out.println("ATTRIBUTES BODY = " + ex.getResponseBodyAsString());

            throw new ResponseStatusException(
                    ex.getStatusCode(),
                    "Fetching customer attributes failed: " + ex.getResponseBodyAsString(),
                    ex
            );
        }
    }

    private HttpHeaders createCustomerHeaders(String authenticationToken, String organization)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.set("authentication-token", authenticationToken);
        headers.set("UserOrganization", organization);
        headers.set("Accept", customerAcceptHeader);
        return headers;
    }
}