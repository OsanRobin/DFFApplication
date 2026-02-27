import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CustomerdetailComponent } from './customerdetail.component';

describe('CustomerdetailComponent', () => {
  let component: CustomerdetailComponent;
  let fixture: ComponentFixture<CustomerdetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CustomerdetailComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CustomerdetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
