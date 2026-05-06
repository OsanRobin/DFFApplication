package fenego.app;

import fenego.app.dto.SaveCustomerSearchRequest;
import fenego.app.dto.SavedCustomerSearchListResponse;
import fenego.app.jpa.SavedCustomerSearch;
import fenego.app.repository.SavedCustomerSearchRepository;
import fenego.app.service.AuditLogService;
import fenego.app.service.SavedCustomerSearchService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavedCustomerSearchServiceTest
{
    @Mock
    private SavedCustomerSearchRepository repository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private SavedCustomerSearchService service;

    private SaveCustomerSearchRequest request;

    @BeforeEach
    void setUp()
    {
        request = new SaveCustomerSearchRequest();
        request.setDomainName("test-domain");
        request.setName("My Search");
    }

    @Test
void getSavedSearches_shouldReturnDataAndCount()
{
    List<SavedCustomerSearch> searches = List.of(
            new SavedCustomerSearch(),
            new SavedCustomerSearch()
    );

    when(repository.findByDomain("test-domain")).thenReturn(searches);

    SavedCustomerSearchListResponse response = service.getSavedSearches("test-domain");

    assertNotNull(response);
    assertEquals(searches, response.getData());
    assertEquals(2, response.getCount());

    verify(repository).findByDomain("test-domain");
}

    @Test
    void saveSearch_shouldThrowException_whenDomainNameIsNull()
    {
        request.setDomainName(null);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.saveSearch(request)
        );

        assertEquals("Domain name is required", exception.getMessage());

        verifyNoInteractions(repository);
        verifyNoInteractions(auditLogService);
    }

    @Test
    void saveSearch_shouldThrowException_whenDomainNameIsBlank()
    {
        request.setDomainName("   ");

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.saveSearch(request)
        );

        assertEquals("Domain name is required", exception.getMessage());

        verifyNoInteractions(repository);
        verifyNoInteractions(auditLogService);
    }

    @Test
    void saveSearch_shouldThrowException_whenNameIsNull()
    {
        request.setName(null);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.saveSearch(request)
        );

        assertEquals("Search name is required", exception.getMessage());

        verifyNoInteractions(repository);
        verifyNoInteractions(auditLogService);
    }

    @Test
    void saveSearch_shouldThrowException_whenNameIsBlank()
    {
        request.setName("   ");

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.saveSearch(request)
        );

        assertEquals("Search name is required", exception.getMessage());

        verifyNoInteractions(repository);
        verifyNoInteractions(auditLogService);
    }

    @Test
    void saveSearch_shouldThrowException_whenSearchExistsAndOverwriteIsFalse()
    {
        request.setOverwrite(false);

        when(repository.existsByDomainAndName("test-domain", "My Search"))
                .thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.saveSearch(request)
        );

        assertEquals("A saved search with this name already exists", exception.getMessage());

        verify(repository).existsByDomainAndName("test-domain", "My Search");
        verify(repository, never()).insert(any());
        verify(repository, never()).update(any());
        verifyNoInteractions(auditLogService);
    }

    @Test
    void saveSearch_shouldUpdateAndAuditLog_whenSearchExistsAndOverwriteIsTrue()
    {
        request.setOverwrite(true);

        when(repository.existsByDomainAndName("test-domain", "My Search"))
                .thenReturn(true);

        service.saveSearch(request);

        verify(repository).existsByDomainAndName("test-domain", "My Search");
        verify(repository).update(request);
        verify(repository, never()).insert(any());

        verify(auditLogService).logChange(
                "SAVED_CUSTOMER_SEARCH",
                "My Search",
                "UPDATE",
                "search",
                "",
                "My Search",
                "system",
                "Saved customer search updated"
        );
    }

    @Test
    void saveSearch_shouldInsertAndAuditLog_whenSearchDoesNotExist()
    {
        when(repository.existsByDomainAndName("test-domain", "My Search"))
                .thenReturn(false);

        service.saveSearch(request);

        verify(repository).existsByDomainAndName("test-domain", "My Search");
        verify(repository).insert(request);
        verify(repository, never()).update(any());

        verify(auditLogService).logChange(
                "SAVED_CUSTOMER_SEARCH",
                "My Search",
                "CREATE",
                "search",
                "",
                "My Search",
                "system",
                "Saved customer search created"
        );
    }

    @Test
    void deleteSearch_shouldDeleteByIdAndAuditLog()
    {
        service.deleteSearch(10L);

        verify(repository).deleteById(10L);

        verify(auditLogService).logChange(
                "SAVED_CUSTOMER_SEARCH",
                "10",
                "DELETE",
                "search",
                "",
                "",
                "system",
                "Saved customer search deleted"
        );
    }

    @Test
    void updateSearchName_shouldThrowException_whenNameIsNull()
    {
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.updateSearchName(10L, null)
        );

        assertEquals("Search name is required", exception.getMessage());

        verifyNoInteractions(repository);
        verifyNoInteractions(auditLogService);
    }

    @Test
    void updateSearchName_shouldThrowException_whenNameIsBlank()
    {
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.updateSearchName(10L, "   ")
        );

        assertEquals("Search name is required", exception.getMessage());

        verifyNoInteractions(repository);
        verifyNoInteractions(auditLogService);
    }

    @Test
    void updateSearchName_shouldUpdateNameAndAuditLog()
    {
        service.updateSearchName(10L, "Updated Search");

        verify(repository).updateName(10L, "Updated Search");

        verify(auditLogService).logChange(
                "SAVED_CUSTOMER_SEARCH",
                "10",
                "UPDATE",
                "name",
                "",
                "Updated Search",
                "system",
                "Saved customer search name updated"
        );
    }
}