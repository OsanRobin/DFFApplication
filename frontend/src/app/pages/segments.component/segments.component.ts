import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { HeaderComponent } from '../../shell/header.component/header.component';
type SegmentCard = {
  name: string;
  rule: string;
  matchedCustomers: number;
  lastUpdated: string;
};

type LogItem = {
  id: string;
  direction: 'up' | 'down';
  message: string;
  timestamp: string;
};
@Component({
  selector: 'app-segments.component',
  imports: [CommonModule, HeaderComponent],
  templateUrl: './segments.component.html',
  styleUrl: './segments.component.css',
})
export class SegmentsComponent {
  segments: SegmentCard[] = [
    {
      name: 'Enterprise Healthcare',
      rule: 'Segment = "Enterprise" AND Custom Segment Attribute = "Healthcare Provider"',
      matchedCustomers: 3,
      lastUpdated: '2026-02-19 14:30:00'
    },
    {
      name: 'Premium Care Homes',
      rule: 'Segment = "Premium" AND Custom Segment Attribute = "Elderly Care"',
      matchedCustomers: 2,
      lastUpdated: '2026-02-19 12:15:00'
    },
    {
      name: 'Active Order Customers',
      rule: 'Status = "Active" AND Order Permission = "Yes"',
      matchedCustomers: 7,
      lastUpdated: '2026-02-20 08:00:00'
    }
  ];

  logItems: LogItem[] = [
    {
      id: '1',
      direction: 'up',
      message: `St. Mary's Hospital Network added to Enterprise Healthcare segment`,
      timestamp: '2026-02-20 09:30:15'
    },
    {
      id: '2',
      direction: 'down',
      message: 'Community Health Clinic removed from Active Order Customers segment',
      timestamp: '2026-02-20 09:15:42'
    },
    {
      id: '3',
      direction: 'up',
      message: 'Metro Hospital Group added to Enterprise Healthcare segment',
      timestamp: '2026-02-20 08:45:23'
    },
    {
      id: '4',
      direction: 'up',
      message: 'Royal Care Nursing Homes added to Premium Care Homes segment',
      timestamp: '2026-02-19 16:30:12'
    },
    {
      id: '5',
      direction: 'down',
      message: 'Oakwood Senior Living removed from Premium Care Homes segment',
      timestamp: '2026-02-19 14:20:33'
    }
  ];

  onCreateSegment(): void {}

  onViewCustomers(_segment: SegmentCard): void {}

  onEditRules(_segment: SegmentCard): void {}

}
