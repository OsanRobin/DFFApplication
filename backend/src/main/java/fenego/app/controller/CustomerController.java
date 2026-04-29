package fenego.app.controller;

import fenego.app.dto.CustomerAttributeRequest;
import fenego.app.dto.CustomerDetailResponse;
import fenego.app.dto.CustomerListResponse;
import fenego.app.dto.CustomerSegmentRequest;
import fenego.app.dto.CustomerSegmentSummaryDTO;
import fenego.app.dto.CustomerUserDetailResponse;
import fenego.app.dto.CustomerUserListResponse;
import fenego.app.service.CustomerService;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@CrossOrigin(origins = "http://localhost:4200")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
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
            @RequestParam(required = false) String email) {
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
            @PathVariable String customerId) {
        return customerService.getCustomerById(authenticationToken, domain, customerId);
    }

    @GetMapping("/{customerId}/users")
    public CustomerUserListResponse getCustomerUsers(@PathVariable String customerId) {
        return customerService.getCustomerUsers(customerId);
    }

    @GetMapping("/{customerId}/users/{businessPartnerNo}")
    public CustomerUserDetailResponse getCustomerUserDetail(
            @PathVariable String customerId,
            @PathVariable String businessPartnerNo) {
        return customerService.getCustomerUserDetail(customerId, businessPartnerNo);
    }

    @PostMapping("/{customerId}/attributes")
    public void addCustomerAttribute(
            @RequestHeader("authentication-token") String authenticationToken,
            @RequestParam("domain") String domain,
            @PathVariable String customerId,
            @RequestBody CustomerAttributeRequest request) {
        customerService.addCustomerAttribute(authenticationToken, domain, customerId, request);
    }

    @PutMapping("/{customerId}/attributes")
    public void updateCustomerAttribute(
            @RequestHeader("authentication-token") String authenticationToken,
            @RequestParam("domain") String domain,
            @PathVariable String customerId,
            @RequestParam("attributeName") String attributeName,
            @RequestBody CustomerAttributeRequest request) {
        customerService.updateCustomerAttribute(authenticationToken, domain, customerId, attributeName, request);
    }

    @DeleteMapping("/{customerId}/attributes")
    public void deleteCustomerAttribute(
            @RequestHeader("authentication-token") String authenticationToken,
            @RequestParam("domain") String domain,
            @PathVariable String customerId,
            @RequestParam("attributeName") String attributeName) {
        customerService.deleteCustomerAttribute(authenticationToken, domain, customerId, attributeName);
    }

    @PostMapping("/{customerId}/users/{businessPartnerNo}/attributes")
    public void addCustomerUserAttribute(
            @RequestHeader("authentication-token") String authenticationToken,
            @PathVariable String customerId,
            @PathVariable String businessPartnerNo,
            @RequestBody CustomerAttributeRequest request) {
        customerService.addCustomerUserAttribute(authenticationToken, customerId, businessPartnerNo, request);
    }

    @PutMapping("/{customerId}/users/{businessPartnerNo}/attributes")
    public void updateCustomerUserAttribute(
            @RequestHeader("authentication-token") String authenticationToken,
            @PathVariable String customerId,
            @PathVariable String businessPartnerNo,
            @RequestParam("attributeName") String attributeName,
            @RequestBody CustomerAttributeRequest request) {
        customerService.updateCustomerUserAttribute(authenticationToken, customerId, businessPartnerNo, attributeName, request);
    }

    @DeleteMapping("/{customerId}/users/{businessPartnerNo}/attributes")
    public void deleteCustomerUserAttribute(
            @RequestHeader("authentication-token") String authenticationToken,
            @PathVariable String customerId,
            @PathVariable String businessPartnerNo,
            @RequestParam("attributeName") String attributeName) {
        customerService.deleteCustomerUserAttribute(authenticationToken, customerId, businessPartnerNo, attributeName);
    }

    @PostMapping("/{customerId}/users/{businessPartnerNo}/customer-list")
    public void addCustomerToUserCustomerList(
            @PathVariable String customerId,
            @PathVariable String businessPartnerNo,
            @RequestBody CustomerAttributeRequest request) {
        customerService.addCustomerToUserCustomerList(customerId, businessPartnerNo, request);
    }

    @DeleteMapping("/{customerId}/users/{businessPartnerNo}/customer-list/{customerNo}")
    public void removeCustomerFromUserCustomerList(
            @PathVariable String customerId,
            @PathVariable String businessPartnerNo,
            @PathVariable String customerNo) {
        customerService.removeCustomerFromUserCustomerList(customerId, businessPartnerNo, customerNo);
    }
  @PostMapping("/{customerId}/relations/sub-customers/{subCustomerNo}")
public void addSubCustomerToCluster(
        @RequestHeader("authentication-token") String authenticationToken,
        @RequestParam("domain") String domain,
        @PathVariable String customerId,
        @PathVariable String subCustomerNo) {
    customerService.addSubCustomerToCluster(authenticationToken, domain, customerId, subCustomerNo);
}

@DeleteMapping("/{customerId}/relations/sub-customers/{subCustomerNo}")
public void removeSubCustomerFromCluster(
        @RequestHeader("authentication-token") String authenticationToken,
        @RequestParam("domain") String domain,
        @PathVariable String customerId,
        @PathVariable String subCustomerNo) {
    customerService.removeSubCustomerFromCluster(authenticationToken, domain, customerId, subCustomerNo);
}

@PostMapping("/{customerId}/relations/parent-clusters/{clusterCustomerNo}")
public void assignCustomerToCluster(
        @RequestHeader("authentication-token") String authenticationToken,
        @RequestParam("domain") String domain,
        @PathVariable String customerId,
        @PathVariable String clusterCustomerNo) {
    customerService.addSubCustomerToCluster(authenticationToken, domain, clusterCustomerNo, customerId);
}

@DeleteMapping("/{customerId}/relations/parent-clusters/{clusterCustomerNo}")
public void unassignCustomerFromCluster(
        @RequestHeader("authentication-token") String authenticationToken,
        @RequestParam("domain") String domain,
        @PathVariable String customerId,
        @PathVariable String clusterCustomerNo) {
    customerService.removeSubCustomerFromCluster(authenticationToken, domain, clusterCustomerNo, customerId);
}
@GetMapping("/segments")
public List<CustomerSegmentSummaryDTO> getSegments(
        @RequestHeader("authentication-token") String authenticationToken,
        @RequestParam("domain") String domain) {
    return customerService.getSegments(authenticationToken, domain);
}

@PostMapping("/segments")
public void createSegment(
        @RequestHeader("authentication-token") String authenticationToken,
        @RequestParam("domain") String domain,
        @RequestBody CustomerSegmentRequest request) {
    customerService.createSegment(authenticationToken, domain, request);
}

@DeleteMapping("/segments/{segmentId}")
public void deleteSegment(
        @RequestHeader("authentication-token") String authenticationToken,
        @RequestParam("domain") String domain,
        @PathVariable String segmentId) {
    customerService.deleteSegment(authenticationToken, domain, segmentId);
}
}