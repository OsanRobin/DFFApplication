import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

type Status = 'Active' | 'Inactive' | 'Pending';

type CustomerRow = {
  id: string;
  name: string;
  type: 'Cluster' | 'Subcustomer';
  segment: 'Enterprise' | 'Premium' | 'Standard' | 'Basic';
  status: Status;
  locations: number;
  level: 0 | 1;       
  parentId?: string;   
};

@Component({
  selector: 'app-customeroverview.component',
  imports: [CommonModule, FormsModule],
  templateUrl: './customeroverview.component.html',
  styleUrl: './customeroverview.component.css',
})
export class CustomeroverviewComponent {
   query = '';
  typeFilter = '';
  segmentFilter = '';
  statusFilter = '';

  bulkOpen = false;
  selectedIds = new Set<string>();

  rows: CustomerRow[] = [
    { id: 'c1', name: "St. Mary's Hospital Network", type: 'Cluster', segment: 'Enterprise', status: 'Active', locations: 12, level: 0 },
    { id: 'c1-1', name: "St. Mary's Hospital - Downtown", type: 'Subcustomer', segment: 'Premium', status: 'Active', locations: 3, level: 1, parentId: 'c1' },
    { id: 'c1-2', name: "St. Mary's Hospital - North Branch", type: 'Subcustomer', segment: 'Premium', status: 'Active', locations: 2, level: 1, parentId: 'c1' },

    { id: 'c2', name: 'Sunshine Care Homes Group', type: 'Cluster', segment: 'Premium', status: 'Active', locations: 8, level: 0 },
    { id: 'c2-1', name: 'Sunshine Care - Riverside', type: 'Subcustomer', segment: 'Standard', status: 'Active', locations: 1, level: 1, parentId: 'c2' },

    { id: 'c3', name: 'Green Valley Medical Center', type: 'Cluster', segment: 'Enterprise', status: 'Active', locations: 15, level: 0 },
    { id: 'c4', name: 'Community Health Clinic', type: 'Cluster', segment: 'Basic', status: 'Inactive', locations: 2, level: 0 },
    { id: 'c5', name: 'Royal Care Nursing Homes', type: 'Cluster', segment: 'Premium', status: 'Pending', locations: 6, level: 0 },
    { id: 'c6', name: 'Metro Hospital Group', type: 'Cluster', segment: 'Enterprise', status: 'Active', locations: 20, level: 0 },
  ];

  isSelected(id: string) {
    return this.selectedIds.has(id);
  }

  toggleRow(id: string) {
    if (this.selectedIds.has(id)) this.selectedIds.delete(id);
    else this.selectedIds.add(id);

  }

  get allSelected() {
    const ids = this.visibleRows().map(r => r.id);
    return ids.length > 0 && ids.every(id => this.selectedIds.has(id));
  }

  toggleAll() {
    const ids = this.visibleRows().map(r => r.id);
    if (ids.length === 0) return;

    if (ids.every(id => this.selectedIds.has(id))) {
      ids.forEach(id => this.selectedIds.delete(id));
    } else {
      ids.forEach(id => this.selectedIds.add(id));
    }
  }

  clearSelection() {
    this.selectedIds.clear();
    this.bulkOpen = false;
  }

  visibleRows(): CustomerRow[] {

    const q = this.query.trim().toLowerCase();

    return this.rows.filter(r => {
      const matchesQuery = !q || r.name.toLowerCase().includes(q);
      const matchesType = !this.typeFilter || r.type === this.typeFilter;
      const matchesSeg = !this.segmentFilter || r.segment === this.segmentFilter;
      const matchesStatus = !this.statusFilter || r.status === this.statusFilter;
      return matchesQuery && matchesType && matchesSeg && matchesStatus;
    });
  }

}
