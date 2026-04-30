package fenego.app.service;

import fenego.app.dto.CustomerAttributeRequest;
import fenego.app.dto.CustomerDetailResponse;
import fenego.app.dto.CustomerListResponse;
import fenego.app.dto.CustomerSegmentDTO;
import fenego.app.dto.CustomerSegmentRequest;
import fenego.app.dto.CustomerSegmentSummaryDTO;
import fenego.app.dto.CustomerUserAttributeDTO;
import fenego.app.dto.CustomerUserDTO;
import fenego.app.dto.CustomerUserDetailResponse;
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
    private final AuditLogService auditLogService;

    public CustomerService(
            CustomerRepository customerRepository,
            IntershopClient intershopClient,
            AuditLogService auditLogService)
    {
        this.customerRepository = customerRepository;
        this.intershopClient = intershopClient;
        this.auditLogService = auditLogService;
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
            String email,
            String currentUser,
            boolean managerRestricted)
    {
        List<String> allowedCustomerNos = managerRestricted
                ? getAllowedCustomerNosForManager(currentUser, domainName)
                : List.of();

        boolean applyManagerFilter = managerRestricted && !isBackofficeAdmin(currentUser);

        List<Customer> customers;

        if (applyManagerFilter)
        {
            customers = customerRepository.findCustomersByCustomerNos(domainName, allowedCustomerNos);

            if (customerNo != null && !customerNo.isBlank())
            {
                String filter = customerNo.trim().toLowerCase();
                customers = customers.stream()
                        .filter(customer -> customer.getCustomerNo() != null
                                && customer.getCustomerNo().toLowerCase().contains(filter))
                        .toList();
            }

            if (query != null && !query.isBlank())
            {
                String filter = query.trim().toLowerCase();
                customers = customers.stream()
                        .filter(customer ->
                                containsIgnoreCase(customer.getCustomerNo(), filter)
                                        || containsIgnoreCase(customer.getDisplayName(), filter)
                                        || containsIgnoreCase(customer.getCompanyName(), filter))
                        .toList();
            }

            if (type != null && !type.isBlank())
            {
                String filter = type.trim();
                customers = customers.stream()
                        .filter(customer -> filter.equalsIgnoreCase(customer.getType()))
                        .toList();
            }

            if (status != null && !status.isBlank())
            {
                String filter = status.trim();
                customers = customers.stream()
                        .filter(customer ->
                                ("Active".equalsIgnoreCase(filter) && customer.isActive())
                                        || ("Inactive".equalsIgnoreCase(filter) && !customer.isActive()))
                        .toList();
            }
        }
        else
        {
            customers = customerRepository.findCustomersByDomain(
                    domainName,
                    offset,
                    limit,
                    customerNo,
                    query,
                    type,
                    status,
                    segment
            );
        }

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

        if (applyManagerFilter && segment != null && !segment.isBlank())
        {
            String filter = segment.trim().toLowerCase();
            customers = customers.stream()
                    .filter(customer -> customer.getSegment() != null
                            && customer.getSegment().toLowerCase().contains(filter))
                    .toList();
        }

        List<String> linkedCustomerNos = customers.stream()
                .filter(customer -> "ClusterCustomer".equalsIgnoreCase(customer.getType()))
                .flatMap(customer -> parseCustomerList(customer.getCustomerList()).stream())
                .distinct()
                .toList();

        List<Customer> linkedCustomers =
                customerRepository.findCustomersByCustomerNos(domainName, linkedCustomerNos);

        if (applyManagerFilter)
        {
            linkedCustomers = linkedCustomers.stream()
                    .filter(customer -> allowedCustomerNos.contains(customer.getCustomerNo()))
                    .toList();
        }

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

        if (applyManagerFilter)
        {
            response.setCount(rows.size());
        }
        else
        {
            response.setCount(customerRepository.countCustomersByDomain(
                    domainName,
                    customerNo,
                    query,
                    type,
                    status,
                    segment
            ));
        }

        response.setData(rows);
        return response;
    }

    public CustomerDetailResponse getCustomerById(
            String authenticationToken,
            String domainName,
            String customerId,
            String currentUser,
            boolean managerRestricted)
    {
        List<String> allowedCustomerNos = managerRestricted
                ? getAllowedCustomerNosForManager(currentUser, domainName)
                : List.of();

        boolean applyManagerFilter = managerRestricted && !isBackofficeAdmin(currentUser);

        if (applyManagerFilter && !allowedCustomerNos.contains(customerId))
        {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customer not assigned to manager");
        }

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
        response.setAttributes(customerRepository.findAttributesByCustomerNo(customerId));

        List<Customer> subCustomers =
                customerRepository.findSubCustomersForCluster(domainName, customerId);

        List<Customer> parentClusterCustomers =
                customerRepository.findParentClustersForSubCustomer(domainName, customerId);

        if (applyManagerFilter)
        {
            subCustomers = subCustomers.stream()
                    .filter(customer -> allowedCustomerNos.contains(customer.getCustomerNo()))
                    .toList();

            parentClusterCustomers = parentClusterCustomers.stream()
                    .filter(customer -> allowedCustomerNos.contains(customer.getCustomerNo()))
                    .toList();
        }

        response.setSubCustomers(subCustomers);
        response.setParentClusterCustomers(parentClusterCustomers);

        return response;
    }

    public CustomerDetailResponse getCustomerById(
            String authenticationToken,
            String domainName,
            String customerId)
    {
        return getCustomerById(authenticationToken, domainName, customerId, null, false);
    }

    public void addCustomerAttribute(
            String authenticationToken,
            String domainName,
            String customerId,
            CustomerAttributeRequest request)
    {
        if (domainName == null || domainName.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Domain is required");
        }

        if (customerId == null || customerId.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer id is required");
        }

        if (request == null || request.getName() == null || request.getName().isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attribute name is required");
        }

        String name = request.getName().trim();
        String value = request.getValue() == null ? "" : request.getValue();
        String changedBy = "system";

        try
        {
            customerRepository.saveCustomerAttribute(customerId, name, value);

            auditLogService.logChange(
                    "CUSTOMER_ATTRIBUTE",
                    customerId,
                    "CREATE",
                    name,
                    "",
                    value,
                    changedBy,
                    "Customer attribute added"
            );
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Local DB save attribute failed: " + ex.getMessage()
            );
        }

        try
        {
            intershopClient.addCustomerAttribute(authenticationToken, customerId, name, value);
        }
        catch (Exception ex)
        {
            System.err.println("Intershop add attribute failed, but local DB was updated: " + ex.getMessage());
        }
    }

    public void updateCustomerAttribute(
            String authenticationToken,
            String domainName,
            String customerId,
            String attributeName,
            CustomerAttributeRequest request)
    {
        if (domainName == null || domainName.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Domain is required");
        }

        if (customerId == null || customerId.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer id is required");
        }

        if (attributeName == null || attributeName.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attribute name is required");
        }

        String name = attributeName.trim();
        String value = request == null || request.getValue() == null ? "" : request.getValue();
        String changedBy = "system";

        customerRepository.saveCustomerAttribute(customerId, name, value);

        auditLogService.logChange(
                "CUSTOMER_ATTRIBUTE",
                customerId,
                "UPDATE",
                name,
                "",
                value,
                changedBy,
                "Customer attribute updated"
        );

        try
        {
            intershopClient.updateCustomerAttribute(authenticationToken, customerId, name, value);
        }
        catch (Exception ex)
        {
            System.err.println("Intershop update attribute failed, but local DB was updated: " + ex.getMessage());
        }
    }

    public void deleteCustomerAttribute(
            String authenticationToken,
            String domainName,
            String customerId,
            String attributeName)
    {
        if (domainName == null || domainName.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Domain is required");
        }

        if (customerId == null || customerId.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer id is required");
        }

        if (attributeName == null || attributeName.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attribute name is required");
        }

        String name = attributeName.trim();
        String changedBy = "system";

        customerRepository.deleteCustomerAttribute(customerId, name);

        auditLogService.logChange(
                "CUSTOMER_ATTRIBUTE",
                customerId,
                "DELETE",
                name,
                "",
                "",
                changedBy,
                "Customer attribute deleted"
        );
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

    public CustomerUserDetailResponse getCustomerUserDetail(String customerId, String businessPartnerNo)
    {
        if (customerId == null || customerId.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer id is required");
        }

        if (businessPartnerNo == null || businessPartnerNo.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Business partner no is required");
        }

        String normalizedBusinessPartnerNo = normalizeBusinessPartnerNo(businessPartnerNo);

        CustomerUserDTO user = customerRepository.findUserByCustomerIdAndBusinessPartnerNo(
                customerId,
                normalizedBusinessPartnerNo
        );

        if (user == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found for this customer");
        }

        List<CustomerUserAttributeDTO> attributes =
                customerRepository.findUserAttributesByCustomerIdAndBusinessPartnerNo(
                        customerId,
                        normalizedBusinessPartnerNo
                );

        CustomerUserDetailResponse response = new CustomerUserDetailResponse();
        response.setUser(user);
        response.setAttributes(attributes);
        return response;
    }

    public void addCustomerToUserCustomerList(
            String customerId,
            String businessPartnerNo,
            CustomerAttributeRequest request)
    {
        if (customerId == null || customerId.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer id is required");
        }

        if (businessPartnerNo == null || businessPartnerNo.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Business partner no is required");
        }

        if (request == null || request.getValue() == null || request.getValue().isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer no is required");
        }

        String normalizedBusinessPartnerNo = normalizeBusinessPartnerNo(businessPartnerNo);
        String customerNoToAdd = request.getValue().trim();

        CustomerUserDTO user = customerRepository.findUserByCustomerIdAndBusinessPartnerNo(
                customerId,
                normalizedBusinessPartnerNo
        );

        if (user == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found for this customer");
        }

        List<String> customerNos = new ArrayList<>(
                parseCustomerList(customerRepository.findUserAttributeValue(normalizedBusinessPartnerNo, "CustomerList"))
        );

        if (!customerNos.contains(customerNoToAdd))
        {
            customerNos.add(customerNoToAdd);
        }

        customerRepository.saveUserAttribute(
                normalizedBusinessPartnerNo,
                "CustomerList",
                String.join("\t", customerNos)
        );
    }

    public void removeCustomerFromUserCustomerList(
            String customerId,
            String businessPartnerNo,
            String customerNo)
    {
        if (businessPartnerNo == null || businessPartnerNo.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Business partner no is required");
        }

        if (customerNo == null || customerNo.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer no is required");
        }

        String normalizedBusinessPartnerNo = normalizeBusinessPartnerNo(businessPartnerNo);
        String normalizedCustomerNo = customerNo.trim();

        String currentValue = customerRepository.findUserAttributeValue(
                normalizedBusinessPartnerNo,
                "CustomerList"
        );

        List<String> customerNos = parseCustomerList(currentValue).stream()
                .filter(value -> !value.equalsIgnoreCase(normalizedCustomerNo))
                .toList();

        customerRepository.saveUserAttribute(
                normalizedBusinessPartnerNo,
                "CustomerList",
                String.join("\t", customerNos)
        );

        auditLogService.logChange(
                "USER_CUSTOMERLIST",
                normalizedBusinessPartnerNo,
                "DELETE",
                "CustomerList",
                normalizedCustomerNo,
                String.join("\t", customerNos),
                "system",
                "Customer removed from user CustomerList"
        );
    }

    public void addCustomerUserAttribute(
            String authenticationToken,
            String customerId,
            String businessPartnerNo,
            CustomerAttributeRequest request)
    {
        if (customerId == null || customerId.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer id is required");
        }

        if (businessPartnerNo == null || businessPartnerNo.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Business partner no is required");
        }

        if (request == null || request.getName() == null || request.getName().isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attribute name is required");
        }

        String normalizedBusinessPartnerNo = normalizeBusinessPartnerNo(businessPartnerNo);
        String name = request.getName().trim();
        String value = request.getValue() == null ? "" : request.getValue();

        CustomerUserDTO user = customerRepository.findUserByCustomerIdAndBusinessPartnerNo(
                customerId,
                normalizedBusinessPartnerNo
        );

        if (user == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found for this customer");
        }

        customerRepository.saveUserAttribute(normalizedBusinessPartnerNo, name, value);

        auditLogService.logChange(
                "USER_ATTRIBUTE",
                normalizedBusinessPartnerNo,
                "CREATE",
                name,
                "",
                value,
                "system",
                "User attribute added"
        );
    }

    public void updateCustomerUserAttribute(
            String authenticationToken,
            String customerId,
            String businessPartnerNo,
            String attributeName,
            CustomerAttributeRequest request)
    {
        if (customerId == null || customerId.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer id is required");
        }

        if (businessPartnerNo == null || businessPartnerNo.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Business partner no is required");
        }

        if (attributeName == null || attributeName.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attribute name is required");
        }

        String normalizedBusinessPartnerNo = normalizeBusinessPartnerNo(businessPartnerNo);
        String name = attributeName.trim();
        String value = request == null || request.getValue() == null ? "" : request.getValue();

        CustomerUserDTO user = customerRepository.findUserByCustomerIdAndBusinessPartnerNo(
                customerId,
                normalizedBusinessPartnerNo
        );

        if (user == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found for this customer");
        }

        customerRepository.saveUserAttribute(normalizedBusinessPartnerNo, name, value);

        auditLogService.logChange(
                "USER_ATTRIBUTE",
                normalizedBusinessPartnerNo,
                "UPDATE",
                name,
                "",
                value,
                "system",
                "User attribute updated"
        );
    }

    public void deleteCustomerUserAttribute(
            String authenticationToken,
            String customerId,
            String businessPartnerNo,
            String attributeName)
    {
        if (customerId == null || customerId.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer id is required");
        }

        if (businessPartnerNo == null || businessPartnerNo.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Business partner no is required");
        }

        if (attributeName == null || attributeName.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attribute name is required");
        }

        String normalizedBusinessPartnerNo = normalizeBusinessPartnerNo(businessPartnerNo);
        String name = attributeName.trim();

        CustomerUserDTO user = customerRepository.findUserByCustomerIdAndBusinessPartnerNo(
                customerId,
                normalizedBusinessPartnerNo
        );

        if (user == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found for this customer");
        }

        customerRepository.deleteUserAttribute(normalizedBusinessPartnerNo, name);

        auditLogService.logChange(
                "USER_ATTRIBUTE",
                normalizedBusinessPartnerNo,
                "DELETE",
                name,
                "",
                "",
                "system",
                "User attribute deleted"
        );
    }

    public void addSubCustomerToCluster(
            String authenticationToken,
            String domainName,
            String clusterCustomerNo,
            String subCustomerNo)
    {
        if (domainName == null || domainName.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Domain is required");
        }

        if (clusterCustomerNo == null || clusterCustomerNo.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cluster customer no is required");
        }

        if (subCustomerNo == null || subCustomerNo.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sub customer no is required");
        }

        String clusterNo = clusterCustomerNo.trim();
        String subNo = subCustomerNo.trim();

        List<Customer> clusterResults = customerRepository.findCustomersByCustomerNos(domainName, List.of(clusterNo));

        if (clusterResults.isEmpty())
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cluster customer not found");
        }

        Customer cluster = clusterResults.get(0);

        if (!"ClusterCustomer".equalsIgnoreCase(cluster.getType()))
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer is not a ClusterCustomer");
        }

        List<Customer> subResults = customerRepository.findCustomersByCustomerNos(domainName, List.of(subNo));

        if (subResults.isEmpty())
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer to add not found");
        }

        Customer subCustomer = subResults.get(0);

        if ("ClusterCustomer".equalsIgnoreCase(subCustomer.getType()))
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot add a ClusterCustomer as sub customer");
        }

        List<String> customerNos = new ArrayList<>(parseCustomerList(cluster.getCustomerList()));

        if (!customerNos.contains(subNo))
        {
            customerNos.add(subNo);
        }

        String newValue = String.join("\t", customerNos);

        customerRepository.saveCustomerAttribute(clusterNo, "CustomerList", newValue);

        auditLogService.logChange(
                "CUSTOMER_RELATION",
                clusterNo,
                "ADD",
                "CustomerList",
                "",
                subNo,
                "system",
                "Sub customer added to cluster"
        );

        try
        {
            intershopClient.updateCustomerAttribute(authenticationToken, clusterNo, "CustomerList", newValue);
        }
        catch (Exception ex)
        {
            System.err.println("Intershop relation update failed, but local DB was updated: " + ex.getMessage());
        }
    }

    public void removeSubCustomerFromCluster(
            String authenticationToken,
            String domainName,
            String clusterCustomerNo,
            String subCustomerNo)
    {
        if (domainName == null || domainName.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Domain is required");
        }

        if (clusterCustomerNo == null || clusterCustomerNo.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cluster customer no is required");
        }

        if (subCustomerNo == null || subCustomerNo.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sub customer no is required");
        }

        String clusterNo = clusterCustomerNo.trim();
        String subNo = subCustomerNo.trim();

        List<Customer> clusterResults = customerRepository.findCustomersByCustomerNos(domainName, List.of(clusterNo));

        if (clusterResults.isEmpty())
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cluster customer not found");
        }

        Customer cluster = clusterResults.get(0);

        if (!"ClusterCustomer".equalsIgnoreCase(cluster.getType()))
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer is not a ClusterCustomer");
        }

        List<String> customerNos = parseCustomerList(cluster.getCustomerList()).stream()
                .filter(value -> !value.equalsIgnoreCase(subNo))
                .toList();

        String newValue = String.join("\t", customerNos);

        customerRepository.saveCustomerAttribute(clusterNo, "CustomerList", newValue);

        auditLogService.logChange(
                "CUSTOMER_RELATION",
                clusterNo,
                "DELETE",
                "CustomerList",
                subNo,
                newValue,
                "system",
                "Sub customer removed from cluster"
        );

        try
        {
            intershopClient.updateCustomerAttribute(authenticationToken, clusterNo, "CustomerList", newValue);
        }
        catch (Exception ex)
        {
            System.err.println("Intershop relation update failed, but local DB was updated: " + ex.getMessage());
        }
    }

    public List<CustomerSegmentSummaryDTO> getSegments(String authenticationToken, String domainName)
    {
        Map<String, CustomerSegmentDTO> segmentById = new LinkedHashMap<>();

        Map<String, CustomerSegmentDTO> intershopSegments = loadCgSegments(authenticationToken);
        segmentById.putAll(intershopSegments);

        for (CustomerSegmentDTO localSegment : customerRepository.findLocalSegments())
        {
            segmentById.put(localSegment.getId(), localSegment);
        }

        List<CustomerSegmentAssignment> assignments =
                customerRepository.findCustomerSegmentAssignmentsByDomain(domainName);

        Map<String, Long> countBySegmentId = assignments.stream()
                .filter(a -> a.getSegmentId() != null && !a.getSegmentId().isBlank())
                .collect(Collectors.groupingBy(
                        CustomerSegmentAssignment::getSegmentId,
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        for (String segmentId : countBySegmentId.keySet())
        {
            segmentById.computeIfAbsent(segmentId, id -> {
                CustomerSegmentDTO dto = new CustomerSegmentDTO();
                dto.setId(id);
                dto.setName(id);
                dto.setDescription("Loaded from local database");
                return dto;
            });
        }

        return segmentById.values().stream()
                .map(segmentDto -> {
                    CustomerSegmentSummaryDTO dto = new CustomerSegmentSummaryDTO();
                    dto.setId(segmentDto.getId());
                    dto.setName(segmentDto.getName());
                    dto.setDescription(segmentDto.getDescription());
                    dto.setCustomerCount(countBySegmentId.getOrDefault(segmentDto.getId(), 0L).intValue());
                    return dto;
                })
                .sorted((a, b) -> a.getId().compareToIgnoreCase(b.getId()))
                .toList();
    }

    public void assignSegmentToCustomer(
            String authenticationToken,
            String domainName,
            String customerId,
            CustomerSegmentRequest request)
    {
        if (domainName == null || domainName.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Domain is required");
        }

        if (customerId == null || customerId.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer id is required");
        }

        if (request == null || request.getId() == null || request.getId().isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Segment id is required");
        }

        String segmentId = request.getId().trim();

        CustomerDetailResponse customer = customerRepository.findCustomerDetailById(customerId);

        if (customer == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
        }

        customerRepository.assignSegmentToCustomer(domainName, customerId, segmentId);

        auditLogService.logChange(
                "CUSTOMER_SEGMENT",
                customerId,
                "ASSIGN",
                "segment",
                "",
                segmentId,
                "system",
                "Segment assigned to customer"
        );
    }

    public void removeSegmentFromCustomer(
            String authenticationToken,
            String domainName,
            String customerId,
            String segmentId)
    {
        if (domainName == null || domainName.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Domain is required");
        }

        if (customerId == null || customerId.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer id is required");
        }

        if (segmentId == null || segmentId.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Segment id is required");
        }

        CustomerDetailResponse customer = customerRepository.findCustomerDetailById(customerId);

        if (customer == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
        }

        String normalizedSegmentId = segmentId.trim();

        customerRepository.removeSegmentFromCustomer(customerId, normalizedSegmentId);

        auditLogService.logChange(
                "CUSTOMER_SEGMENT",
                customerId,
                "REMOVE",
                "segment",
                normalizedSegmentId,
                "",
                "system",
                "Segment removed from customer"
        );
    }

    public void createSegment(String authenticationToken, String domainName, CustomerSegmentRequest request)
    {
        if (request == null || request.getId() == null || request.getId().isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Segment id is required");
        }

        if (request.getName() == null || request.getName().isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Segment name is required");
        }

        customerRepository.saveLocalSegment(
                request.getId().trim(),
                request.getName().trim(),
                request.getDescription() == null ? "" : request.getDescription().trim()
        );
    }

    public void deleteSegment(String authenticationToken, String domainName, String segmentId)
    {
        if (segmentId == null || segmentId.isBlank())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Segment id is required");
        }

        customerRepository.deleteLocalSegment(segmentId.trim());
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

    private boolean isBackofficeAdmin(String currentUser)
    {
        if (currentUser == null || currentUser.isBlank())
        {
            return false;
        }

        String normalizedUser = normalizeBusinessPartnerNo(currentUser);

        return "admin".equalsIgnoreCase(normalizedUser);
    }

    private List<String> getAllowedCustomerNosForManager(String currentUser, String domainName)
    {
        if (currentUser == null || currentUser.isBlank())
        {
            return List.of();
        }

        String normalizedUser = normalizeBusinessPartnerNo(currentUser);

        List<String> assignedCustomerNos =
                customerRepository.findAssignedCustomerNosForUser(normalizedUser, domainName);

        System.out.println("MANAGER FILTER currentUser = " + normalizedUser);
        System.out.println("MANAGER FILTER assignedCustomerNos = " + assignedCustomerNos);

        return assignedCustomerNos;
    }

    private String normalizeBusinessPartnerNo(String value)
    {
        String normalized = value.trim();

        if (normalized.contains(":"))
        {
            normalized = normalized.substring(0, normalized.indexOf(":"));
        }

        return normalized;
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

    private boolean containsIgnoreCase(String value, String filter)
    {
        return value != null && value.toLowerCase().contains(filter);
    }
}