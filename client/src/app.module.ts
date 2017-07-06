import { NgModule }       from '@angular/core';
import { BrowserModule }  from '@angular/platform-browser';
import { HttpModule, JsonpModule } from '@angular/http';
import { AppComponent }         from './app/app.component';
import { routing } from './app/app.routes';
import { FormsModule } from '@angular/forms';
import { PipeModule } from './pipe.module';
import {enableProdMode} from '@angular/core';

import { NavbarComponent } from './app/navbar/navbar.component';
import { GardenComponent } from "./app/garden/src/garden-component";
import { PlantListComponent } from "./app/garden/components/plant_list/src/plant-list.component";
import { BedDropdownComponent } from "./app/garden/components/bed_dropdown/src/bed-dropdown.component";
import { CommonNameDropdownComponent } from "./app/garden/components/common_name_dropdown/src/common-name-dropdown.component";

import { PlantListService } from "./app/garden/components/plant_list/src/plant-list.service";
import { BedDropdownService } from "./app/garden/components/bed_dropdown/src/bed-dropdown.service";
import { CommonNameDropdownService } from "./app/garden/components/common_name_dropdown/src/common-name-dropdown.service";

import { AdminComponent } from "./app/admin/src/admin.component";
import {SlowLoginComponent} from "./app/admin/authentication/slow-login.component";
import {AuthGuard} from "./app/admin/authentication/auth-guard";
import {IncorrectAccountComponent} from "./app/admin/authentication/incorrect-account.component";
import {ConfirmOptions, Position, ConfirmModule} from 'angular2-bootstrap-confirm';
import {Positioning} from 'angular2-bootstrap-confirm/position';

import { PlantComponent } from "./app/garden/components/plant_list/src/plant.component";
import { ImportComponent } from "./app/admin/src/import.component";
import { DeleteComponent } from "./app/admin/src/delete.component";
import { AdminService } from "./app/admin/src/admin.service";
import { ExportComponent } from "./app/admin/src/export.component";
import { FileUploadComponent } from "./app/admin/src/file-upload.component";
import { PlantService } from "./app/garden/components/plant_list/src/plant.service";
import { GraphComponent } from "./app/admin/src/google-charts.component";
import { Ng2GoogleChartsModule } from 'ng2-google-charts';

import { FilterGardenComponent } from "./app/garden/components/filter_garden_sidebar/src/filter-garden.component";
import { FooterComponent } from "./app/garden/components/footer/src/footer.component";
import { RouterModule } from "@angular/router";

@NgModule({
    imports: [
        BrowserModule,
        ConfirmModule,
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
        CommonNameDropdownComponent,
        PlantComponent,
        AdminComponent,
        ImportComponent,
        ExportComponent,
        DeleteComponent,
        FileUploadComponent,
        FooterComponent,
        GraphComponent,
        IncorrectAccountComponent,
        SlowLoginComponent,

    ],
    providers: [
        PlantListService,
        BedDropdownService,
        CommonNameDropdownService,
        AdminService,
        PlantService,
        {provide: 'CanAlwaysActivateGuard', useClass: AuthGuard},
        AuthGuard,
    ],
    bootstrap: [ AppComponent ]
})

export class AppModule {}