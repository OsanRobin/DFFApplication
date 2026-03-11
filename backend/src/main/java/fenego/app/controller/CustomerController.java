package fenego.app.controller;

import fenego.app.dto.CustomerDetailResponse;
import fenego.app.dto.CustomerListResponse;
import fenego.app.service.CustomerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping
    public CustomerListResponse getCustomers(
            @RequestHeader("authentication-token") String authenticationToken,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String customerNo,
            @RequestParam(required = false) String email
    )
    {
        return customerService.getCustomers(authenticationToken, offset, limit, customerNo, email);
    }
    @GetMapping("/{customerId}")
public CustomerDetailResponse getCustomerById(@PathVariable String customerId)
{
    return customerService.getCustomerById(customerId);
}
}