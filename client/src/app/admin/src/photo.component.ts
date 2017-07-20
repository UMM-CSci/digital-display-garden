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
    public id: string;
    public bed: string;
    public plant: Plant;
    public clicked: boolean;
    public textValue: string;
    searchTerms : string;
    authorized: boolean;

    private readonly URL: string = process.env.API_URL;

    constructor(private adminService: AdminService, private plantListService: PlantListService, private router: Router, private route: ActivatedRoute, private location: Location) {

    }

    @ViewChild('fu') fu;

    filename:string;
    uploadAttempted:boolean = false;

    handleUpload(){
        this.fu.uploadPhoto(this.id, this.bed).subscribe(
            response => {
                this.filename = response.json();
                this.uploadAttempted = true;
                location.reload();
            },
            err => {
                this.uploadAttempted = true;
            }
        );

    }

    private handleSearchTerms(searchTerms): void{

        //this.location.replaceState("/bed/" + searchTerms);

        // Filter plant list
        //this.plantListService.setSearchTerms(searchTerms); //TODO
    }

    public getPlant(id: string, bed: string): void {
        this.uploadAttempted = false;
        this.filename = undefined;
        this.clicked = true;
        this.id = id;
        this.bed = bed;
        this.plantListService.getPlantById(id, bed, false).subscribe(
            plant => this.plant = plant,
            err => {
                console.log(err);
            })
    }

    private onKey(event: any) {
        if (event.key === "Enter") {
            this.getPlant(this.textValue, this.bed);
        }
    }

    ngOnInit(): void {
        this.route.queryParams.subscribe((params: Params) => {this.textValue = params['query'];
        if(this.textValue !== ""){
            this.getPlant(this.textValue, "10"); //TODO Bed is set to 10 specifically right now.
            this.clicked = false;
        }
        });
        this.adminService.authorized().subscribe(authorized => this.authorized = authorized);

    }

    refresh(id: string) {
        this.router.navigate(['/admin/managePhotos'], { queryParams: { query: this.id} });
    }
}