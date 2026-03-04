import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { City, TspResult, TspProgress } from '../model/city';

@Injectable({
  providedIn: 'root'
})
export class TspService {
  private readonly apiUrl = `${window.location.origin}/api`;
  private readonly sseUrl = `${window.location.origin}/api/tsp/optimize/stream`;

  constructor(private http: HttpClient) {}

  addCity(city: City): Observable<City> {
    return this.http.post<City>(`${this.apiUrl}/cities`, city);
  }

  getCities(): Observable<City[]> {
    return this.http.get<City[]>(`${this.apiUrl}/cities`);
  }

  deleteAllCities(): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/cities`);
  }

  optimize(): Observable<TspResult> {
    return this.http.post<TspResult>(`${this.apiUrl}/tsp/optimize`, {});
  }

  generateCitiesCsv(cityCount: number): Observable<HttpResponse<Blob>> {
    return this.http.post(`${this.apiUrl}/cities/csv/generate`, { cityCount }, {
      observe: 'response',
      responseType: 'blob'
    });
  }

  uploadCitiesCsv(file: File): Observable<{
    citiesImported: number;
    distancePairsProvided: number;
    distancePairsCalculated: number;
    distancePairsTotal: number;
    distanceEntriesCached: number;
  }> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<{
      citiesImported: number;
      distancePairsProvided: number;
      distancePairsCalculated: number;
      distancePairsTotal: number;
      distanceEntriesCached: number;
    }>(
      `${this.apiUrl}/cities/csv/upload`,
      formData
    );
  }

  getRouteGeometry(fromCityId: number, toCityId: number): Observable<number[][]> {
    return this.http.get<number[][]>(`${this.apiUrl}/tsp/route?fromCityId=${fromCityId}&toCityId=${toCityId}`);
  }

  optimizeStream(): Subject<TspProgress> {
    const subject = new Subject<TspProgress>();
    const eventSource = new EventSource(this.sseUrl);

    eventSource.onopen = () => {
      console.log('SSE connection opened');
    };
    
    eventSource.onmessage = (event) => {
      if (!event.data || event.data.trim() === '') {
        console.log('Empty SSE message received');
        return;
      }
      try {
        const data = JSON.parse(event.data);
        subject.next(data);
      } catch (e) {
        console.error('SSE parse error:', e, 'data:', event.data);
      }
    };

    eventSource.onerror = (err) => {
      console.error('SSE error:', err);
      eventSource.close();
      subject.complete();
    };

    return subject;
  }
}
