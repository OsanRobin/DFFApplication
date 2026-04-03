import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HeaderComponent } from '../../shell/header.component/header.component';
import { SegmentsApiService, SegmentCard, LogItem } from '../../core/api/segments-api.service';

@Component({
  selector: 'app-segments.component',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderComponent],
  templateUrl: './segments.component.html',
  styleUrl: './segments.component.css',
})
export class SegmentsComponent implements OnInit {
  private segmentsApi = inject(SegmentsApiService);

  segments: SegmentCard[] = [];
  logItems: LogItem[] = [];

  loadingSegments = false;
  loadingLogs = false;
  syncing = false;
  errorMessage = '';

  editingSegmentId: string | null = null;
  editedRule = '';
  savingRule = false;

  ngOnInit(): void {
    this.loadSegments();
    this.loadLogs();
  }

  loadSegments(): void {
    this.loadingSegments = true;
    this.errorMessage = '';

    this.segmentsApi.getSegments().subscribe({
      next: (segments) => {
        this.segments = segments;
        this.loadingSegments = false;

        if (segments.length === 0) {
          this.syncFromIntershop();
        }
      },
      error: (error) => {
        console.error(error);
        this.errorMessage = 'Failed to load segments.';
        this.loadingSegments = false;
      }
    });
  }

  loadLogs(): void {
    this.loadingLogs = true;

    this.segmentsApi.getLogItems().subscribe({
      next: (logs) => {
        this.logItems = logs;
        this.loadingLogs = false;
      },
      error: (error) => {
        console.error(error);
        this.loadingLogs = false;
      }
    });
  }

  syncFromIntershop(): void {
    this.syncing = true;
    this.errorMessage = '';

    this.segmentsApi.syncSegments().subscribe({
      next: () => {
        this.syncing = false;
        this.loadSegments();
        this.loadLogs();
      },
      error: (error) => {
        console.error(error);
        this.errorMessage = 'Failed to sync segments from Intershop.';
        this.syncing = false;
      }
    });
  }

  startEditRules(segment: SegmentCard): void {
    this.editingSegmentId = segment.id;
    this.editedRule = segment.rule || '';
  }

  cancelEditRules(): void {
    this.editingSegmentId = null;
    this.editedRule = '';
  }

  saveRules(segment: SegmentCard): void {
    this.savingRule = true;
    this.errorMessage = '';

    this.segmentsApi.updateSegmentRule(segment.id, this.editedRule).subscribe({
      next: () => {
        segment.rule = this.editedRule;
        segment.autoUpdated = false;
        this.savingRule = false;
        this.editingSegmentId = null;
        this.editedRule = '';
        this.loadSegments();
        this.loadLogs();
      },
      error: (error) => {
        console.error(error);
        this.errorMessage = 'Failed to update segment rule.';
        this.savingRule = false;
      }
    });
  }

  onCreateSegment(): void {}

  onViewCustomers(_segment: SegmentCard): void {}

  onEditRules(segment: SegmentCard): void {
    this.startEditRules(segment);
  }
}