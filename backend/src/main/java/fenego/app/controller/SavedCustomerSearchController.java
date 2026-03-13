package fenego.app.controller;

import fenego.app.dto.SaveCustomerSearchRequest;
import fenego.app.dto.SavedCustomerSearchListResponse;
import fenego.app.service.SavedCustomerSearchService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer-searches")
public class SavedCustomerSearchController
{
    private final SavedCustomerSearchService savedCustomerSearchService;

    public SavedCustomerSearchController(SavedCustomerSearchService savedCustomerSearchService)
    {
        this.savedCustomerSearchService = savedCustomerSearchService;
    }

    @GetMapping
    public SavedCustomerSearchListResponse getSavedSearches(@RequestParam String domain)
    {
        return savedCustomerSearchService.getSavedSearches(domain);
    }

    @PostMapping
    public void saveSearch(@RequestBody SaveCustomerSearchRequest request)
    {
        savedCustomerSearchService.saveSearch(request);
    }
    @DeleteMapping("/{id}")
public void deleteSearch(@PathVariable Long id)
{
    savedCustomerSearchService.deleteSearch(id);
}

@PutMapping("/{id}/name")
public void updateSearchName(
        @PathVariable Long id,
        @RequestBody String name
)
{
    savedCustomerSearchService.updateSearchName(id, name);
}
}