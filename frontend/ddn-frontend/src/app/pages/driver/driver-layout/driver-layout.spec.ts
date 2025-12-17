import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverLayoutComponent } from './driver-layout';

describe('DriverLayout', () => {
  let component: DriverLayoutComponent;
  let fixture: ComponentFixture<DriverLayoutComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverLayoutComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverLayoutComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
