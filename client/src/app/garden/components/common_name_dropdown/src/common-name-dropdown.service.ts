/**
 * The CommonNameListService requests all common names from the server.
 * These common names are then loaded into the CommonNameListComponent for viewing and interaction.
 *
 * @author Iteration 3 - Team revolver en guardia
 */
import {Injectable} from "@angular/core";
import {Observable} from "rxjs";
import {CommonName} from "./common-name";
import {Http} from "@angular/http";
import {CommonNameCollection} from "./commonnamecollection";
import {Plant} from "../../plant_list/src/plant";
import {PlantFilter} from "../../plant_list/src/plantfilter";
import {CommonNameFilter} from "./common-name-filter";
import {PlantListService} from "../../plant_list/src/plant-list.service";
import {Bed} from "../../bed_dropdown/src/bed";

@Injectable()
export class CommonNameDropdownService {

    private readonly URL: string = process.env.API_URL;
    /**
     * Master collection of common names
     */
    private commonNameCollection: CommonNameCollection;

    /**
     * Collection of common names shown in CommonNameListComponent
     */
    public commonNames: CommonName[];

    constructor(private http:Http, private plantListService: PlantListService) {
        this.getCommonNamesFromServer().subscribe(
            commonNames => {
                this.commonNameCollection = new CommonNameCollection(commonNames);
                this.commonNames = this.commonNameCollection.getCommonNames();
            },
            err => {
                console.log(err);
            }
        );
    }

    /**
     * Requests the list of common names from the server.
     * @returns {Observable<R>} - the common name collection from the server
     */
    getCommonNamesFromServer(): Observable<CommonName[]> {
        return this.http.request(this.URL + "/commonNames").map(res => res.json());
    }

    /**
     * Gets the list of CommonNames shown within CommonNameListComponent
     * @returns {CommonName[]}
     */
    public getCommonNames(): CommonName[]{
        return this.commonNames;
    }

    public updateCommonNamesDropdown(plants: Plant[], bedName: string): void{
        this.plantListService.setCommonNameFilter(PlantFilter.NO_FILTER);
        this.commonNames = CommonNameFilter.filterByBed(plants, this.commonNameCollection.getCommonNames(), new Bed(bedName));
    }

}
