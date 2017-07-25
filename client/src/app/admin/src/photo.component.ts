import {Component, OnInit, ViewChild} from '@angular/core';
import { AdminService } from './admin.service';
import { PlantListService } from '../../garden/components/plant_list/src/plant-list.service';
import {Plant} from "../../garden/components/plant_list/src/plant";
import {Params, ActivatedRoute, Router} from "@angular/router";
import { Location } from '@angular/common';


@Component({
    selector: 'photo-component',
    templateUrl: 'photo.component.html',
})
export class PhotoComponent implements OnInit {
    public plant: Plant;
    public clicked: boolean;
    searchTerms : string;
    authorized: boolean;

    private readonly URL: string = process.env.API_URL;

    constructor(private adminService: AdminService, private plantListService: PlantListService, private router: Router, private route: ActivatedRoute, private location: Location) {

    }

    @ViewChild('fu') fu;

    filename:string;
    uploadAttempted:boolean = false;

    handleUpload(){
        this.fu.uploadPhoto(this.plant.id, this.plant.gardenLocation).subscribe(
            response => {
                this.filename = response.json();
                this.uploadAttempted = true;
                this.plantListService.refreshPlant(this.plant.id, this.plant.gardenLocation)
                    .add(res => {
                    this.plant = this.plantListService.getPlant(this.plant.id, this.plant.gardenLocation);
                });
                //location.reload();
            },
            err => {
                this.uploadAttempted = true;
            }
        );

    }

    private handleSearchTerms(searchTerms): void{

        //this.location.replaceState("/bed/" + searchTerms);

        // Filter plant list
        this.plantListService.setSearchTerms(searchTerms);
    }

    private listEntryClicked(plant : Plant)
    {
        this.plant = plant;
        this.uploadAttempted = false;
        this.filename = undefined;
        this.clicked = true;
    }

    // public getPlant(id: string, bed: string): void {
    //     this.uploadAttempted = false;
    //     this.filename = undefined;
    //     this.clicked = true;
    //     this.plantListService.getPlantById(id, bed, false).subscribe(
    //         plant => this.plant = plant,
    //         err => {
    //             console.log(err);
    //         })
    // }

    ngOnInit(): void {

        this.route.queryParams.subscribe(
            qparams => {
                this.route.params.subscribe(params => {
/*
                    this.searchTerms = qparams['query'];
                    if(this.searchTerms !== ""){
                        this.getPlant(, params['bed']); //TODO Bed is set to 10 specifically right now.
                        this.clicked = false;
                    }*/
                })
            }
        );
        this.adminService.authorized().subscribe(authorized => this.authorized = authorized);

    }
/*
    refresh(id: string) {
        this.router.navigate(['/admin/managePhotos'], { queryParams: { query: this.id} });
    }*/
}