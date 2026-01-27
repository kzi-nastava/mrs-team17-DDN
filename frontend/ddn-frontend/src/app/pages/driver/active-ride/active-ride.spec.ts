import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActiveRide } from './active-ride';

describe('ActiveRide', () => {
  let component: ActiveRide;
  let fixture: ComponentFixture<ActiveRide>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActiveRide]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ActiveRide);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
