import { describe, it, expect } from 'vitest';
import { VehicleCategory } from '../types/ride';

describe('CabFlow Frontend Types & Pricing Multipliers', () => {
  it('correctly exports VehicleCategory constants', () => {
    expect(VehicleCategory.SEDAN).toBe('SEDAN');
    expect(VehicleCategory.HATCHBACK).toBe('HATCHBACK');
    expect(VehicleCategory.SUV).toBe('SUV');
    expect(VehicleCategory.PREMIUM).toBe('PREMIUM');
  });

  it('verifies vehicle options structure', () => {
    const keys = Object.keys(VehicleCategory);
    expect(keys).toContain('SEDAN');
    expect(keys).toContain('SUV');
    expect(keys.length).toBe(4);
  });
});
