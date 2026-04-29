import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SegmentsComponents } from './segments.components';

describe('SegmentsComponents', () => {
  let component: SegmentsComponents;
  let fixture: ComponentFixture<SegmentsComponents>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SegmentsComponents]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SegmentsComponents);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
