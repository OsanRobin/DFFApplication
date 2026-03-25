package fenego.app.service;

import fenego.app.dto.CustomerAttributeListResponse;
import fenego.app.dto.CustomerDetailResponse;
import fenego.app.dto.CustomerListResponse;
import fenego.app.dto.CustomerUserListResponse;
import fenego.app.intershop.IntershopClient;
import fenego.app.repository.CustomerRepository;
import org.springframework.stereotype.Service;

@Service
public class CustomerService
{
    private final CustomerRepository customerRepository;
    private final IntershopClient intershopClient;

    public CustomerService(CustomerRepository customerRepository, IntershopClient intershopClient)
    {
        this.customerRepository = customerRepository;
        this.intershopClient = intershopClient;
    }

    public CustomerListResponse getCustomers(String domainName, int offset, int limit, String customerNo)
    {
        CustomerListResponse response = new CustomerListResponse();
        response.setOffset(offset);
        response.setLimit(limit);
        response.setCount(customerRepository.countCustomersByDomain(domainName, customerNo));
        response.setData(customerRepository.findCustomersByDomain(domainName, offset, limit, customerNo));
        return response;
    }

    public CustomerDetailResponse getCustomerById(String authenticationToken, String organization, String customerId)
    {
        return intershopClient.getCustomerById(authenticationToken, organization, customerId);
    }

    public CustomerUserListResponse getCustomerUsers(String authenticationToken, String organization, String customerId)
    {
        return intershopClient.getCustomerUsers(authenticationToken, organization, customerId);
    }

    public CustomerAttributeListResponse getCustomerAttributes(String authenticationToken, String organization, String customerId)
    {
        return intershopClient.getCustomerAttributes(authenticationToken, organization, customerId);
    }
}