import { ModuleWithProviders }  from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import {GardenComponent} from "./garden/src/garden-component";
import {AdminComponent} from "./admin/src/admin.component";
import {ImportComponent} from "./admin/src/import.component";
import {ExportComponent} from "./admin/src/export.component";
import {GraphComponent} from "./admin/src/google-charts.component";
import {PlantComponent} from "./garden/components/plant_list/src/plant.component";
import {AuthGuard} from "./admin/authentication/auth-guard";
import {IncorrectAccountComponent} from "./admin/authentication/incorrect-account.component";
import {SlowLoginComponent} from "./admin/authentication/slow-login.component";

export const routes: Routes = [

    { path: '', redirectTo: "/bed/all", pathMatch: 'full' },
    { path: 'bed', redirectTo: "/bed/all", pathMatch: 'full' },
    { path: 'bed/:id', component: GardenComponent },
    { path: 'plant/:id', component: PlantComponent },
    { path: 'admin', component: AdminComponent, canActivate: ['CanAlwaysActivateGuard', AuthGuard]},
    { path: 'admin/importData', component: ImportComponent, canActivate: ['CanAlwaysActivateGuard', AuthGuard]},
    { path: 'admin/exportData', component: ExportComponent, canActivate: ['CanAlwaysActivateGuard', AuthGuard]},
    { path: 'admin/graph', component: GraphComponent},
    { path: 'admin/incorrectAccount', component: IncorrectAccountComponent},
    { path: 'admin/slowLogin', component: SlowLoginComponent},

];

export const routing: ModuleWithProviders = RouterModule.forRoot(routes);