/**
 * Represents all functions and data that are contained within the PlantListComponent view
 * within the GardenComponent.
 *
 * @author Iteration 2 - Team Omar Anwar
 * @author Iteration 3 - Team Revolver en Guardia
 */
import {Component, OnInit} from '@angular/core';
import {PlantListService} from "./plant-list.service";
import {ActivatedRoute} from "@angular/router";
import {BedDropdownComponent} from "../../bed_dropdown/src/bed-dropdown.component";
import {BedDropdownService} from "../../bed_dropdown/src/bed-dropdown.service";
import {FilterGardenComponent} from "../../filter_garden_sidebar/src/filter-garden.component";
import {GardenComponent} from "../../../src/garden-component";
import {Location} from '@angular/common';
import {isUndefined} from "util";

@Component({
    selector: 'plant-list',
    templateUrl: 'plant-list.component.html'
})

export class PlantListComponent implements OnInit {

    constructor(private plantListService: PlantListService, private bedListService : BedDropdownService, private route: ActivatedRoute, private location: Location){ }

    ngOnInit(){
        //Send reportBedVisit Post request
        this.route.queryParams.subscribe(
            qparams => {
                this.route.params.subscribe(params => {
                    let bedName = params['id'];
                    let isQr : boolean = qparams["qr"];
                    let searchTerms : string = qparams["query"];

                    //Send post request to server reporting a Bed Visit
                    this.bedListService.reportBedVisit(bedName, isQr).subscribe();

                    //Replace URL to hide the qr=true query param from the user (and removes it from browser history)
                    if(searchTerms == "" || isUndefined(searchTerms)) {
                        this.location.replaceState("/bed/" + this.plantListService.getBedFilter());
                    }
                    else
                    {
                        this.plantListService.setSearchTerms(searchTerms);
                        this.location.replaceState("/bed/" + this.plantListService.getBedFilter() + "?query=" + searchTerms);
                    }


                    //Tell the plantListService what bed it should filter by
                    if(bedName != "all")
                        this.plantListService.prepareBedFilter(bedName);

                });
            },
            err => {
                console.log(err);
            });
    }
}
