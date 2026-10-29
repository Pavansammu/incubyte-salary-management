import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { of } from 'rxjs';

import { InsightsComponent } from './insights.component';
import { InsightsService } from '../core/insights.service';
import { CountrySalaryStats } from '../core/models';

const COUNTRIES: CountrySalaryStats[] = [
  { country: 'GERMANY', countryName: 'Germany', currency: 'EUR', headcount: 800, minSalary: 20000, maxSalary: 160000, averageSalary: 66000 },
  { country: 'INDIA', countryName: 'India', currency: 'INR', headcount: 3500, minSalary: 275000, maxSalary: 3500000, averageSalary: 1400000 },
];

describe('InsightsComponent', () => {
  let fixture: ComponentFixture<InsightsComponent>;
  let component: InsightsComponent;
  let insights: jasmine.SpyObj<InsightsService>;

  beforeEach(async () => {
    insights = jasmine.createSpyObj<InsightsService>('InsightsService', ['byCountry', 'byJobTitle', 'distribution']);
    insights.byCountry.and.returnValue(of(COUNTRIES));
    insights.byJobTitle.and.returnValue(of([
      { jobTitle: 'Engineering Manager', headcount: 96, minSalary: 1200000, maxSalary: 3500000, averageSalary: 2800000 },
    ]));
    insights.distribution.and.callFake(country => of({
      country, currency: 'INR',
      bands: [
        { from: 275000, to: 920000, headcount: 50 },
        { from: 920000, to: 1565000, headcount: 200 },
      ],
    }));

    await TestBed.configureTestingModule({
      imports: [InsightsComponent],
      providers: [provideNoopAnimations(), { provide: InsightsService, useValue: insights }],
    }).compileComponents();

    fixture = TestBed.createComponent(InsightsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('summarises total headcount across countries', () => {
    expect(component.totalHeadcount()).toBe(4300);
    expect(fixture.nativeElement.textContent).toContain('4,300 people across 2 countries');
  });

  it('opens on the country with the most employees and loads its details', () => {
    expect(component.selected()).toBe('INDIA');
    expect(insights.byJobTitle).toHaveBeenCalledWith('INDIA');
    expect(insights.distribution).toHaveBeenCalledWith('INDIA');
    expect(fixture.nativeElement.textContent).toContain('Engineering Manager');
  });

  it('loads a different country when the HR Manager selects it', () => {
    component.select('GERMANY');

    expect(insights.byJobTitle).toHaveBeenCalledWith('GERMANY');
    expect(component.currency()).toBe('EUR');
  });

  it('scales salary bars relative to the busiest band', () => {
    expect(component.barWidth(200)).toBe(100);
    expect(component.barWidth(50)).toBe(25);
  });
});
