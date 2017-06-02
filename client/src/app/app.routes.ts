import { ModuleWithProviders }  from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import {GardenComponent} from "./garden/src/garden-component";
import {AdminComponent} from "./admin/admin.component";
import {ImportComponent} from "./admin/import.component";
import {ExportComponent} from "./admin/export.component";
import {GraphComponent} from "./admin/google-charts.component";
import {PlantComponent} from "./garden/components/plant_list/src/plant.component";


export const routes: Routes = [

    { path: '', redirectTo: "/bed/all", pathMatch: 'full' },
    { path: 'bed', redirectTo: "/bed/all", pathMatch: 'full' },
    { path: 'bed/:id', component: GardenComponent },
    { path: 'plant/:bed/:id', component: PlantComponent },
    { path: 'adminj148iz5noq50aaq5', component: AdminComponent },
    { path: 'adminj148iz5noq50aaq5/importData', component: ImportComponent},
    { path: 'adminj148iz5noq50aaq5/exportData', component: ExportComponent},
    { path: 'adminj148iz5noq50aaq5/graph', component: GraphComponent},

];

export const routing: ModuleWithProviders = RouterModule.forRoot(routes);