import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { HeaderComponent } from '../../shell/header.component/header.component';
type AttrType = 'Boolean' | 'Dropdown' | 'Text' | 'Number' | 'Date';

type AttributeRow = {
  name: string;
  type: AttrType;
  usageCount: number;
  uniqueValues: number;
  linkedSegments: number;
};


@Component({
  selector: 'app-attributeoverview.component',
  imports: [CommonModule, HeaderComponent],
  templateUrl: './attributeoverview.component.html',
  styleUrl: './attributeoverview.component.css',
})
export class AttributeoverviewComponent {
  rows: AttributeRow[] = [
    { name: 'Order Permission', type: 'Boolean', usageCount: 12, uniqueValues: 2, linkedSegments: 3 },
    { name: 'Customer Type', type: 'Dropdown', usageCount: 12, uniqueValues: 2, linkedSegments: 5 },
    { name: 'Debtor Number', type: 'Text', usageCount: 12, uniqueValues: 12, linkedSegments: 0 },
    { name: 'Custom Segment Attribute', type: 'Text', usageCount: 12, uniqueValues: 3, linkedSegments: 4 },
    { name: 'Annual Revenue', type: 'Number', usageCount: 8, uniqueValues: 8, linkedSegments: 2 },
    { name: 'Contract Start Date', type: 'Date', usageCount: 10, uniqueValues: 10, linkedSegments: 1 },
  ];

  badgeClass(t: AttrType) {
    switch (t) {
      case 'Boolean':
        return 'badge boolean';
      case 'Dropdown':
        return 'badge dropdown';
      case 'Text':
        return 'badge text';
      case 'Number':
        return 'badge number';
      case 'Date':
        return 'badge date';
      default:
        return 'badge';
    }
  }

  onCreate() {}

  onViewDetails(row: AttributeRow) {}

}
