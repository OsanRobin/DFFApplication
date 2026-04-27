package fenego.app.controller;

import fenego.app.dto.CustomerAttributeRequest;
import fenego.app.dto.CustomerDetailResponse;
import fenego.app.dto.CustomerListResponse;
import fenego.app.dto.CustomerUserListResponse;
import fenego.app.service.CustomerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
public class CustomerController
{
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService)
    {
        this.customerService = customerService;
    }

    @GetMapping("/{customerId}/users")
    public CustomerUserListResponse getCustomerUsers(@PathVariable String customerId)
    {
        return customerService.getCustomerUsers(customerId);
    }

    @GetMapping
    public CustomerListResponse getCustomers(
            @RequestHeader("authentication-token") String authenticationToken,
            @RequestParam("domain") String domain,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String customerNo,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String segment,
            @RequestParam(required = false) String email)
    {
        return customerService.getCustomers(
                authenticationToken,
                domain,
                offset,
                limit,
                customerNo,
                query,
                type,
                status,
                segment,
                email
        );
    }

    @GetMapping("/{customerId}")
    public CustomerDetailResponse getCustomerById(
            @RequestHeader("authentication-token") String authenticationToken,
            @RequestParam("domain") String domain,
            @PathVariable String customerId)
    {
        return customerService.getCustomerById(authenticationToken, domain, customerId);
    }

    @PostMapping("/{customerId}/attributes")
    public void addCustomerAttribute(
            @RequestHeader("authentication-token") String authenticationToken,
            @RequestParam("domain") String domain,
            @PathVariable String customerId,
            @RequestBody CustomerAttributeRequest request)
    {
        customerService.addCustomerAttribute(authenticationToken, domain, customerId, request);
    }

    @PutMapping("/{customerId}/attributes/{attributeName}")
    public void updateCustomerAttribute(
            @RequestHeader("authentication-token") String authenticationToken,
            @RequestParam("domain") String domain,
            @PathVariable String customerId,
            @PathVariable String attributeName,
            @RequestBody CustomerAttributeRequest request)
    {
        customerService.updateCustomerAttribute(authenticationToken, domain, customerId, attributeName, request);
    }
}