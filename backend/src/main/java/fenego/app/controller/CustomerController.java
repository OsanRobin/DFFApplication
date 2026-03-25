package fenego.app.controller;

import fenego.app.dto.CustomerAttributeListResponse;
import fenego.app.dto.CustomerDetailResponse;
import fenego.app.dto.CustomerListResponse;
import fenego.app.dto.CustomerUserListResponse;
import fenego.app.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class CustomerController
{
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService)
    {
        this.customerService = customerService;
    }

    @GetMapping
    public CustomerListResponse getCustomers(
            @RequestParam String domain,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String customerNo
    )
    {
        return customerService.getCustomers(domain, offset, limit, customerNo);
    }

    @GetMapping("/{customerId}")
    public CustomerDetailResponse getCustomerById(
            @PathVariable String customerId,
            HttpSession session
    )
    {
        String authenticationToken = getRequiredSessionValue(session, "intershopToken");
        String organization = getRequiredSessionValue(session, "organization");

        return customerService.getCustomerById(authenticationToken, organization, customerId);
    }

    @GetMapping("/{customerId}/users")
    public CustomerUserListResponse getCustomerUsers(
            @PathVariable String customerId,
            HttpSession session
    )
    {
        String authenticationToken = getRequiredSessionValue(session, "intershopToken");
        String organization = getRequiredSessionValue(session, "organization");

        return customerService.getCustomerUsers(authenticationToken, organization, customerId);
    }

    @GetMapping("/{customerId}/attributes")
    public CustomerAttributeListResponse getCustomerAttributes(
            @PathVariable String customerId,
            HttpSession session
    )
    {
        String authenticationToken = getRequiredSessionValue(session, "intershopToken");
        String organization = getRequiredSessionValue(session, "organization");

        return customerService.getCustomerAttributes(authenticationToken, organization, customerId);
    }

    private String getRequiredSessionValue(HttpSession session, String key)
    {
        Object value = session.getAttribute(key);

        if (value == null || String.valueOf(value).isBlank())
        {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        return String.valueOf(value);
    }
}