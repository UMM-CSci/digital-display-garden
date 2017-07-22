/**
 * Provides the ability to request Plant data to be sent from the server. This class also contains
 * the primary Plant Collection that will be used to store the full list of plant data. In addition,
 * this PlantListService also provides the ability to filter the plants contained within the
 * PlantListComponent by bed and common name.
 *
 * @author Iteration 2 - Team Omar Anwar
 * @editor Iteration 3 - Team revolver en guardia
 */
import {Injectable} from '@angular/core';
import { Http } from '@angular/http';
import { Plant } from './plant';
import { Observable } from "rxjs";
import {PlantCollection} from "./plantcollection";
import {PlantFilter} from "./plantfilter";
import {isUndefined} from "util";

@Injectable()
export class PlantListService {

    // URL for server plant collection
    private readonly URL: string = process.env.API_URL;

    // Master collection of all plants
    private plantCollection: PlantCollection;

    // Plants to display within the PlantListComponent
    private filteredPlants: Plant[] = [];

    // Current bed filter for plants within PlantListComponent
    private bedFilter: string = PlantFilter.NO_FILTER;
    public searchTerms: string = ""; //This is public so that it can be bound within filter-garden-component
    constructor(private http:Http) {
        this.getPlantsFromServer().subscribe(
            plants => {
                // Setup master collection and displayed filtered plants
                this.plantCollection = new PlantCollection(plants);
                this.filteredPlants = this.plantCollection.getPlants();

                // In case the user went directly to a bed page
                // and the bedFilter was prepared, filter by those plants.
                this.filterPlants();
                err => {
                    console.log(err);
                }
            }
        );
    }

    /**
     * Requests that the plant collection be sent from the server.
     * @returns {Observable<R>} - the received Observable Plant collection
     */
    public getPlantsFromServer(): Observable<Plant[]> {
        return this.http.request(this.URL + "plants").map(res => res.json());
    }

    /**
     * Requests that the Plant specified by the provided id be sent from the server.
     * @param id
     * @returns {Observable<R>}
     */
    public getPlantById(id: string, bed : string, reportVisit : boolean): Observable<Plant> {
        if(reportVisit) {
            return this.http.request(this.URL + "plant/" + bed + "/" + id).map(res => res.json());
        }
        else {
            return this.http.request(this.URL + "plant/" + bed + "/" + id + "?visitor=fa").map(res => res.json());
        }
    }


    /**
     * Requests that the PlantListComponent be updated according to the currently set filters.
     */
    private filterPlants(): void{

        // Filter from the master plant collection
        let plantsBeingFiltered: Plant[] = this.plantCollection.getPlants();

        // Apply filters to plant list
        plantsBeingFiltered = PlantFilter.filterByBedName(this.bedFilter, plantsBeingFiltered);
        plantsBeingFiltered = PlantFilter.filterByTerms(this.searchTerms, plantsBeingFiltered);

        // Bind the filtered plants to be displayed
        this.filteredPlants = plantsBeingFiltered;
    }

    /**
     * Returns the filtered plants displayed within the PlantListComponent.
     * @returns {Plant[]} - filtered plants shown in PlantListComponent
     */
    public getFilteredPlants(): Plant[]{
        return this.filteredPlants;
    }

    public getPlants(): Plant[]{
        return this.plantCollection.getPlants();
    }

    public getPlant(id : string, bed : string): Plant{
        return this.plantCollection.getPlant(id, bed);
    }

    /**
     * Filter the plants by the provided bed filter.
     * @param bedFilter - bed to filter by
     */
    public setBedFilter(bedFilter: string): void{
        this.bedFilter = bedFilter;
        if(!isUndefined(this.plantCollection)) {
            this.filterPlants();
        }
    }

    /**
     * Set the filter for plants, but don't do any filtering now.
     * filterPlants() can't be run before plant-list.service is initialized
     * and bedFilter must be set from bed-dropdown.service.ts which
     * happens to be initialized first.
     * This and the filterPlants() in the constructor enable
     * going to /bed/1S and automatically filtering by that bed.
     * @param bedFilter
     */
    public prepareBedFilter(bedFilter: string): void{
        this.bedFilter = bedFilter;
    }

    /**
     * Filter plants by arbitrary terms
     * @param searchTerms - search terms to filter by
     */
    public setSearchTerms(searchTerms: string): void{
        if(isUndefined(searchTerms)) {
            this.searchTerms = "";
        }
        else {
            this.searchTerms = searchTerms;
        }

        this.refreshSearchTerms();
    }

    /**
    Filter plants according to the current SearchTerms
     */
    public refreshSearchTerms(): void{
        if(!isUndefined(this.plantCollection)) {
            this.filterPlants();
        }
    }

    /**
     * Refresh one plant's information from db
     * and replace existing information
     * @param id
     * @param bed
     */
    public refreshPlant(id : string, bed : string){
        return this.getPlantById(id, bed, false).subscribe(plant => {
            this.plantCollection.updatePlant(plant);
            this.filterPlants();
        });
    }

    /**
     * Returns the current bed filter for the plants.
     * @returns {string} - bed filter for plants
     */
    public getBedFilter(): string{
        return this.bedFilter;
    }

    public getSearchTerms(): string{
        return this.searchTerms;
    }



}