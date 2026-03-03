import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { City, TspResult, TspProgress } from '../model/city';

@Injectable({
  providedIn: 'root'
})
export class TspService {
  private apiUrl = 'http://localhost:8080/api';

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

  optimizeStream(): Subject<TspProgress> {
    const subject = new Subject<TspProgress>();
    const eventSource = new EventSource('http://localhost:8080/api/tsp/optimize/stream');

    eventSource.onmessage = (event) => {
      const data = JSON.parse(event.data);
      subject.next(data);
    };

    eventSource.onerror = () => {
      eventSource.close();
      subject.complete();
    };

    return subject;
  }

  seedCities(): Observable<City[]> {
    return this.http.post<City[]>(`${this.apiUrl}/cities/seed`, {});
  }
}
