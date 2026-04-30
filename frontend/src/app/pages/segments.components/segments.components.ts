import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { HeaderComponent } from '../../shell/header.component/header.component';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';

type SegmentRow = {
  id: string;
  name: string;
  description: string | null;
  customerCount: number;
};

@Component({
  selector: 'app-segments',
  imports: [CommonModule, FormsModule, RouterModule, HeaderComponent],
  templateUrl: './segments.components.html',
  styleUrl: './segments.components.css',
})
export class SegmentsComponents {
  private http = inject(HttpClient);
  private router = inject(Router);

  private readonly apiUrl = 'http://localhost:8081/api/customers/segments';

  loading = false;
  error = '';
  segments: SegmentRow[] = [];

  showDeleteSegmentConfirm = false;
  segmentToDelete: SegmentRow | null = null;

  newSegment = {
    id: '',
    name: '',
    description: '',
  };

  ngOnInit(): void {
    this.loadSegments();
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }

  loadSegments(): void {
    this.loading = true;
    this.error = '';

    this.http
      .get<SegmentRow[]>(this.apiUrl, {
        headers: this.headers(),
        params: this.params(),
      })
      .subscribe({
        next: (segments) => {
          this.segments = segments;
          this.loading = false;
        },
        error: (err) => {
          console.error('Failed to load segments', err);
          this.error = 'Failed to load segments.';
          this.loading = false;
        },
      });
  }

  addSegment(): void {
    const id = this.newSegment.id.trim();
    const name = this.newSegment.name.trim();

    if (!id || !name) {
      this.error = 'Segment ID and name are required.';
      return;
    }

    this.loading = true;
    this.error = '';

    this.http
      .post(
        this.apiUrl,
        {
          id,
          name,
          description: this.newSegment.description.trim(),
        },
        {
          headers: this.headers(),
          params: this.params(),
        }
      )
      .subscribe({
        next: () => {
          this.newSegment = { id: '', name: '', description: '' };
          this.loadSegments();
        },
        error: (err) => {
          console.error('Failed to create segment', err);
          this.error = 'Failed to create segment.';
          this.loading = false;
        },
      });
  }

  openDeleteSegmentConfirm(segment: SegmentRow): void {
    if (this.loading) {
      return;
    }

    this.segmentToDelete = segment;
    this.showDeleteSegmentConfirm = true;
  }

  closeDeleteSegmentConfirm(): void {
    if (this.loading) {
      return;
    }

    this.segmentToDelete = null;
    this.showDeleteSegmentConfirm = false;
  }

  confirmDeleteSegment(): void {
    if (!this.segmentToDelete) {
      return;
    }

    this.deleteSegment(this.segmentToDelete);
  }

  private deleteSegment(segment: SegmentRow): void {
    this.loading = true;
    this.error = '';

    this.http
      .delete(`${this.apiUrl}/${encodeURIComponent(segment.id)}`, {
        headers: this.headers(),
        params: this.params(),
      })
      .subscribe({
        next: () => {
          this.segmentToDelete = null;
          this.showDeleteSegmentConfirm = false;
          this.loadSegments();
        },
        error: (err) => {
          console.error('Failed to delete segment', err);
          this.error = 'Failed to delete segment.';
          this.segmentToDelete = null;
          this.showDeleteSegmentConfirm = false;
          this.loading = false;
        },
      });
  }

  private headers(): HttpHeaders {
    return new HttpHeaders({
      'authentication-token':
        localStorage.getItem('authentication-token') ||
        localStorage.getItem('authenticationToken') ||
        localStorage.getItem('token') ||
        '',
    });
  }

  private params(): HttpParams {
    return new HttpParams().set(
      'domain',
      localStorage.getItem('domain') ||
        localStorage.getItem('domainName') ||
        'DailyFreshFood'
    );
  }
}