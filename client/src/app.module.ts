import { NgModule }       from '@angular/core';
import { BrowserModule }  from '@angular/platform-browser';
import { HttpModule, JsonpModule } from '@angular/http';
import { AppComponent }         from './app/app.component';
import { routing } from './app/app.routes';
import { FormsModule } from '@angular/forms';
import { PipeModule } from './pipe.module';

import { NavbarComponent } from './app/navbar/navbar.component';
import { GardenComponent } from "./app/garden/src/garden-component";
import { PlantListComponent } from "./app/garden/components/plant_list/src/plant-list.component";
import { BedDropdownComponent } from "./app/garden/components/bed_dropdown/src/bed-dropdown.component";

import { PlantListService } from "./app/garden/components/plant_list/src/plant-list.service";
import { BedDropdownService } from "./app/garden/components/bed_dropdown/src/bed-dropdown.service";
import { AdminComponent } from "./app/admin/admin.component";
import { PlantComponent } from "./app/garden/components/plant_list/src/plant.component";
import { ImportComponent } from "./app/admin/import.component";
import { AdminService } from "./app/admin/admin.service";
import { ExportComponent } from "./app/admin/export.component";
import { FileUploadComponent } from "./app/admin/file-upload.component";
import { PlantService } from "./app/garden/components/plant_list/src/plant.service";
import { GraphComponent } from "./app/admin/google-charts.component";
import { Ng2GoogleChartsModule } from 'ng2-google-charts';

import { FilterGardenComponent } from "./app/garden/components/filter_garden_sidebar/src/filter-garden.component";
import { FooterComponent } from "./app/garden/components/footer/src/footer.component";
import { RouterModule } from "@angular/router";

@NgModule({
    imports: [
        BrowserModule,
        HttpModule,
        JsonpModule,
        routing,
        FormsModule,
        PipeModule,
        Ng2GoogleChartsModule,
        PipeModule,
        RouterModule
    ],
    declarations: [
        AppComponent,
        NavbarComponent,
        GardenComponent,
        FilterGardenComponent,
        PlantListComponent,
        BedDropdownComponent,
        PlantComponent,
        AdminComponent,
        ImportComponent,
        ExportComponent,
        FileUploadComponent,
        FooterComponent,
        GraphComponent,

    ],
    providers: [
        PlantListService,
        BedDropdownService,
        AdminService,
        PlantService
    ],
    bootstrap: [ AppComponent ]
})

export class AppModule {}