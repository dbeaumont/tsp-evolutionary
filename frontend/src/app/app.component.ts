import { Component, OnInit, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TspService } from './service/tsp.service';
import { ToastService } from './service/toast.service';
import { City, TspResult, TspProgress } from './model/city';
import * as L from 'leaflet';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit, AfterViewInit, OnDestroy {
  cities: City[] = [];
  newCity: City = { name: '', x: 0, y: 0 };
  result: TspResult | null = null;
  loading = false;
  optimizing = false;
  currentProgress: TspProgress | null = null;
  toast: { message: string; type: string } | null = null;
  private toastTimeout: any;

  private map!: L.Map;
  private markers: L.CircleMarker[] = [];
  private routeLine: L.Polyline | null = null;

  constructor(
    private tspService: TspService,
    private toastService: ToastService
  ) {}

  ngOnInit() {
    this.loadCities();
    this.toastService.toasts$.subscribe(toast => {
      this.showToast(toast.message, toast.type);
    });
  }

  private showToast(message: string, type: string) {
    this.toast = { message, type };
    clearTimeout(this.toastTimeout);
    this.toastTimeout = setTimeout(() => {
      this.toast = null;
    }, 3000);
  }

  ngAfterViewInit() {
    this.initMap();
  }

  ngOnDestroy() {
    if (this.map) {
      this.map.remove();
    }
  }

  private initMap() {
    this.map = L.map('map').setView([46.2276, 2.2137], 6);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);
  }

  private updateMap() {
    this.markers.forEach(marker => marker.remove());
    this.markers = [];

    if (this.routeLine) {
      this.routeLine.remove();
      this.routeLine = null;
    }

    const bounds: [number, number][] = [];

    this.cities.forEach(city => {
      const latLng = this.coordToLatLng(city.x, city.y);
      bounds.push(latLng);

      const marker = L.circleMarker(latLng as L.LatLngTuple, {
        radius: 6,
        fillColor: '#667eea',
        color: '#fff',
        weight: 2,
        opacity: 1,
        fillOpacity: 0.8
      }).bindPopup(city.name);

      marker.addTo(this.map);
      this.markers.push(marker);
    });

    if (bounds.length > 0) {
      this.map.fitBounds(bounds, { padding: [50, 50] });
    }
  }

  private updateRoute(route: City[]) {
    if (this.routeLine) {
      this.routeLine.remove();
    }

    const latLngs: [number, number][] = route.map(city => {
      const lat = typeof city.x === 'string' ? parseFloat(city.x) : city.x;
      const lng = typeof city.y === 'string' ? parseFloat(city.y) : city.y;
      return [lat, lng];
    });

    if (latLngs.length > 1) {
      const allRouteCoords: [number, number][] = [];
      
      const routePromises: Promise<void>[] = [];
      for (let i = 0; i < latLngs.length - 1; i++) {
        const fromCity = route[i];
        const toCity = route[i + 1];
        
        if (!fromCity.id || !toCity.id) {
          allRouteCoords.push(latLngs[i]);
          allRouteCoords.push(latLngs[i + 1]);
          continue;
        }
        
        const promise = new Promise<void>((resolve) => {
          this.tspService.getRouteGeometry(fromCity.id!, toCity.id!).subscribe({
            next: (geometry) => {
              if (geometry && geometry.length > 0) {
                geometry.forEach((coord: number[]) => {
                  allRouteCoords.push([coord[0], coord[1]]);
                });
              } else {
                allRouteCoords.push(latLngs[i]);
                allRouteCoords.push(latLngs[i + 1]);
              }
              resolve();
            },
            error: () => {
              allRouteCoords.push(latLngs[i]);
              allRouteCoords.push(latLngs[i + 1]);
              resolve();
            }
          });
        });
        routePromises.push(promise);
      }

      Promise.all(routePromises).then(() => {
        if (allRouteCoords.length > 0) {
          allRouteCoords.push(allRouteCoords[0]);
        }
        
        this.routeLine = L.polyline(allRouteCoords.length > 0 ? allRouteCoords : latLngs, {
          color: '#e53e3e',
          weight: 3,
          opacity: 0.8
        }).addTo(this.map);
      });
    } else {
      this.routeLine = L.polyline(latLngs, {
        color: '#e53e3e',
        weight: 3,
        opacity: 0.8
      }).addTo(this.map);
    }
  }

  private coordToLatLng(x: number | string, y: number | string): [number, number] {
    return [x as number, y as number];
  }

  loadCities() {
    this.tspService.getCities().subscribe({
      next: (cities) => {
        this.cities = cities;
        setTimeout(() => this.updateMap(), 100);
      },
      error: (err) => {
        console.error('Error loading cities:', err);
        this.toastService.error('Erreur lors du chargement des villes - backend connecté?');
        this.cities = [];
      }
    });
  }

  addCity() {
    if (!this.newCity.name || this.newCity.x === undefined || this.newCity.y === undefined) {
      return;
    }

    this.loading = true;
    this.tspService.addCity(this.newCity).subscribe({
      next: () => {
        this.loadCities();
        this.newCity = { name: '', x: 0, y: 0 };
        this.loading = false;
        this.result = null;
        this.currentProgress = null;
        if (this.routeLine) {
          this.routeLine.remove();
          this.routeLine = null;
        }
      },
      error: () => {
        this.loading = false;
        this.toastService.error('Erreur lors de l\'ajout de la ville');
      }
    });
  }

  clearCities() {
    this.tspService.deleteAllCities().subscribe({
      next: () => {
        this.cities = [];
        this.result = null;
        this.currentProgress = null;
        this.updateMap();
      },
      error: () => this.toastService.error('Erreur lors de l\'effacement des villes')
    });
  }

  optimize() {
    if (this.cities.length < 2) {
      this.toastService.error('Il faut au moins 2 villes pour optimiser');
      return;
    }

    this.optimizing = true;
    this.currentProgress = null;
    this.result = null;

    if (this.routeLine) {
      this.routeLine.remove();
      this.routeLine = null;
    }

    const progress$ = this.tspService.optimizeStream();

    progress$.subscribe({
      next: (progress) => {
        this.currentProgress = progress;
        this.updateRoute(progress.bestRoute);
      },
      error: (err) => {
        this.toastService.error('Erreur lors de l\'optimisation');
        this.optimizing = false;
      },
      complete: () => {
        this.optimizing = false;
        this.tspService.optimize().subscribe({
          next: (result) => {
            this.result = result;
            this.currentProgress = null;
            this.updateRoute(result.optimalRoute);
            this.toastService.success('Optimisation terminée !');
          },
          error: () => {
            this.toastService.error('Erreur lors de la récupération du résultat');
          }
        });
      }
    });
  }

  clearResult() {
    this.result = null;
    this.currentProgress = null;
    if (this.routeLine) {
      this.routeLine.remove();
      this.routeLine = null;
    }
  }

  seedCities() {
    this.loading = true;
    this.tspService.seedCities().subscribe({
      next: (cities) => {
        this.cities = cities;
        this.result = null;
        this.currentProgress = null;
        this.loading = false;
        setTimeout(() => this.updateMap(), 100);
      },
      error: () => this.loading = false
    });
  }
}
