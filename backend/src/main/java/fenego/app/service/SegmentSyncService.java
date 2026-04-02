package fenego.app.service;

import fenego.app.dto.CustomerSegmentItemDTO;
import fenego.app.dto.CustomerSegmentListResponse;
import fenego.app.intershop.IntershopClient;
import fenego.app.repository.SegmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SegmentSyncService
{
    private final IntershopClient intershopClient;
    private final SegmentRepository segmentRepository;

    @Value("${intershop.username}")
    private String intershopUsername;

    @Value("${intershop.password}")
    private String intershopPassword;

    public SegmentSyncService(IntershopClient intershopClient, SegmentRepository segmentRepository)
    {
        this.intershopClient = intershopClient;
        this.segmentRepository = segmentRepository;
    }

    public int syncSegmentsFromIntershop()
    {
        CustomerSegmentListResponse response = intershopClient.getCustomerSegments(intershopUsername, intershopPassword);

        if (response == null || response.getData() == null)
        {
            return 0;
        }

        int count = 0;

        for (CustomerSegmentItemDTO item : response.getData())
        {
            String id = item.getId();
            String name = id;
            String description = null;

            if (item.getData() != null)
            {
                if (item.getData().getName() != null && !item.getData().getName().isBlank())
                {
                    name = item.getData().getName();
                }

                description = item.getData().getDescription();
            }

            segmentRepository.upsertSegmentFromIntershop(id, name, description);
            count++;
        }

        segmentRepository.insertSyncLog("up", "Intershop segment sync completed: " + count + " segment(s)");

        return count;
    }
}