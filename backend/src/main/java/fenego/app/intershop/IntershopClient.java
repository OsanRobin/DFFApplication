package fenego.app.intershop;
import fenego.app.dto.CustomerDetailResponse;
import fenego.app.dto.IntershopLoginResult;
import fenego.app.jpa.CustomerAddress;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
public class IntershopClient
{
    private final RestTemplate restTemplate;

    @Value("${intershop.validation-url}")
    private String validationUrl;

    @Value("${intershop.accept-header}")
    private String acceptHeader;

    @Value("${intershop.customers-url}")
    private String customersUrl;

    @Value("${intershop.customer-accept-header}")
    private String customerAcceptHeader;

    @Value("${intershop.customer-segments-url}")
    private String customerSegmentsUrl;

    @Value("${intershop.customer-segments-accept}")
    private String customerSegmentsAccept;

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

    public CustomerDetailResponse getCustomerById(String authenticationToken, String customerId)
    {
        try
        {
            String url = customersUrl + "/" + customerId;

            HttpHeaders headers = new HttpHeaders();
            headers.set("authentication-token", authenticationToken);
            headers.set("Accept", customerAcceptHeader);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            Map body = response.getBody();
            if (body == null)
            {
                throw new RuntimeException("Empty response from Intershop customer detail endpoint");
            }

            CustomerDetailResponse dto = new CustomerDetailResponse();
            dto.setCustomerNo((String) body.get("customerNo"));
            dto.setCompanyName((String) body.get("companyName"));
            dto.setCustomerType((String) body.get("customerType"));
            dto.setBudgetPriceType((String) body.get("budgetPriceType"));
            dto.setType((String) body.get("type"));

            Map preferredInvoice = (Map) body.get("preferredInvoiceToAddress");
            if (preferredInvoice != null)
            {
                dto.setPreferredInvoiceToAddress(mapAddress(preferredInvoice));
            }

            Map preferredShip = (Map) body.get("preferredShipToAddress");
            if (preferredShip != null)
            {
                dto.setPreferredShipToAddress(mapAddress(preferredShip));
            }

            return dto;
        }
        catch (HttpStatusCodeException ex)
        {
            throw new RuntimeException("Intershop customer detail failed: " + ex.getStatusCode().value() + " - " + ex.getResponseBodyAsString(), ex);
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Intershop customer detail failed: " + ex.getMessage(), ex);
        }
    }

    private CustomerAddress mapAddress(Map address)
    {
        CustomerAddress dto = new CustomerAddress();
        dto.setId((String) address.get("id"));
        dto.setAddressName((String) address.get("addressName"));
        dto.setFirstName((String) address.get("firstName"));
        dto.setLastName((String) address.get("lastName"));
        dto.setCompanyName1((String) address.get("companyName1"));
        dto.setAddressLine1((String) address.get("addressLine1"));
        dto.setPostalCode((String) address.get("postalCode"));
        dto.setCountry((String) address.get("country"));
        dto.setCountryCode((String) address.get("countryCode"));
        dto.setCity((String) address.get("city"));
        dto.setStreet((String) address.get("street"));
        dto.setCompany((String) address.get("company"));

        dto.setShipFromAddress(Boolean.TRUE.equals(address.get("shipFromAddress")));
        dto.setServiceToAddress(Boolean.TRUE.equals(address.get("serviceToAddress")));
        dto.setInstallToAddress(Boolean.TRUE.equals(address.get("installToAddress")));
        dto.setInvoiceToAddress(Boolean.TRUE.equals(address.get("invoiceToAddress")));
        dto.setShipToAddress(Boolean.TRUE.equals(address.get("shipToAddress")));

        return dto;
    }

    public void addCustomerAttribute(String authenticationToken, String customerId, String attributeName, String attributeValue)
    {
        try
        {
            String url = customersUrl + "/" + customerId + "/attributes";

            HttpHeaders headers = new HttpHeaders();
            headers.set("authentication-token", authenticationToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", customerAcceptHeader);

            Map<String, Object> body = Map.of(
                    "name", attributeName,
                    "value", attributeValue
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
        }
        catch (HttpStatusCodeException ex)
        {
            throw new RuntimeException("Add attribute failed: " + ex.getStatusCode().value() + " - " + ex.getResponseBodyAsString(), ex);
        }
    }

    public void updateCustomerAttribute(String authenticationToken, String customerId, String attributeName, String attributeValue)
    {
        try
        {
            String url = customersUrl + "/" + customerId + "/attributes/" + attributeName;

            HttpHeaders headers = new HttpHeaders();
            headers.set("authentication-token", authenticationToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", customerAcceptHeader);

            Map<String, Object> body = Map.of("value", attributeValue);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
        }
        catch (HttpStatusCodeException ex)
        {
            throw new RuntimeException("Update attribute failed: " + ex.getStatusCode().value() + " - " + ex.getResponseBodyAsString(), ex);
        }
    }

    public void assignCustomerToSegment(String authenticationToken, String customerId, String segmentId)
    {
        try
        {
            String url = customersUrl + "/" + customerId + "/customersegments";

            HttpHeaders headers = new HttpHeaders();
            headers.set("authentication-token", authenticationToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", customerAcceptHeader);

            Map<String, Object> body = Map.of("id", segmentId);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
        }
        catch (HttpStatusCodeException ex)
        {
            throw new RuntimeException("Assign segment failed: " + ex.getStatusCode().value() + " - " + ex.getResponseBodyAsString(), ex);
        }
    }

    
}