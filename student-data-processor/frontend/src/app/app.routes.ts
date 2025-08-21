import { Routes } from '@angular/router';
import { DataGenerationComponent } from './components/data-generation/data-generation.component';
import { DataProcessingComponent } from './components/data-processing/data-processing.component';
import { DataUploadComponent } from './components/data-upload/data-upload.component';
import { ReportsComponent } from './components/reports/reports.component';

export const routes: Routes = [
  { path: '', redirectTo: '/generate', pathMatch: 'full' },
  { path: 'generate', component: DataGenerationComponent },
  { path: 'process', component: DataProcessingComponent },
  { path: 'upload', component: DataUploadComponent },
  { path: 'reports', component: ReportsComponent },
  { path: '**', redirectTo: '/generate' } // Wildcard route for 404 page
];
