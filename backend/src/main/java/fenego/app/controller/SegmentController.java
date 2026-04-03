package fenego.app.controller;

import fenego.app.dto.CreateSegmentRequest;
import fenego.app.dto.SegmentDTO;
import fenego.app.dto.SegmentLogItemDTO;
import fenego.app.dto.UpdateSegmentRuleRequest;
import fenego.app.service.SegmentService;
import fenego.app.service.SegmentSyncService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/segments")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class SegmentController
{
    private final SegmentService segmentService;
    private final SegmentSyncService segmentSyncService;

    public SegmentController(SegmentService segmentService, SegmentSyncService segmentSyncService)
    {
        this.segmentService = segmentService;
        this.segmentSyncService = segmentSyncService;
    }

    @GetMapping
    public List<SegmentDTO> getSegments()
    {
        return segmentService.getSegments();
    }

    @GetMapping("/log")
    public List<SegmentLogItemDTO> getLogs()
    {
        return segmentService.getLogs();
    }

    @PostMapping("/sync")
    public Map<String, Object> syncSegments()
    {
        int importedCount = segmentSyncService.syncSegmentsFromIntershop();

        return Map.of(
                "success", true,
                "importedCount", importedCount
        );
    }

    @PutMapping("/{id}/rule")
    public Map<String, Object> updateSegmentRule(@PathVariable String id, @RequestBody UpdateSegmentRuleRequest request)
    {
        segmentService.updateSegmentRule(id, request.getRule());

        return Map.of(
                "success", true,
                "message", "Segment rule updated successfully"
        );
    }
    @PostMapping
public Map<String, Object> createSegment(@RequestBody CreateSegmentRequest request)
{
    String id = segmentService.createSegment(
            request.getName(),
            request.getDescription(),
            request.getRule()
    );

    return Map.of(
            "success", true,
            "id", id
    );
}
}