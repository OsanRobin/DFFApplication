package fenego.app.controller;

import fenego.app.dto.AttributeOptionDTO;
import fenego.app.dto.BulkActionRequest;
import fenego.app.dto.BulkActionResponse;
import fenego.app.dto.SegmentOptionDTO;
import fenego.app.service.BulkActionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bulk-actions")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class BulkActionController
{
    private final BulkActionService bulkActionService;

    public BulkActionController(BulkActionService bulkActionService)
    {
        this.bulkActionService = bulkActionService;
    }

    @GetMapping("/attributes")
    public List<AttributeOptionDTO> getAvailableAttributes()
    {
        return bulkActionService.getAvailableAttributes();
    }

    @GetMapping("/segments")
    public List<SegmentOptionDTO> getAvailableSegments()
    {
        return bulkActionService.getAvailableSegments();
    }

    @PostMapping
    public BulkActionResponse executeBulkAction(@RequestBody BulkActionRequest request)
    {
        return bulkActionService.executeBulkAction(request);
    }
}