package fenego.app.service;

import fenego.app.dto.SegmentLogItemDTO;
import fenego.app.jpa.Segment;
import fenego.app.repository.SegmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SegmentService
{
    private final SegmentRepository segmentRepository;

    public SegmentService(SegmentRepository segmentRepository)
    {
        this.segmentRepository = segmentRepository;
    }

    public List<Segment> getSegments()
    {
        return segmentRepository.findAll();
    }

    public List<SegmentLogItemDTO> getLogs()
    {
        return segmentRepository.findAllLogs();
    }

    public void updateSegmentRule(String segmentId, String rule)
    {
        segmentRepository.updateRuleExpression(segmentId, rule);
        segmentRepository.insertSegmentLog(segmentId, "down", "Rule updated for segment " + segmentId);
    }

    public String createSegment(String name, String description, String rule)
    {
        String id = UUID.randomUUID().toString();

        segmentRepository.insertSegment(id, name, description, rule);
        segmentRepository.insertSegmentLog(id, "down", "Segment created: " + name);

        return id;
    }
}