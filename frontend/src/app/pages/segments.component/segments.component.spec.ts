import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SegmentsComponent } from './segments.component';

describe('SegmentsComponent', () => {
  let component: SegmentsComponent;
  let fixture: ComponentFixture<SegmentsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SegmentsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SegmentsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
