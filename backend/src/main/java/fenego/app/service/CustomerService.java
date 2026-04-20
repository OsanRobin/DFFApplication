package fenego.app.service;

import fenego.app.dto.CustomerDetailResponse;
import fenego.app.dto.CustomerListResponse;
import fenego.app.dto.CustomerSegmentDTO;
import fenego.app.dto.CustomerUserDTO;
import fenego.app.dto.CustomerUserListResponse;
import fenego.app.intershop.IntershopClient;
import fenego.app.jpa.Customer;
import fenego.app.jpa.CustomerSegmentAssignment;
import fenego.app.repository.CustomerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public CustomerListResponse getCustomers(String authenticationToken, String domainName, int offset, int limit, String customerNo, String email)
    {
        List<Customer> customers = customerRepository.findCustomersByDomain(domainName, offset, limit, customerNo);

        Map<String, CustomerSegmentDTO> tempSegmentById = new LinkedHashMap<>();

        try
        {
            List<CustomerSegmentDTO> allSegments = intershopClient.getAllCustomerSegments(authenticationToken);

            tempSegmentById = allSegments.stream()
                    .collect(Collectors.toMap(
                            CustomerSegmentDTO::getId,
                            s -> s,
                            (a, b) -> a,
                            LinkedHashMap::new
                    ));
        }
        catch (Exception ex)
        {
            System.err.println("Could not load customer segments in getCustomers: " + ex.getMessage());
        }

        final Map<String, CustomerSegmentDTO> segmentById = tempSegmentById;

        List<CustomerSegmentAssignment> assignments = customerRepository.findCustomerSegmentAssignmentsByDomain(domainName);

        Map<String, List<String>> segmentIdsByCustomerNo = assignments.stream()
                .collect(Collectors.groupingBy(
                        CustomerSegmentAssignment::getCustomerNo,
                        Collectors.mapping(CustomerSegmentAssignment::getSegmentId, Collectors.toList())
                ));

        for (Customer customer : customers)
        {
            List<String> segmentIds = segmentIdsByCustomerNo.getOrDefault(customer.getCustomerNo(), List.of());

            String segmentNames = segmentIds.stream()
                    .map(segmentId -> {
                        CustomerSegmentDTO dto = segmentById.get(segmentId);
                        if (dto != null && dto.getName() != null && !dto.getName().isBlank())
                        {
                            return dto.getName();
                        }
                        return segmentId;
                    })
                    .distinct()
                    .collect(Collectors.joining(", "));

            customer.setSegment(segmentNames.isBlank() ? "-" : segmentNames);
        }

        CustomerListResponse response = new CustomerListResponse();
        response.setOffset(offset);
        response.setLimit(limit);
        response.setCount(customerRepository.countCustomersByDomain(domainName, customerNo));
        response.setData(customers);
        return response;
    }

    public CustomerDetailResponse getCustomerById(String authenticationToken, String customerId)
    {
        CustomerDetailResponse response = customerRepository.findCustomerDetailById(customerId);

        if (response == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
        }

        Map<String, CustomerSegmentDTO> tempSegmentById = new LinkedHashMap<>();

        try
        {
            List<CustomerSegmentDTO> allSegments = intershopClient.getAllCustomerSegments(authenticationToken);

            tempSegmentById = allSegments.stream()
                    .collect(Collectors.toMap(
                            CustomerSegmentDTO::getId,
                            s -> s,
                            (a, b) -> a,
                            LinkedHashMap::new
                    ));
        }
        catch (Exception ex)
        {
            System.err.println("Could not load customer segments in getCustomerById: " + ex.getMessage());
        }

        final Map<String, CustomerSegmentDTO> segmentById = tempSegmentById;

        List<String> segmentIds = customerRepository.findSegmentIdsByCustomerNo(customerId);

        List<CustomerSegmentDTO> segments = segmentIds.stream()
                .map(segmentId -> {
                    CustomerSegmentDTO dto = segmentById.get(segmentId);
                    if (dto != null)
                    {
                        return dto;
                    }

                    CustomerSegmentDTO fallback = new CustomerSegmentDTO();
                    fallback.setId(segmentId);
                    fallback.setName(segmentId);
                    fallback.setDescription(null);
                    return fallback;
                })
                .toList();

        response.setSegments(segments);

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