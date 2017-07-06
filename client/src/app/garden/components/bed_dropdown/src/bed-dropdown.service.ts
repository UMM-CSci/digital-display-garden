/**
 * The BedListService requests all bed names from the server.
 * These bed names are then loaded into the BedListComponent for viewing and interaction.
 *
 * @author Iteration 2 - Team Omar Anwar
 * @editor Iteration 3 - Team Revolver en Guardia
 */
import {Injectable} from "@angular/core";
import {Observable} from "rxjs";
import {Bed} from "./bed";
import {Http} from "@angular/http";
import {BedCollection} from "./bedcollection";

@Injectable()
export class BedDropdownService {

    private readonly URL: string = process.env.API_URL;
    /**
     * Master collection of beds
     */
    private bedCollection: BedCollection;

    /**
     * Collection of beds shown in the BedDropdownComponent
     */
    private beds: Bed[];

    constructor(private http:Http) {
        this.getBedNamesFromServer().subscribe(
            bedNames => {
                this.bedCollection = new BedCollection(bedNames);
                this.beds = this.bedCollection.getBeds();
            },
            err => {
                console.log(err);
            }
        );
    }

    /**
     * Requests the list of bed names (garden locations) from the server.
     * @returns {Observable<R>} - the bed name collection from the server
     */
    private getBedNamesFromServer(): Observable<Bed[]> {
        return this.http.request(this.URL + "gardenLocations").map(res => res.json());
    }

    /**
     * Gets the Beds shown within the BedDropdownComponent
     * @returns {Bed[]} - Beds displayed in BedDropdownComponent
     */
    public getBedNames(): Bed[]{
        return this.beds;
    }

    /**
     * TODO: Comment
     * @param gardenLocation
     * @param isQR
     * @returns {Observable<R>}
     */
    reportBedVisit(gardenLocation : string, isQR : boolean): Observable<boolean> {
        console.log("!?");
        if(isQR) {
            return this.http.post(this.URL + "qrVisit", gardenLocation).map(res => res.json());
        } else {
            return this.http.post(this.URL + "bedVisit", gardenLocation).map(res => res.json());
        }
    }
}