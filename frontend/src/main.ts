import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';

console.log('Starting TSP Evolutionary app...');

bootstrapApplication(AppComponent, appConfig)
  .then(() => console.log('App bootstrapped successfully'))
  .catch((err) => {
    console.error('Bootstrap error:', err);
    document.body.innerHTML = '<h1>Error loading app</h1><pre>' + err + '</pre>';
  });
