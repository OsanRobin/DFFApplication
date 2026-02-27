import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AttributeoverviewComponent } from './attributeoverview.component';

describe('AttributeoverviewComponent', () => {
  let component: AttributeoverviewComponent;
  let fixture: ComponentFixture<AttributeoverviewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AttributeoverviewComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AttributeoverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
