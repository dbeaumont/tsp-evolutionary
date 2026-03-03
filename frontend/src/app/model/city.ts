export interface City {
  id?: number;
  name: string;
  x: number;
  y: number;
}

export interface TspResult {
  optimalRoute: City[];
  totalDistance: number;
  generations: number;
  executionTimeMs: number;
}

export interface TspProgress {
  generation: number;
  bestRoute: City[];
  distance: number;
}
