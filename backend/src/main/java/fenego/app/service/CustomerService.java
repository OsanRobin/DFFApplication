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

import java.util.ArrayList;
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

    public CustomerListResponse getCustomers(
            String authenticationToken,
            String domainName,
            int offset,
            int limit,
            String customerNo,
            String query,
            String type,
            String status,
            String segment,
            String email)
    {
        List<Customer> customers = customerRepository.findCustomersByDomain(
                domainName,
                offset,
                limit,
                customerNo,
                query,
                type,
                status,
                segment
        );

        Map<String, CustomerSegmentDTO> segmentById = loadCgSegments(authenticationToken);

        List<CustomerSegmentAssignment> assignments =
                customerRepository.findCustomerSegmentAssignmentsByDomain(domainName);

        Map<String, List<String>> segmentIdsByCustomerNo = assignments.stream()
                .filter(assignment -> hasCgPrefix(assignment.getSegmentId()))
                .collect(Collectors.groupingBy(
                        CustomerSegmentAssignment::getCustomerNo,
                        Collectors.mapping(CustomerSegmentAssignment::getSegmentId, Collectors.toList())
                ));

        applySegments(customers, segmentIdsByCustomerNo, segmentById);

        List<String> linkedCustomerNos = customers.stream()
                .filter(customer -> "ClusterCustomer".equalsIgnoreCase(customer.getType()))
                .flatMap(customer -> parseCustomerList(customer.getCustomerList()).stream())
                .distinct()
                .toList();

        List<Customer> linkedCustomers =
                customerRepository.findCustomersByCustomerNos(domainName, linkedCustomerNos);

        applySegments(linkedCustomers, segmentIdsByCustomerNo, segmentById);

        Map<String, Customer> linkedCustomerByNo = linkedCustomers.stream()
                .collect(Collectors.toMap(
                        Customer::getCustomerNo,
                        customer -> customer,
                        (a, b) -> a
                ));

        List<Customer> rows = new ArrayList<>();

        for (Customer customer : customers)
        {
            rows.add(customer);

            if (!"ClusterCustomer".equalsIgnoreCase(customer.getType()))
            {
                continue;
            }

            for (String childNo : parseCustomerList(customer.getCustomerList()))
            {
                Customer child = linkedCustomerByNo.get(childNo);

                if (child == null)
                {
                    continue;
                }

                Customer childRow = copyCustomer(child);
                childRow.setParentCustomerNo(customer.getCustomerNo());
                rows.add(childRow);
            }
        }

        CustomerListResponse response = new CustomerListResponse();
        response.setOffset(offset);
        response.setLimit(limit);
        response.setCount(customerRepository.countCustomersByDomain(
                domainName,
                customerNo,
                query,
                type,
                status,
                segment
        ));
        response.setData(rows);
        return response;
    }

    public CustomerDetailResponse getCustomerById(
            String authenticationToken,
            String domainName,
            String customerId)
    {
        CustomerDetailResponse response = customerRepository.findCustomerDetailById(customerId);

        if (response == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
        }

        Map<String, CustomerSegmentDTO> segmentById = loadCgSegments(authenticationToken);

        List<String> segmentIds = customerRepository.findSegmentIdsByCustomerNo(customerId).stream()
                .filter(this::hasCgPrefix)
                .toList();

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

        List<Customer> subCustomers =
                customerRepository.findSubCustomersForCluster(domainName, customerId);

        List<Customer> parentClusterCustomers =
                customerRepository.findParentClustersForSubCustomer(domainName, customerId);

        response.setSubCustomers(subCustomers);
        response.setParentClusterCustomers(parentClusterCustomers);

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

    private Map<String, CustomerSegmentDTO> loadCgSegments(String authenticationToken)
    {
        try
        {
            List<CustomerSegmentDTO> allSegments =
                    intershopClient.getAllCustomerSegments(authenticationToken);

            return allSegments.stream()
                    .filter(segmentDto -> hasCgPrefix(segmentDto.getId()))
                    .collect(Collectors.toMap(
                            CustomerSegmentDTO::getId,
                            segmentDto -> segmentDto,
                            (a, b) -> a,
                            LinkedHashMap::new
                    ));
        }
        catch (Exception ex)
        {
            System.err.println("Could not load customer segments: " + ex.getMessage());
            return new LinkedHashMap<>();
        }
    }

    private void applySegments(
            List<Customer> customers,
            Map<String, List<String>> segmentIdsByCustomerNo,
            Map<String, CustomerSegmentDTO> segmentById)
    {
        for (Customer customer : customers)
        {
            List<String> segmentIds =
                    segmentIdsByCustomerNo.getOrDefault(customer.getCustomerNo(), List.of());

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
    }

    private List<String> parseCustomerList(String value)
    {
        if (value == null || value.isBlank())
        {
            return List.of();
        }

        return List.of(value.split("[\\t|,;\\s]+")).stream()
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .distinct()
                .toList();
    }

    private Customer copyCustomer(Customer source)
    {
        Customer target = new Customer();
        target.setId(source.getId());
        target.setCustomerNo(source.getCustomerNo());
        target.setCustomerType(source.getCustomerType());
        target.setType(source.getType());
        target.setDisplayName(source.getDisplayName());
        target.setCompanyName(source.getCompanyName());
        target.setEmail(source.getEmail());
        target.setSegment(source.getSegment());
        target.setActive(source.isActive());
        target.setLocations(source.getLocations());
        target.setCustomerList(source.getCustomerList());
        target.setParentCustomerNo(source.getParentCustomerNo());
        return target;
    }

    private boolean hasCgPrefix(String value)
    {
        return value != null && value.toLowerCase().startsWith("cg");
    }
}