package fenego.app.service;

import fenego.app.dto.CustomerDetailResponse;
import fenego.app.dto.CustomerListResponse;
import fenego.app.dto.CustomerUserDTO;
import fenego.app.dto.CustomerUserListResponse;
import fenego.app.repository.CustomerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CustomerService
{
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository)
    {
        this.customerRepository = customerRepository;
    }

    public CustomerListResponse getCustomers(String domainName, int offset, int limit, String customerNo, String email)
    {
        CustomerListResponse response = new CustomerListResponse();
        response.setOffset(offset);
        response.setLimit(limit);
        response.setCount(customerRepository.countCustomersByDomain(domainName, customerNo));
        response.setData(customerRepository.findCustomersByDomain(domainName, offset, limit, customerNo));
        return response;
    }

    public CustomerDetailResponse getCustomerById(String customerId)
    {
        CustomerDetailResponse response = customerRepository.findCustomerDetailById(customerId);

        if (response == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
        }

        return response;
    }

    public CustomerUserListResponse getCustomerUsers(String customerId)
    {
        List<CustomerUserDTO> users = customerRepository.findUsersByCustomerId(customerId);

        CustomerUserListResponse response = new CustomerUserListResponse();
        response.setType("UserLinkCollection");
        response.setName("users");
        response.setAmount(users.size());
        response.setOffset(0);
        response.setLimit(50);
        response.setSortKeys(List.of("name"));
        response.setElements(users);
        return response;
    }
}