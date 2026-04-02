package fenego.app.service;

import fenego.app.dto.SegmentDTO;
import fenego.app.dto.SegmentLogItemDTO;
import fenego.app.repository.SegmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SegmentService
{
    private final SegmentRepository segmentRepository;

    public SegmentService(SegmentRepository segmentRepository)
    {
        this.segmentRepository = segmentRepository;
    }

    public List<SegmentDTO> getSegments()
    {
        return segmentRepository.findAll();
    }

    public List<SegmentLogItemDTO> getLogs()
    {
        return segmentRepository.findAllLogs();
    }
}