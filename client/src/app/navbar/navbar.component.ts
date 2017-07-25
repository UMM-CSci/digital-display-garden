import { Component } from '@angular/core';
import {PlantListService} from "../garden/components/plant_list/src/plant-list.service";

@Component({
    selector: 'navbar-component',
    templateUrl: 'navbar.component.html'
})

export class NavbarComponent {

    constructor (private plantListService: PlantListService)
    {

    }


    private clearSearchTerms()
    {
        this.plantListService.setSearchTerms("");
    }
}

