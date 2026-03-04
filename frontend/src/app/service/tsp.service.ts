import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { City, TspResult, TspProgress } from '../model/city';

@Injectable({
  providedIn: 'root'
})
export class TspService {
  private apiUrl = window.location.hostname === 'localhost' 
    ? 'http://localhost:8090/api' 
    : '/api';

  constructor(private http: HttpClient) {}

  private getHeaders() {
    return {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    };
  }

  addCity(city: City): Observable<City> {
    return this.http.post<City>(`${this.apiUrl}/cities`, city, { headers: this.getHeaders() });
  }

  getCities(): Observable<City[]> {
    return this.http.get<City[]>(`${this.apiUrl}/cities`, { headers: this.getHeaders() });
  }

  deleteAllCities(): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/cities`, { headers: this.getHeaders() });
  }

  optimize(): Observable<TspResult> {
    return this.http.post<TspResult>(`${this.apiUrl}/tsp/optimize`, {}, { headers: this.getHeaders() });
  }

  seedCities(): Observable<City[]> {
    return this.http.post<City[]>(`${this.apiUrl}/cities/seed`, {}, { headers: this.getHeaders() });
  }

  getRouteGeometry(fromCityId: number, toCityId: number): Observable<number[][]> {
    return this.http.get<number[][]>(`${this.apiUrl}/tsp/route?fromCityId=${fromCityId}&toCityId=${toCityId}`, { headers: this.getHeaders() });
  }

  optimizeStream(): Subject<TspProgress> {
    const subject = new Subject<TspProgress>();
    const baseUrl = window.location.hostname === 'localhost' 
      ? 'http://localhost:8090' 
      : '';
    const eventSource = new EventSource(`${baseUrl}/api/tsp/optimize/stream`);

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
