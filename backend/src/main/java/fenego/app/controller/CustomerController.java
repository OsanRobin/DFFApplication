package fenego.app.controller;

import fenego.app.dto.CustomerDetailResponse;
import fenego.app.dto.CustomerListResponse;
import fenego.app.dto.CustomerUserListResponse;
import fenego.app.service.CustomerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    public CustomerDetailResponse getCustomerById(@PathVariable String customerId)
    {
        return customerService.getCustomerById(customerId);
    }

    @GetMapping("/{customerId}/users")
    public CustomerUserListResponse getCustomerUsers(@PathVariable String customerId)
    {
        return customerService.getCustomerUsers(customerId);
    }
}