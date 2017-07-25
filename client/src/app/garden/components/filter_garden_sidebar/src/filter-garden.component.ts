/**
 * The FilterGardenSidebarComponent is anchored on the left side of the screen
 * in a vertical orientation that provides an interface to filter the PlantListComponent.
 * @author Iteration 3 - Team Revolver en Guardia
 * @editor Iteration 4 - Team Revolver en Guardia++
 */
import { Component } from '@angular/core';
import {BedDropdownService} from "../../bed_dropdown/src/bed-dropdown.service";
import {ActivatedRoute} from "@angular/router";
import {PlantListService} from "../../plant_list/src/plant-list.service"
import {Location} from '@angular/common';
import {PlantFilter} from "../../plant_list/src/plantfilter";

@Component({
    selector: 'filter-garden-component',
    templateUrl: 'filter-garden.component.html',
})

export class FilterGardenComponent {
    private searchTerms : string;

    constructor(private bedListService: BedDropdownService,private plantListService: PlantListService,
                private location: Location, private route :ActivatedRoute){ }

    ngOnInit(){
        //Send reportBedVisit Post request
        this.route.queryParams.subscribe(
            qparams => {
                let searchTerms: string = qparams["query"];
                this.searchTerms=searchTerms;
            });
    }

    /**
     * Pass on the search terms to the PlantListService
     * replacing the location state if necessary
     * @param searchTerms
     */
    private handleSearchTerms(searchTerms): void{
        if(searchTerms == "") {
            //searchTerms = PlantFilter.NO_FILTER;
            this.location.replaceState("/bed/" + this.plantListService.getBedFilter());
        }
        else
            this.location.replaceState("/bed/" + this.plantListService.getBedFilter() + "?query="+searchTerms);

        // Filter plant list
        this.plantListService.setSearchTerms(searchTerms);
    }

    //programatically set Search terms
    public setSearchTerms(searchTerms : string) : void{
        this.searchTerms = searchTerms;
    }
}

