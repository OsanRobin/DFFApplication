package fenego.app;

import fenego.app.dto.CustomerAttributeRequest;
import fenego.app.dto.CustomerDetailResponse;
import fenego.app.dto.CustomerSegmentDTO;
import fenego.app.dto.CustomerSegmentRequest;
import fenego.app.dto.CustomerSegmentSummaryDTO;
import fenego.app.dto.CustomerUserDTO;
import fenego.app.dto.CustomerUserDetailResponse;
import fenego.app.dto.CustomerUserListResponse;
import fenego.app.intershop.IntershopClient;
import fenego.app.jpa.Customer;
import fenego.app.jpa.CustomerSegmentAssignment;
import fenego.app.repository.CustomerRepository;
import fenego.app.service.AuditLogService;
import fenego.app.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest
{
    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private IntershopClient intershopClient;

    @Mock
    private AuditLogService auditLogService;

    private CustomerService customerService;

    @BeforeEach
    void setUp()
    {
        customerService = new CustomerService(
                customerRepository,
                intershopClient,
                auditLogService
        );
    }

    @Test
    void getCustomersShouldReturnCustomersForUnrestrictedUser()
    {
        Customer customer = customer("1", "C100", "StandardCustomer", "Acme", true);

        when(customerRepository.findCustomersByDomain(
                eq("domain"),
                eq(0),
                eq(10),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(List.of(customer));

        when(customerRepository.countCustomersByDomain(
                eq("domain"),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(1);

        when(customerRepository.findCustomerSegmentAssignmentsByDomain("domain"))
                .thenReturn(List.of());

        var response = customerService.getCustomers(
                "token",
                "domain",
                0,
                10,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false
        );

        assertThat(response.getOffset()).isEqualTo(0);
        assertThat(response.getLimit()).isEqualTo(10);
        assertThat(response.getCount()).isEqualTo(1);
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().get(0).getCustomerNo()).isEqualTo("C100");
        assertThat(response.getData().get(0).getSegment()).isEqualTo("-");
    }

    @Test
    void getCustomersShouldApplyManagerFilter()
    {
        Customer allowed = customer("1", "C100", "StandardCustomer", "Allowed customer", true);
       

        when(customerRepository.findAssignedCustomerNosForUser("manager", "domain"))
                .thenReturn(List.of("C100"));

        when(customerRepository.findCustomersByCustomerNos("domain", List.of("C100")))
                .thenReturn(List.of(allowed));

        when(customerRepository.findCustomerSegmentAssignmentsByDomain("domain"))
                .thenReturn(List.of());

        var response = customerService.getCustomers(
                "token",
                "domain",
                0,
                10,
                null,
                null,
                null,
                null,
                null,
                null,
                "manager:domain",
                true
        );

        assertThat(response.getCount()).isEqualTo(1);
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().get(0).getCustomerNo()).isEqualTo("C100");

        verify(customerRepository, never()).findCustomersByDomain(
                anyString(),
                anyInt(),
                anyInt(),
                any(),
                any(),
                any(),
                any(),
                any()
        );
    }

    @Test
    void getCustomersShouldAllowBackofficeAdminWithoutManagerFilter()
    {
        Customer customer = customer("1", "C100", "StandardCustomer", "Acme", true);

        when(customerRepository.findAssignedCustomerNosForUser("admin", "domain"))
                .thenReturn(List.of());

        when(customerRepository.findCustomersByDomain(
                eq("domain"),
                eq(0),
                eq(10),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(List.of(customer));

        when(customerRepository.countCustomersByDomain(
                eq("domain"),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(1);

        when(customerRepository.findCustomerSegmentAssignmentsByDomain("domain"))
                .thenReturn(List.of());

        var response = customerService.getCustomers(
                "token",
                "domain",
                0,
                10,
                null,
                null,
                null,
                null,
                null,
                null,
                "admin",
                true
        );

        assertThat(response.getCount()).isEqualTo(1);
        assertThat(response.getData()).hasSize(1);

        verify(customerRepository).findCustomersByDomain(
                eq("domain"),
                eq(0),
                eq(10),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        );
    }

    @Test
    void getCustomersShouldExpandClusterCustomersWithLinkedChildren()
    {
        Customer cluster = customer("1", "CL100", "ClusterCustomer", "Cluster", true);
        cluster.setCustomerList("C100\tC200");

        Customer child1 = customer("2", "C100", "StandardCustomer", "Child 1", true);
        Customer child2 = customer("3", "C200", "StandardCustomer", "Child 2", true);

        when(customerRepository.findCustomersByDomain(
                eq("domain"),
                eq(0),
                eq(10),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(List.of(cluster));

        when(customerRepository.countCustomersByDomain(
                eq("domain"),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(1);

        when(customerRepository.findCustomerSegmentAssignmentsByDomain("domain"))
                .thenReturn(List.of());

        when(customerRepository.findCustomersByCustomerNos("domain", List.of("C100", "C200")))
                .thenReturn(List.of(child1, child2));

        var response = customerService.getCustomers(
                "token",
                "domain",
                0,
                10,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false
        );

        assertThat(response.getData()).hasSize(3);
        assertThat(response.getData().get(0).getCustomerNo()).isEqualTo("CL100");
        assertThat(response.getData().get(1).getCustomerNo()).isEqualTo("C100");
        assertThat(response.getData().get(1).getParentCustomerNo()).isEqualTo("CL100");
        assertThat(response.getData().get(2).getCustomerNo()).isEqualTo("C200");
        assertThat(response.getData().get(2).getParentCustomerNo()).isEqualTo("CL100");
    }

    @Test
    void getCustomerByIdShouldReturnDetailWithCgSegmentsAndAttributes()
    {
        CustomerDetailResponse detail = new CustomerDetailResponse();
        detail.setCustomerNo("C100");

        CustomerSegmentDTO segment = segment("cgVip", "VIP", "VIP customers");

        when(customerRepository.findCustomerDetailById("C100")).thenReturn(detail);
        when(intershopClient.getAllCustomerSegments("token")).thenReturn(List.of(
                segment,
                segment("other", "Other", "")
        ));
        when(customerRepository.findSegmentIdsByCustomerNo("C100"))
                .thenReturn(List.of("cgVip", "other"));
        when(customerRepository.findAttributesByCustomerNo("C100"))
                .thenReturn(List.of());
        when(customerRepository.findSubCustomersForCluster("domain", "C100"))
                .thenReturn(List.of());
        when(customerRepository.findParentClustersForSubCustomer("domain", "C100"))
                .thenReturn(List.of());

        CustomerDetailResponse response = customerService.getCustomerById(
                "token",
                "domain",
                "C100",
                null,
                false
        );

        assertThat(response).isSameAs(detail);
        assertThat(response.getSegments()).hasSize(1);
        assertThat(response.getSegments().get(0).getId()).isEqualTo("cgVip");
        assertThat(response.getSegments().get(0).getName()).isEqualTo("VIP");
    }

    @Test
    void getCustomerByIdShouldThrowForbiddenWhenManagerHasNoAccess()
    {
        when(customerRepository.findAssignedCustomerNosForUser("manager", "domain"))
                .thenReturn(List.of("C200"));

        assertThatThrownBy(() -> customerService.getCustomerById(
                "token",
                "domain",
                "C100",
                "manager",
                true
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getCustomerByIdShouldThrowNotFound()
    {
        when(customerRepository.findCustomerDetailById("C100")).thenReturn(null);

        assertThatThrownBy(() -> customerService.getCustomerById(
                "token",
                "domain",
                "C100",
                null,
                false
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void addCustomerAttributeShouldSaveLocallyAuditAndCallIntershop()
    {
        CustomerAttributeRequest request = new CustomerAttributeRequest();
        request.setName("Color");
        request.setValue("Blue");

        customerService.addCustomerAttribute("token", "domain", "C100", request);

        verify(customerRepository).saveCustomerAttribute("C100", "Color", "Blue");
        verify(auditLogService).logChange(
                eq("CUSTOMER_ATTRIBUTE"),
                eq("C100"),
                eq("CREATE"),
                eq("Color"),
                eq(""),
                eq("Blue"),
                eq("system"),
                eq("Customer attribute added")
        );
        verify(intershopClient).addCustomerAttribute("token", "C100", "Color", "Blue");
    }

    @Test
    void addCustomerAttributeShouldThrowBadRequestWhenNameMissing()
    {
        CustomerAttributeRequest request = new CustomerAttributeRequest();
        request.setName(" ");

        assertThatThrownBy(() -> customerService.addCustomerAttribute(
                "token",
                "domain",
                "C100",
                request
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verifyNoInteractions(intershopClient);
    }

    @Test
    void updateCustomerAttributeShouldSaveAuditAndCallIntershop()
    {
        CustomerAttributeRequest request = new CustomerAttributeRequest();
        request.setValue("New value");

        customerService.updateCustomerAttribute(
                "token",
                "domain",
                "C100",
                "Attr",
                request
        );

        verify(customerRepository).saveCustomerAttribute("C100", "Attr", "New value");
        verify(auditLogService).logChange(
                eq("CUSTOMER_ATTRIBUTE"),
                eq("C100"),
                eq("UPDATE"),
                eq("Attr"),
                eq(""),
                eq("New value"),
                eq("system"),
                eq("Customer attribute updated")
        );
        verify(intershopClient).updateCustomerAttribute("token", "C100", "Attr", "New value");
    }

    @Test
    void deleteCustomerAttributeShouldDeleteAndAudit()
    {
        customerService.deleteCustomerAttribute(
                "token",
                "domain",
                "C100",
                "Attr"
        );

        verify(customerRepository).deleteCustomerAttribute("C100", "Attr");
        verify(auditLogService).logChange(
                eq("CUSTOMER_ATTRIBUTE"),
                eq("C100"),
                eq("DELETE"),
                eq("Attr"),
                eq(""),
                eq(""),
                eq("system"),
                eq("Customer attribute deleted")
        );
    }

    @Test
    void getCustomerUsersShouldReturnUserListResponse()
    {
        CustomerUserDTO user = new CustomerUserDTO();
        user.setBusinessPartnerNo("BP100");

        when(customerRepository.findUsersByCustomerId("C100"))
                .thenReturn(List.of(user));

        CustomerUserListResponse response = customerService.getCustomerUsers("C100");

        assertThat(response.getType()).isEqualTo("UserLinkCollection");
        assertThat(response.getName()).isEqualTo("users");
        assertThat(response.getAmount()).isEqualTo(1);
        assertThat(response.getOffset()).isEqualTo(0);
        assertThat(response.getLimit()).isEqualTo(50);
        assertThat(response.getSortKeys()).containsExactly("name");
        assertThat(response.getElements()).containsExactly(user);
    }

    @Test
    void getCustomerUserDetailShouldReturnUserAndAttributes()
    {
        CustomerUserDTO user = new CustomerUserDTO();
        user.setBusinessPartnerNo("BP100");

        when(customerRepository.findUserByCustomerIdAndBusinessPartnerNo("C100", "BP100"))
                .thenReturn(user);

        when(customerRepository.findUserAttributesByCustomerIdAndBusinessPartnerNo("C100", "BP100"))
                .thenReturn(List.of());

        CustomerUserDetailResponse response = customerService.getCustomerUserDetail(
                "C100",
                "BP100:domain"
        );

        assertThat(response.getUser()).isSameAs(user);
        assertThat(response.getAttributes()).isEmpty();
    }

    @Test
    void getCustomerUserDetailShouldThrowNotFound()
    {
        when(customerRepository.findUserByCustomerIdAndBusinessPartnerNo("C100", "BP100"))
                .thenReturn(null);

        assertThatThrownBy(() -> customerService.getCustomerUserDetail("C100", "BP100"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void addCustomerToUserCustomerListShouldAppendCustomerNo()
    {
        CustomerAttributeRequest request = new CustomerAttributeRequest();
        request.setValue("C200");

        CustomerUserDTO user = new CustomerUserDTO();
        user.setBusinessPartnerNo("BP100");

        when(customerRepository.findUserByCustomerIdAndBusinessPartnerNo("C100", "BP100"))
                .thenReturn(user);

        when(customerRepository.findUserAttributeValue("BP100", "CustomerList"))
                .thenReturn("C100");

        customerService.addCustomerToUserCustomerList("C100", "BP100:domain", request);

        verify(customerRepository).saveUserAttribute(
                "BP100",
                "CustomerList",
                "C100\tC200"
        );
    }

    @Test
    void addCustomerToUserCustomerListShouldNotDuplicateCustomerNo()
    {
        CustomerAttributeRequest request = new CustomerAttributeRequest();
        request.setValue("C100");

        CustomerUserDTO user = new CustomerUserDTO();
        user.setBusinessPartnerNo("BP100");

        when(customerRepository.findUserByCustomerIdAndBusinessPartnerNo("C100", "BP100"))
                .thenReturn(user);

        when(customerRepository.findUserAttributeValue("BP100", "CustomerList"))
                .thenReturn("C100");

        customerService.addCustomerToUserCustomerList("C100", "BP100", request);

        verify(customerRepository).saveUserAttribute(
                "BP100",
                "CustomerList",
                "C100"
        );
    }

    @Test
    void removeCustomerFromUserCustomerListShouldRemoveCustomerNoAndAudit()
    {
        when(customerRepository.findUserAttributeValue("BP100", "CustomerList"))
                .thenReturn("C100\tC200\tC300");

        customerService.removeCustomerFromUserCustomerList(
                "C100",
                "BP100:domain",
                "C200"
        );

        verify(customerRepository).saveUserAttribute(
                "BP100",
                "CustomerList",
                "C100\tC300"
        );

        verify(auditLogService).logChange(
                eq("USER_CUSTOMERLIST"),
                eq("BP100"),
                eq("DELETE"),
                eq("CustomerList"),
                eq("C200"),
                eq("C100\tC300"),
                eq("system"),
                eq("Customer removed from user CustomerList")
        );
    }

    @Test
    void addCustomerUserAttributeShouldSaveAndAudit()
    {
        CustomerAttributeRequest request = new CustomerAttributeRequest();
        request.setName("Role");
        request.setValue("Buyer");

        CustomerUserDTO user = new CustomerUserDTO();

        when(customerRepository.findUserByCustomerIdAndBusinessPartnerNo("C100", "BP100"))
                .thenReturn(user);

        customerService.addCustomerUserAttribute(
                "token",
                "C100",
                "BP100:domain",
                request
        );

        verify(customerRepository).saveUserAttribute("BP100", "Role", "Buyer");
        verify(auditLogService).logChange(
                eq("USER_ATTRIBUTE"),
                eq("BP100"),
                eq("CREATE"),
                eq("Role"),
                eq(""),
                eq("Buyer"),
                eq("system"),
                eq("User attribute added")
        );
    }

    @Test
    void updateCustomerUserAttributeShouldSaveAndAudit()
    {
        CustomerAttributeRequest request = new CustomerAttributeRequest();
        request.setValue("Admin");

        CustomerUserDTO user = new CustomerUserDTO();

        when(customerRepository.findUserByCustomerIdAndBusinessPartnerNo("C100", "BP100"))
                .thenReturn(user);

        customerService.updateCustomerUserAttribute(
                "token",
                "C100",
                "BP100",
                "Role",
                request
        );

        verify(customerRepository).saveUserAttribute("BP100", "Role", "Admin");
        verify(auditLogService).logChange(
                eq("USER_ATTRIBUTE"),
                eq("BP100"),
                eq("UPDATE"),
                eq("Role"),
                eq(""),
                eq("Admin"),
                eq("system"),
                eq("User attribute updated")
        );
    }

    @Test
    void deleteCustomerUserAttributeShouldDeleteAndAudit()
    {
        CustomerUserDTO user = new CustomerUserDTO();

        when(customerRepository.findUserByCustomerIdAndBusinessPartnerNo("C100", "BP100"))
                .thenReturn(user);

        customerService.deleteCustomerUserAttribute(
                "token",
                "C100",
                "BP100",
                "Role"
        );

        verify(customerRepository).deleteUserAttribute("BP100", "Role");
        verify(auditLogService).logChange(
                eq("USER_ATTRIBUTE"),
                eq("BP100"),
                eq("DELETE"),
                eq("Role"),
                eq(""),
                eq(""),
                eq("system"),
                eq("User attribute deleted")
        );
    }

    @Test
    void addSubCustomerToClusterShouldUpdateCustomerListAuditAndIntershop()
    {
        Customer cluster = customer("1", "CL100", "ClusterCustomer", "Cluster", true);
        cluster.setCustomerList("C100");

        Customer subCustomer = customer("2", "C200", "StandardCustomer", "Sub", true);

        when(customerRepository.findCustomersByCustomerNos("domain", List.of("CL100")))
                .thenReturn(List.of(cluster));

        when(customerRepository.findCustomersByCustomerNos("domain", List.of("C200")))
                .thenReturn(List.of(subCustomer));

        customerService.addSubCustomerToCluster(
                "token",
                "domain",
                "CL100",
                "C200"
        );

        verify(customerRepository).saveCustomerAttribute(
                "CL100",
                "CustomerList",
                "C100\tC200"
        );

        verify(auditLogService).logChange(
                eq("CUSTOMER_RELATION"),
                eq("CL100"),
                eq("ADD"),
                eq("CustomerList"),
                eq(""),
                eq("C200"),
                eq("system"),
                eq("Sub customer added to cluster")
        );

        verify(intershopClient).updateCustomerAttribute(
                "token",
                "CL100",
                "CustomerList",
                "C100\tC200"
        );
    }

    @Test
    void addSubCustomerToClusterShouldThrowWhenTargetIsNotCluster()
    {
        Customer notCluster = customer("1", "C100", "StandardCustomer", "Customer", true);

        when(customerRepository.findCustomersByCustomerNos("domain", List.of("C100")))
                .thenReturn(List.of(notCluster));

        assertThatThrownBy(() -> customerService.addSubCustomerToCluster(
                "token",
                "domain",
                "C100",
                "C200"
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void addSubCustomerToClusterShouldThrowWhenSubCustomerIsCluster()
    {
        Customer cluster = customer("1", "CL100", "ClusterCustomer", "Cluster", true);
        Customer subCluster = customer("2", "CL200", "ClusterCustomer", "Sub cluster", true);

        when(customerRepository.findCustomersByCustomerNos("domain", List.of("CL100")))
                .thenReturn(List.of(cluster));

        when(customerRepository.findCustomersByCustomerNos("domain", List.of("CL200")))
                .thenReturn(List.of(subCluster));

        assertThatThrownBy(() -> customerService.addSubCustomerToCluster(
                "token",
                "domain",
                "CL100",
                "CL200"
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void removeSubCustomerFromClusterShouldUpdateCustomerListAuditAndIntershop()
    {
        Customer cluster = customer("1", "CL100", "ClusterCustomer", "Cluster", true);
        cluster.setCustomerList("C100\tC200\tC300");

        when(customerRepository.findCustomersByCustomerNos("domain", List.of("CL100")))
                .thenReturn(List.of(cluster));

        customerService.removeSubCustomerFromCluster(
                "token",
                "domain",
                "CL100",
                "C200"
        );

        verify(customerRepository).saveCustomerAttribute(
                "CL100",
                "CustomerList",
                "C100\tC300"
        );

        verify(auditLogService).logChange(
                eq("CUSTOMER_RELATION"),
                eq("CL100"),
                eq("DELETE"),
                eq("CustomerList"),
                eq("C200"),
                eq("C100\tC300"),
                eq("system"),
                eq("Sub customer removed from cluster")
        );

        verify(intershopClient).updateCustomerAttribute(
                "token",
                "CL100",
                "CustomerList",
                "C100\tC300"
        );
    }

    @Test
    void getSegmentsShouldMergeIntershopLocalAndAssignedSegments()
    {
        CustomerSegmentDTO intershopSegment = segment("cgVip", "VIP", "VIP customers");
        CustomerSegmentDTO localSegment = segment("local", "Local", "Local segment");

        CustomerSegmentAssignment assignment1 = assignment("C100", "cgVip");
        CustomerSegmentAssignment assignment2 = assignment("C200", "cgVip");
        CustomerSegmentAssignment assignment3 = assignment("C300", "unknown");

        when(intershopClient.getAllCustomerSegments("token"))
                .thenReturn(List.of(intershopSegment));

        when(customerRepository.findLocalSegments())
                .thenReturn(List.of(localSegment));

        when(customerRepository.findCustomerSegmentAssignmentsByDomain("domain"))
                .thenReturn(List.of(assignment1, assignment2, assignment3));

        List<CustomerSegmentSummaryDTO> response = customerService.getSegments(
                "token",
                "domain"
        );

        assertThat(response)
                .extracting(CustomerSegmentSummaryDTO::getId)
                .containsExactly("cgVip", "local", "unknown");

        CustomerSegmentSummaryDTO vip = response.stream()
                .filter(item -> "cgVip".equals(item.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(vip.getName()).isEqualTo("VIP");
        assertThat(vip.getCustomerCount()).isEqualTo(2);

        CustomerSegmentSummaryDTO unknown = response.stream()
                .filter(item -> "unknown".equals(item.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(unknown.getName()).isEqualTo("unknown");
        assertThat(unknown.getCustomerCount()).isEqualTo(1);
    }

    @Test
    void assignSegmentToCustomerShouldAssignAndAudit()
    {
        CustomerSegmentRequest request = new CustomerSegmentRequest();
        request.setId("cgVip");

        CustomerDetailResponse customer = new CustomerDetailResponse();
        customer.setCustomerNo("C100");

        when(customerRepository.findCustomerDetailById("C100"))
                .thenReturn(customer);

        customerService.assignSegmentToCustomer(
                "token",
                "domain",
                "C100",
                request
        );

        verify(customerRepository).assignSegmentToCustomer("domain", "C100", "cgVip");
        verify(auditLogService).logChange(
                eq("CUSTOMER_SEGMENT"),
                eq("C100"),
                eq("ASSIGN"),
                eq("segment"),
                eq(""),
                eq("cgVip"),
                eq("system"),
                eq("Segment assigned to customer")
        );
    }

    @Test
    void assignSegmentToCustomerShouldThrowNotFoundWhenCustomerMissing()
    {
        CustomerSegmentRequest request = new CustomerSegmentRequest();
        request.setId("cgVip");

        when(customerRepository.findCustomerDetailById("C100"))
                .thenReturn(null);

        assertThatThrownBy(() -> customerService.assignSegmentToCustomer(
                "token",
                "domain",
                "C100",
                request
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void removeSegmentFromCustomerShouldRemoveAndAudit()
    {
        CustomerDetailResponse customer = new CustomerDetailResponse();
        customer.setCustomerNo("C100");

        when(customerRepository.findCustomerDetailById("C100"))
                .thenReturn(customer);

        customerService.removeSegmentFromCustomer(
                "token",
                "domain",
                "C100",
                "cgVip"
        );

        verify(customerRepository).removeSegmentFromCustomer("C100", "cgVip");
        verify(auditLogService).logChange(
                eq("CUSTOMER_SEGMENT"),
                eq("C100"),
                eq("REMOVE"),
                eq("segment"),
                eq("cgVip"),
                eq(""),
                eq("system"),
                eq("Segment removed from customer")
        );
    }

    @Test
    void createSegmentShouldSaveLocalSegment()
    {
        CustomerSegmentRequest request = new CustomerSegmentRequest();
        request.setId("local");
        request.setName("Local segment");
        request.setDescription("Description");

        customerService.createSegment("token", "domain", request);

        verify(customerRepository).saveLocalSegment(
                "local",
                "Local segment",
                "Description"
        );
    }

    @Test
    void deleteSegmentShouldDeleteLocalSegment()
    {
        customerService.deleteSegment("token", "domain", "local");

        verify(customerRepository).deleteLocalSegment("local");
    }

    private Customer customer(
            String id,
            String customerNo,
            String type,
            String displayName,
            boolean active)
    {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setCustomerNo(customerNo);
        customer.setType(type);
        customer.setDisplayName(displayName);
        customer.setCompanyName(displayName);
        customer.setEmail(customerNo.toLowerCase() + "@example.com");
        customer.setActive(active);
        return customer;
    }

    private CustomerSegmentDTO segment(String id, String name, String description)
    {
        CustomerSegmentDTO dto = new CustomerSegmentDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setDescription(description);
        return dto;
    }

    private CustomerSegmentAssignment assignment(String customerNo, String segmentId)
    {
        CustomerSegmentAssignment assignment = new CustomerSegmentAssignment();
        assignment.setCustomerNo(customerNo);
        assignment.setSegmentId(segmentId);
        return assignment;
    }
}