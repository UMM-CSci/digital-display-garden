/**
 * The FilterGardenSidebarComponent is anchored on the left side of the screen
 * in a vertical orientation that provides an interface to filter the PlantListComponent.
 * @author Iteration 3 - Team Revolver en Guardia
 * @editor Iteration 4 - Team Revolver en Guardia++
 */
import { Component } from '@angular/core';
import {BedDropdownService} from "../../bed_dropdown/src/bed-dropdown.service";
import {PlantListService} from "../../plant_list/src/plant-list.service"
import {Location} from '@angular/common';

@Component({
    selector: 'filter-garden-component',
    templateUrl: 'filter-garden.component.html',
})

export class FilterGardenComponent {
    private searchTerms : string;

    constructor(private bedListService: BedDropdownService,private plantListService: PlantListService,
                private location: Location){ }


    private handleSearchTerms(searchTerms): void{

        //this.location.replaceState("/bed/" + searchTerms);

        // Filter plant list
        this.plantListService.setSearchTerms(searchTerms);
    }
}

