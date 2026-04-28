package fenego.app.service;

import fenego.app.dto.SaveCustomerSearchRequest;
import fenego.app.dto.SavedCustomerSearchListResponse;
import fenego.app.repository.SavedCustomerSearchRepository;
import org.springframework.stereotype.Service;

@Service
public class SavedCustomerSearchService
{
    private final SavedCustomerSearchRepository repository;
    private final AuditLogService auditLogService;

    public SavedCustomerSearchService(
            SavedCustomerSearchRepository repository,
            AuditLogService auditLogService)
    {
        this.repository = repository;
        this.auditLogService = auditLogService;
    }

    public SavedCustomerSearchListResponse getSavedSearches(String domainName)
    {
        SavedCustomerSearchListResponse response = new SavedCustomerSearchListResponse();
        response.setData(repository.findByDomain(domainName));
        response.setCount(response.getData().size());
        return response;
    }

    public void saveSearch(SaveCustomerSearchRequest request)
    {
        if (request.getDomainName() == null || request.getDomainName().isBlank())
        {
            throw new RuntimeException("Domain name is required");
        }

        if (request.getName() == null || request.getName().isBlank())
        {
            throw new RuntimeException("Search name is required");
        }

        boolean exists = repository.existsByDomainAndName(request.getDomainName(), request.getName());

        if (exists)
        {
            if (!request.isOverwrite())
            {
                throw new RuntimeException("A saved search with this name already exists");
            }

            repository.update(request);

            auditLogService.logChange(
                    "SAVED_CUSTOMER_SEARCH",
                    request.getName(),
                    "UPDATE",
                    "search",
                    "",
                    request.getName(),
                    "system",
                    "Saved customer search updated"
            );

            return;
        }

        repository.insert(request);

        auditLogService.logChange(
                "SAVED_CUSTOMER_SEARCH",
                request.getName(),
                "CREATE",
                "search",
                "",
                request.getName(),
                "system",
                "Saved customer search created"
        );
    }

    public void deleteSearch(Long id)
    {
        repository.deleteById(id);

        auditLogService.logChange(
                "SAVED_CUSTOMER_SEARCH",
                String.valueOf(id),
                "DELETE",
                "search",
                "",
                "",
                "system",
                "Saved customer search deleted"
        );
    }

    public void updateSearchName(Long id, String name)
    {
        if (name == null || name.isBlank())
        {
            throw new RuntimeException("Search name is required");
        }

        repository.updateName(id, name);

        auditLogService.logChange(
                "SAVED_CUSTOMER_SEARCH",
                String.valueOf(id),
                "UPDATE",
                "name",
                "",
                name,
                "system",
                "Saved customer search name updated"
        );
    }
}